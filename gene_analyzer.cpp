// 基因序列分析：限制性内切酶切点分析 + 虚拟凝胶电泳 + ORF 识别 + 蛋白翻译
// 编译 g++ -std=c++17 -O2 gene_analyzer.cpp -o gene_analyzer
// 运行 ./gene_analyzer [--demo | 序列文件]

#include <algorithm>
#include <cctype>
#include <cmath>
#include <fstream>
#include <iomanip>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

// cut = 切点在识别序列 5' 端起第几个碱基之后，例如 EcoRI 的 G^AATTC 为 1
struct Enzyme { string name, site; int cut; };
const vector<Enzyme> ENZYMES = {
    {"EcoRI", "GAATTC", 1},   {"HindIII", "AAGCTT", 1}, {"BamHI", "GGATCC", 1},
    {"XhoI", "CTCGAG", 1},    {"XbaI", "TCTAGA", 1},    {"SalI", "GTCGAC", 1},
    {"KpnI", "GGTACC", 5},    {"PstI", "CTGCAG", 5},    {"SmaI", "CCCGGG", 3},
    {"NotI", "GCGGCCGC", 2},  {"BsaI", "GGTCTC", 1},
};
const int MIN_AA = 30;   // 判定可用蛋白的最小氨基酸数

// ---------------- 序列处理 ----------------
char comp(char c) {
    switch (c) {
        case 'A': return 'T'; case 'T': return 'A';
        case 'C': return 'G'; case 'G': return 'C';
        default:  return 'N';
    }
}
string revComp(string s) {
    reverse(s.begin(), s.end());
    for (char& c : s) c = comp(c);
    return s;
}
string trim(const string& s) {
    size_t b = s.find_first_not_of(" \t\r\n");
    if (b == string::npos) return "";
    return s.substr(b, s.find_last_not_of(" \t\r\n") - b + 1);
}
// 去掉 FASTA 头与空白，U 转 T，统一大写
string cleanSeq(const string& raw) {
    string s;
    istringstream in(raw);
    string line;
    while (getline(in, line)) {
        if (!line.empty() && (line[0] == '>' || line[0] == ';')) continue;
        for (char ch : line)
            if (isalpha((unsigned char)ch)) {
                char c = toupper((unsigned char)ch);
                s += (c == 'U' ? 'T' : c);
            }
    }
    return s;
}
string badChars(const string& s) {
    string bad;
    for (char c : s)
        if (string("ACGTN").find(c) == string::npos && bad.find(c) == string::npos) bad += c;
    return bad;
}
double gcPercent(const string& s) {
    if (s.empty()) return 0;
    int gc = 0;
    for (char c : s) if (c == 'G' || c == 'C') ++gc;
    return 100.0 * gc / s.size();
}

// ---------------- 酶切 ----------------
vector<int> cutSites(const string& seq, const Enzyme& e) {
    vector<int> cuts;
    int n = seq.size(), m = e.site.size();

    for (size_t p = seq.find(e.site); p != string::npos; p = seq.find(e.site, p + 1))
        cuts.push_back(p + e.cut);

    // 非回文酶还需在反向互补链上搜索，并换算回正向坐标
    if (e.site != revComp(e.site)) {
        string rc = revComp(e.site);
        for (size_t p = seq.find(rc); p != string::npos; p = seq.find(rc, p + 1))
            cuts.push_back(p + m - e.cut);
    }

    sort(cuts.begin(), cuts.end());
    cuts.erase(unique(cuts.begin(), cuts.end()), cuts.end());
    cuts.erase(remove_if(cuts.begin(), cuts.end(), [n](int c) { return c <= 0 || c >= n; }), cuts.end());
    return cuts;
}
vector<int> mergeCuts(const vector<vector<int>>& groups) {
    vector<int> out;
    for (auto& g : groups) out.insert(out.end(), g.begin(), g.end());
    sort(out.begin(), out.end());
    out.erase(unique(out.begin(), out.end()), out.end());
    return out;
}
struct Fragment {
    int start, end;
    int len() const { return end - start; }
};
vector<Fragment> digest(const vector<int>& cuts, int seqLen) {
    vector<Fragment> frs;
    int prev = 0;
    for (int c : cuts) {
        if (c > prev) frs.push_back({prev, c});
        prev = c;
    }
    if (prev < seqLen) frs.push_back({prev, seqLen});
    return frs;
}

// ---------------- 虚拟凝胶电泳：log(长度) 映射到行号，短片段跑得远（靠下） ----------------
string pad(const string& s, int w) {
    if ((int)s.size() >= w) return s.substr(0, w);
    int left = (w - (int)s.size()) / 2;
    return string(left, ' ') + s + string(w - s.size() - left, ' ');
}
void printGel(const vector<Fragment>& frs) {
    if (frs.empty()) { cout << "  （无片段）\n"; return; }

    const vector<int> ladder = {10000, 6000, 3000, 1500, 1000, 500, 250, 100};
    const int ROWS = 18, W = 6;
    int mx = ladder.front(), mn = ladder.back();
    for (auto& f : frs) { mx = max(mx, f.len()); mn = min(mn, f.len()); }

    double hi = log(max(mx, 2)), lo = log(max(mn, 2));
    if (hi - lo < 1e-9) hi = lo + 1e-9;
    auto row = [&](int len) {
        return min((int)llround((hi - log((double)max(len, 2))) / (hi - lo) * (ROWS - 1)), ROWS - 1);
    };

    int lanes = frs.size();
    vector<vector<string>> grid(ROWS, vector<string>(lanes + 1));
    for (int l : ladder) grid[row(l)][0] = "=";
    for (int j = 0; j < lanes; ++j) grid[row(frs[j].len())][j + 1] = "##";

    auto border = [&] {
        cout << "    +";
        for (int j = 0; j <= lanes; ++j) cout << string(W, '-') << "+";
        cout << "\n";
    };
    auto line = [&](const vector<string>& cells) {
        cout << "    |";
        for (auto& s : cells) cout << pad(s, W) << "|";
        cout << "\n";
    };

    cout << "     " << pad("M", W + 1);
    for (int j = 0; j < lanes; ++j) cout << pad("F" + to_string(j + 1), W + 1);
    cout << "\n";
    border();
    line(vector<string>(lanes + 1, "[]"));
    for (auto& r : grid) line(r);
    border();

    cout << "  泳道长度:";
    for (int j = 0; j < lanes; ++j) cout << " F" << j + 1 << "=" << frs[j].len() << "bp";
    cout << "\n  Marker:";
    for (int l : ladder) cout << " " << l;
    cout << "\n";
}

// ---------------- 翻译：按 T,C,A,G 顺序的 64 密码子表 ----------------
char codon2aa(const string& c) {
    static const string TBL =
        "FFLLSSSSYY**CC*WLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG";
    auto idx = [](char b) { return b == 'T' ? 0 : b == 'C' ? 1 : b == 'A' ? 2 : b == 'G' ? 3 : -1; };
    int i1 = idx(c[0]), i2 = idx(c[1]), i3 = idx(c[2]);
    if (i1 < 0 || i2 < 0 || i3 < 0) return 'X';   // 含 N 的密码子
    return TBL[i1 * 16 + i2 * 4 + i3];
}
bool isStop(const string& c) { return c == "TAA" || c == "TAG" || c == "TGA"; }

struct ORF {
    int strand = 1, frame = 0, start = 0, end = 0;
    bool complete = false;
    string dna, protein;
    int aa() const { return protein.size(); }
};

// 正反两条链 × 3 个阅读框扫描，从 ATG 到终止密码子
vector<ORF> findORFs(const string& seq, int minAA) {
    vector<ORF> out;
    int L = seq.size();

    for (int strand : {1, -1}) {
        string s = strand == 1 ? seq : revComp(seq);
        int n = s.size();
        for (int f = 0; f < 3; ++f) {
            int i = f;
            while (i + 3 <= n) {
                if (s.compare(i, 3, "ATG") != 0) { i += 3; continue; }

                string prot;
                int j = i;
                bool done = false;
                while (j + 3 <= n) {
                    string cod = s.substr(j, 3);
                    if (isStop(cod)) { done = true; break; }
                    prot += codon2aa(cod);
                    j += 3;
                }
                int end = done ? j + 3 : j;

                if ((int)prot.size() >= minAA) {
                    ORF o;
                    o.strand = strand;
                    o.frame = f;
                    o.complete = done;
                    o.dna = s.substr(i, end - i);
                    o.protein = prot;
                    o.start = strand == 1 ? i : L - end;
                    o.end = strand == 1 ? end : L - i;
                    out.push_back(o);
                }
                i = done ? end : i + 3;
            }
        }
    }
    sort(out.begin(), out.end(), [](const ORF& a, const ORF& b) {
        if (a.complete != b.complete) return a.complete > b.complete;
        return a.aa() > b.aa();
    });
    return out;
}

// 可用蛋白 = ORF 完整 + 长度达标 + 切点不落在 ORF 内部
bool usable(const ORF& o, const vector<int>& cuts, string& why) {
    if (!o.complete) return why = "无终止密码子，不完整", false;
    if (o.aa() < MIN_AA) return why = "少于 " + to_string(MIN_AA) + " aa", false;
    for (int c : cuts)
        if (c > o.start && c < o.end) return why = "被酶切位点打断", false;
    why = "完整且未被切断，可编码完整蛋白质";
    return true;
}
int fragmentOf(const ORF& o, const vector<Fragment>& frs) {
    for (int i = 0; i < (int)frs.size(); ++i)
        if (o.start >= frs[i].start && o.end <= frs[i].end) return i + 1;
    return 0;
}

// ---------------- 报告 ----------------
string wrap(const string& s, size_t w) {
    string out;
    for (size_t i = 0; i < s.size(); i += w) out += s.substr(i, w) + "\n";
    return out;
}

void digestReport(const string& seq, const vector<Enzyme>& ens) {
    vector<vector<int>> groups;
    for (auto& e : ens) groups.push_back(cutSites(seq, e));
    vector<int> cuts = mergeCuts(groups);
    vector<Fragment> frs = digest(cuts, seq.size());

    cout << "\n============ 酶切结果 ============\n";
    cout << "  长度 " << seq.size() << " bp，用酶 ";
    for (int i = 0; i < (int)ens.size(); ++i) cout << ens[i].name << (i + 1 < (int)ens.size() ? " + " : "");
    cout << (ens.size() == 1 ? "（单酶切）" : "（多酶切）") << "\n  切点:";
    if (cuts.empty()) cout << " 无";
    for (int c : cuts) cout << " " << c;

    cout << "\n  片段:\n    #   起点   终点   长度\n";
    for (int i = 0; i < (int)frs.size(); ++i)
        cout << "    " << setw(2) << i + 1 << setw(7) << frs[i].start
             << setw(7) << frs[i].end << setw(7) << frs[i].len() << "\n";

    cout << "\n============ 虚拟电泳 ============\n";
    printGel(frs);
    cout << "\n";
}

void orfReport(const string& seq, const vector<int>& cuts) {
    vector<ORF> orfs = findORFs(seq, MIN_AA);
    if (orfs.empty()) { cout << "\n  未找到长度 ≥ " << MIN_AA << " aa 的 ORF。\n"; return; }
    vector<Fragment> frs = digest(cuts, seq.size());

    cout << "\n============ ORF 分析 ============\n";
    cout << "   #  链   坐标          nt  aa  完整 被切 片段  可用\n";
    int okCnt = 0;
    for (int i = 0; i < (int)orfs.size(); ++i) {
        const ORF& o = orfs[i];
        string why;
        bool ok = usable(o, cuts, why);
        int frag = fragmentOf(o, frs);
        okCnt += ok;
        cout << "  " << setw(2) << i + 1 << "  " << (o.strand == 1 ? "+" : "-")
             << "  " << setw(9) << (to_string(o.start) + "-" + to_string(o.end))
             << "  " << setw(4) << o.dna.size() << " " << setw(3) << o.aa()
             << "  " << (o.complete ? "FULL" : "PART") << "  " << (ok ? "no " : "YES")
             << "   " << (frag ? "F" + to_string(frag) : "-")
             << "   " << (ok ? "USABLE" : "no") << "\n";
    }

    cout << "\n  【结论】\n";
    for (int i = 0; i < (int)orfs.size(); ++i) {
        string why;
        if (!usable(orfs[i], cuts, why)) continue;
        const ORF& o = orfs[i];
        cout << "  ORF" << i + 1 << " " << o.start << "~" << o.end << "（" << o.dna.size()
             << " bp / " << o.aa() << " aa，" << (o.strand == 1 ? "正向" : "反向") << "）可用\n"
             << "      " << why << "，位于片段 F" << fragmentOf(o, frs) << "\n"
             << "      分子量约 " << fixed << setprecision(1) << o.aa() * 110.0 / 1000.0 << " kDa\n";
    }
    if (!okCnt) {
        cout << "  当前酶切体系下没有可完整翻译的 ORF。\n";
        for (auto& o : orfs)
            if (o.complete) cout << "  注意：ORF " << o.start << "~" << o.end << " 被切点打断，可换酶或减少酶数。\n";
    }
    cout << "\n";
}

void printProteins(const string& seq, const vector<int>& cuts) {
    vector<ORF> orfs = findORFs(seq, MIN_AA);
    int shown = 0;
    for (int i = 0; i < (int)orfs.size(); ++i) {
        string why;
        if (!usable(orfs[i], cuts, why)) continue;
        const ORF& o = orfs[i];
        cout << "\n-------- ORF" << i + 1 << "（" << (o.strand == 1 ? "+" : "-")
             << " 链，框 " << o.frame << "，坐标 " << o.start << "-" << o.end << "，" << o.aa() << " aa）--------\n"
             << "  DNA:\n" << wrap(o.dna, 60) << "  蛋白:\n" << wrap(o.protein, 60);
        ++shown;
    }
    if (!shown) cout << "  没有可用蛋白。\n";
}

void exportAll(const string& seq, const vector<Enzyme>& ens, const vector<int>& cuts, const vector<Fragment>& frs) {
    string names, cutStr;
    for (int i = 0; i < (int)ens.size(); ++i) names += ens[i].name + (i + 1 < (int)ens.size() ? "+" : "");
    for (int c : cuts) cutStr += to_string(c) + ";";

    ofstream f1("gene_result_fragments.csv");
    f1 << "enzyme_set,seq_len,cut_positions,fragment,start,end,len\n";
    for (int i = 0; i < (int)frs.size(); ++i)
        f1 << names << "," << seq.size() << ",\"" << cutStr << "\"," << i + 1
           << "," << frs[i].start << "," << frs[i].end << "," << frs[i].len() << "\n";

    ofstream f2("gene_result_cds.fasta"), f3("gene_result_protein.fasta");
    vector<ORF> orfs = findORFs(seq, MIN_AA);
    int saved = 0;
    for (int i = 0; i < (int)orfs.size(); ++i) {
        string why;
        if (!usable(orfs[i], cuts, why)) continue;
        const ORF& o = orfs[i];
        string tag = "ORF" + to_string(i + 1) + " strand=" + (o.strand == 1 ? "+" : "-") +
                     " pos=" + to_string(o.start) + "-" + to_string(o.end) + " " + to_string(o.aa()) + "aa";
        f2 << ">" << tag << "\n" << wrap(o.dna, 60);
        f3 << ">" << tag << "\n" << wrap(o.protein, 60);
        ++saved;
    }

    cout << "\n============ 已导出 ============\n"
         << "  gene_result_fragments.csv   切点与片段\n"
         << "  gene_result_cds.fasta       可用蛋白 CDS（" << saved << " 条）\n"
         << "  gene_result_protein.fasta   蛋白质序列\n"
         << "  下一步：在突变体序列同一坐标区间逐位比较，即可定位突变位点。\n";
}

// ---------------- 内置演示序列 ----------------
string demoSeq() {
    string leader = "GGTACCCGCGATCGATCGATCGATCGATCGGAATTCTTGCACGAT";   // 含 KpnI / EcoRI
    string cds = "ATGGCTGACCTGAAACGTTACGGCATCGTGCCGAATAGCGAAGTTCTGGCAAAATGGTTT"
                 "GATCGCATCCCGACTGAAGCCTGGAAAGCACTGGGTATTCCGCGTGACTAA";   // 36 aa
    string trailer = "GCTAGCTAGCTAGCTAAGCTTGGATCCCTCGAG";             // 含 HindIII / BamHI / XhoI
    return leader + cds + trailer;
}

// ---------------- 主程序 ----------------
string g_seq;
vector<Enzyme> g_ens;
vector<int> g_cuts;
vector<Fragment> g_frs;

void recalc() {
    vector<vector<int>> groups;
    for (auto& e : g_ens) groups.push_back(cutSites(g_seq, e));
    g_cuts = mergeCuts(groups);
    g_frs = digest(g_cuts, g_seq.size());
}

void showState() {
    if (g_seq.empty()) { cout << "  序列: 未输入    酶: 未选择\n"; return; }
    cout << "  序列: " << g_seq.size() << " bp (" << fixed << setprecision(1) << gcPercent(g_seq) << "% GC)";
    string bad = badChars(g_seq);
    if (!bad.empty()) cout << "  [非法字符: " << bad << "]";
    cout << "    酶: ";
    if (g_ens.empty()) cout << "未选择";
    for (int i = 0; i < (int)g_ens.size(); ++i) cout << g_ens[i].name << (i + 1 < (int)g_ens.size() ? "+" : "");
    cout << "\n";
}

void inputSeq() {
    cout << "  1 手输  2 读文件  3 内置演示  选择: ";
    string ch;
    if (!getline(cin, ch)) return;
    ch = trim(ch);

    if (ch == "3") {
        g_seq = demoSeq();
    } else if (ch == "2") {
        cout << "  文件路径: ";
        string path;
        if (!getline(cin, path)) return;
        ifstream fin(trim(path));
        if (!fin) { cout << "  打开失败。\n"; return; }
        stringstream ss;
        ss << fin.rdbuf();
        g_seq = cleanSeq(ss.str());
    } else {
        cout << "  粘贴序列（FASTA 可用，输入 END 结束）:\n";
        string line, raw;
        while (getline(cin, line)) {
            if (trim(line) == "END") break;
            if (trim(line).empty() && !raw.empty()) break;
            raw += line + "\n";
        }
        g_seq = cleanSeq(raw);
    }
    if (g_seq.empty()) { cout << "  未读到有效序列。\n"; return; }
    cout << "  已载入 " << g_seq.size() << " bp。\n";
    if (!g_ens.empty()) recalc();
}

void inputEnzyme() {
    cout << "  可用酶:\n";
    for (int i = 0; i < (int)ENZYMES.size(); ++i)
        cout << "    " << setw(2) << i + 1 << ") " << left << setw(8) << ENZYMES[i].name
             << right << ENZYMES[i].site << "\n";
    cout << "  输入编号（多个用空格分隔，如 1 2；0 取消）: ";

    string line;
    if (!getline(cin, line)) return;
    istringstream is(line);
    int x;
    vector<Enzyme> chosen;
    while (is >> x) {
        if (x == 0) return;
        if (x >= 1 && x <= (int)ENZYMES.size()) chosen.push_back(ENZYMES[x - 1]);
    }
    if (chosen.empty()) { cout << "  未选择任何酶。\n"; return; }
    g_ens = chosen;
    recalc();
}

void demo() {
    g_seq = demoSeq();
    g_ens = {ENZYMES[0], ENZYMES[1]};
    recalc();
    cout << "\n============ 一键演示 ============\n";
    showState();
    for (auto& e : g_ens) digestReport(g_seq, {e});
    digestReport(g_seq, g_ens);
    orfReport(g_seq, g_cuts);
    printProteins(g_seq, g_cuts);
    exportAll(g_seq, g_ens, g_cuts, g_frs);
}

int main(int argc, char** argv) {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    if (argc > 1 && string(argv[1]) == "--demo") { demo(); return 0; }
    if (argc > 1) {
        ifstream fin(argv[1]);
        if (fin) {
            stringstream ss;
            ss << fin.rdbuf();
            g_seq = cleanSeq(ss.str());
            cout << "已载入 " << g_seq.size() << " bp。\n";
        }
    }

    while (true) {
        cout << "\n=== DNA 酶切 / ORF / 蛋白翻译 ===\n";
        showState();
        cout << "  1 输入序列   2 选择酶   3 酶切分析   4 ORF 分析\n"
                "  5 翻译+导出  6 一键演示  0 退出\n  请选择: ";

        string line;
        if (!getline(cin, line)) break;
        string ch = trim(line);

        if (ch == "0") break;
        else if (ch == "1") inputSeq();
        else if (ch == "2") inputEnzyme();
        else if (ch == "3") {
            if (g_seq.empty() || g_ens.empty()) { cout << "  请先输入序列并选择酶。\n"; continue; }
            if (g_ens.size() > 1)
                for (auto& e : g_ens) digestReport(g_seq, {e});
            digestReport(g_seq, g_ens);
        } else if (ch == "4") {
            if (g_seq.empty()) { cout << "  请先输入序列。\n"; continue; }
            if (!g_ens.empty()) recalc();
            orfReport(g_seq, g_cuts);
        } else if (ch == "5") {
            if (g_seq.empty()) { cout << "  请先输入序列。\n"; continue; }
            if (!g_ens.empty()) recalc();
            printProteins(g_seq, g_cuts);
            exportAll(g_seq, g_ens, g_cuts, g_frs);
        } else if (ch == "6") demo();
    }
    cout << "再见。\n";
    return 0;
}
