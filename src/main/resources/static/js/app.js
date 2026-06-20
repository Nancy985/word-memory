// ---- State ----
let wordBooks = [];
let selectedBookId = localStorage.getItem('selectedBookId') || null;
let memorizeQueue = [];
let memorizeIndex = 0;
let memorizeRevealed = false;
let quizQuestions = [];
let quizIndex = 0;
let quizAnswers = [];
let quizStartTime = 0;

// ---- Utils ----
function toast(msg, isError = false) {
  const el = document.createElement('div');
  el.className = `toast${isError ? ' error' : ''}`;
  el.textContent = msg;
  document.getElementById('toast-container').appendChild(el);
  setTimeout(() => el.remove(), 3000);
}

function showModal(title, html, onConfirm) {
  const overlay = document.getElementById('modal-overlay');
  const content = document.getElementById('modal-content');
  content.innerHTML = `<h3>${title}</h3>${html}<div class="modal-actions">
    <button class="btn btn-ghost" id="modal-cancel">取消</button>
    <button class="btn btn-primary" id="modal-confirm">确定</button>
  </div>`;
  overlay.classList.remove('hidden');
  document.getElementById('modal-cancel').onclick = closeModal;
  document.getElementById('modal-confirm').onclick = () => { onConfirm(); };
}

function closeModal() {
  document.getElementById('modal-overlay').classList.add('hidden');
}

document.getElementById('modal-overlay').addEventListener('click', (e) => {
  if (e.target.id === 'modal-overlay') closeModal();
});

function getSelectedBookId() {
  return selectedBookId ? Number(selectedBookId) : (wordBooks[0]?.id ?? null);
}

function fillBookSelects() {
  const selects = ['global-book-select', 'memorize-book-select', 'quiz-book-select', 'wrong-book-select'];
  selects.forEach(id => {
    const sel = document.getElementById(id);
    if (!sel) return;
    const isWrong = id === 'wrong-book-select';
    sel.innerHTML = isWrong ? '<option value="">全部单词书</option>' : '';
    wordBooks.forEach(b => {
      const opt = document.createElement('option');
      opt.value = b.id;
      opt.textContent = `${b.name} (${b.wordCount} 词)`;
      sel.appendChild(opt);
    });
    if (!isWrong && getSelectedBookId()) sel.value = getSelectedBookId();
  });
}

async function loadWordBooks() {
  wordBooks = await api.getWordBooks();
  if (!selectedBookId && wordBooks.length) {
    selectedBookId = wordBooks[0].id;
    localStorage.setItem('selectedBookId', selectedBookId);
  }
  fillBookSelects();
}

// ---- Router ----
function navigate(route) {
  if (!api.getToken() && route !== 'auth') {
    location.hash = '#/login';
    return;
  }

  document.querySelectorAll('.page').forEach(p => p.classList.add('hidden'));
  document.querySelectorAll('.nav a').forEach(a => {
    a.classList.toggle('active', a.dataset.route === route);
  });

  const pages = {
    home: 'page-home',
    books: 'page-books',
    memorize: 'page-memorize',
    quiz: 'page-quiz',
    wrong: 'page-wrong',
    exams: 'page-exams',
  };

  if (pages[route]) {
    document.getElementById(pages[route]).classList.remove('hidden');
  }

  const loaders = {
    home: loadHome,
    books: loadBooks,
    memorize: loadMemorize,
    quiz: resetQuiz,
    wrong: loadWrong,
    exams: loadExams,
  };
  if (loaders[route]) loaders[route]();
}

function onHashChange() {
  const hash = location.hash.slice(2) || 'home';
  const route = hash === 'login' ? 'auth' : hash;

  if (route === 'auth') {
    document.getElementById('view-auth').classList.remove('hidden');
    document.getElementById('view-main').classList.add('hidden');
    return;
  }

  document.getElementById('view-auth').classList.add('hidden');
  document.getElementById('view-main').classList.remove('hidden');

  const user = api.getUser();
  if (user) document.getElementById('user-name').textContent = user.nickname || user.username;

  navigate(route);
}

// ---- Auth ----
document.querySelectorAll('.tab').forEach(tab => {
  tab.addEventListener('click', () => {
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    tab.classList.add('active');
    const isLogin = tab.dataset.tab === 'login';
    document.getElementById('form-login').classList.toggle('hidden', !isLogin);
    document.getElementById('form-register').classList.toggle('hidden', isLogin);
  });
});

document.getElementById('form-login').addEventListener('submit', async (e) => {
  e.preventDefault();
  const fd = new FormData(e.target);
  try {
    const data = await api.login(fd.get('username'), fd.get('password'));
    api.setToken(data.token);
    api.setUser(data.user);
    toast('登录成功');
    location.hash = '#/home';
  } catch (err) {
    toast(err.message, true);
  }
});

document.getElementById('form-register').addEventListener('submit', async (e) => {
  e.preventDefault();
  const fd = new FormData(e.target);
  try {
    const data = await api.register(fd.get('username'), fd.get('password'), fd.get('nickname') || undefined);
    api.setToken(data.token);
    api.setUser(data.user);
    toast('注册成功');
    location.hash = '#/home';
  } catch (err) {
    toast(err.message, true);
  }
});

document.getElementById('btn-logout').addEventListener('click', async () => {
  await api.logout();
  api.setToken(null);
  api.setUser(null);
  location.hash = '#/login';
});

// ---- Home ----
async function loadHome() {
  await loadWordBooks();
  const bookId = getSelectedBookId();
  if (!bookId) {
    document.getElementById('home-stats').innerHTML = '<div class="empty-state card"><p>还没有单词书，先去创建一个吧</p></div>';
    return;
  }

  try {
    const [progress, stats] = await Promise.all([
      api.getProgress(bookId),
      api.getWrongStats(),
    ]);
    document.getElementById('home-stats').innerHTML = `
      <div class="stat-card card"><div class="label">总单词</div><div class="value">${progress.totalWords}</div></div>
      <div class="stat-card card"><div class="label">已学习</div><div class="value">${progress.learnedWords}</div></div>
      <div class="stat-card card"><div class="label">今日待复习</div><div class="value">${progress.dueToday}</div></div>
      <div class="stat-card card"><div class="label">学习进度</div><div class="value">${progress.progressPercent}%</div></div>
      <div class="stat-card card"><div class="label">错词数</div><div class="value">${stats.totalWrongWords}</div></div>
      <div class="stat-card card"><div class="label">已掌握</div><div class="value">${progress.masteredWords}</div></div>`;
  } catch (err) {
    toast(err.message, true);
  }
}

document.getElementById('global-book-select').addEventListener('change', (e) => {
  selectedBookId = e.target.value;
  localStorage.setItem('selectedBookId', selectedBookId);
  loadHome();
});

// ---- Books ----
async function loadBooks() {
  await loadWordBooks();
  document.getElementById('book-detail').classList.add('hidden');
  document.getElementById('book-list').classList.remove('hidden');

  const list = document.getElementById('book-list');
  if (!wordBooks.length) {
    list.innerHTML = '<div class="empty-state card"><p>还没有单词书，点击右上角创建一个</p></div>';
    return;
  }

  list.innerHTML = wordBooks.map(b => `
    <div class="book-card card" data-id="${b.id}">
      <h3>${esc(b.name)}</h3>
      <p>${esc(b.description || '暂无描述')}</p>
      <div class="book-meta">
        <span class="tag">${b.wordCount} 个单词</span>
        <span>${b.isPublic ? '公开' : '私有'}</span>
      </div>
      <div class="book-actions">
        <button class="btn btn-primary btn-sm btn-open-book" data-id="${b.id}">查看单词</button>
        <button class="btn btn-ghost btn-sm btn-edit-book" data-id="${b.id}">编辑</button>
        <button class="btn btn-outline-danger btn-sm btn-del-book" data-id="${b.id}">删除</button>
      </div>
    </div>`).join('');

  list.querySelectorAll('.btn-open-book').forEach(btn => {
    btn.addEventListener('click', () => openBookDetail(Number(btn.dataset.id)));
  });
  list.querySelectorAll('.btn-edit-book').forEach(btn => {
    btn.addEventListener('click', (e) => { e.stopPropagation(); editBook(Number(btn.dataset.id)); });
  });
  list.querySelectorAll('.btn-del-book').forEach(btn => {
    btn.addEventListener('click', (e) => { e.stopPropagation(); deleteBook(Number(btn.dataset.id)); });
  });
}

function esc(str) {
  const d = document.createElement('div');
  d.textContent = str;
  return d.innerHTML;
}

document.getElementById('btn-new-book').addEventListener('click', () => {
  showModal('新建单词书', `
    <label>名称<input name="name" required placeholder="如：考研核心词汇"></label>
    <label>描述<textarea name="description" placeholder="可选"></textarea></label>
    <label>是否公开<input type="checkbox" name="isPublic" checked></label>`, async () => {
    const modal = document.getElementById('modal-content');
    const name = modal.querySelector('[name=name]').value.trim();
    const description = modal.querySelector('[name=description]').value.trim();
    const isPublic = modal.querySelector('[name=isPublic]').checked;
    if (!name) { toast('请输入名称', true); return; }
    try {
      await api.createWordBook({ name, description, isPublic });
      closeModal();
      toast('创建成功');
      loadBooks();
    } catch (err) { toast(err.message, true); }
  });
});

function editBook(id) {
  const book = wordBooks.find(b => b.id === id);
  if (!book) return;
  showModal('编辑单词书', `
    <label>名称<input name="name" value="${esc(book.name)}" required></label>
    <label>描述<textarea name="description">${esc(book.description || '')}</textarea></label>
    <label>是否公开<input type="checkbox" name="isPublic" ${book.isPublic ? 'checked' : ''}></label>`, async () => {
    const modal = document.getElementById('modal-content');
    try {
      await api.updateWordBook(id, {
        name: modal.querySelector('[name=name]').value.trim(),
        description: modal.querySelector('[name=description]').value.trim(),
        isPublic: modal.querySelector('[name=isPublic]').checked,
      });
      closeModal();
      toast('更新成功');
      loadBooks();
    } catch (err) { toast(err.message, true); }
  });
}

async function deleteBook(id) {
  if (!confirm('确定删除该单词书？所有单词将一并删除。')) return;
  try {
    await api.deleteWordBook(id);
    toast('已删除');
    loadBooks();
  } catch (err) { toast(err.message, true); }
}

async function openBookDetail(bookId) {
  const book = wordBooks.find(b => b.id === bookId);
  if (!book) return;
  selectedBookId = bookId;
  localStorage.setItem('selectedBookId', bookId);

  document.getElementById('book-list').classList.add('hidden');
  document.getElementById('book-detail').classList.remove('hidden');
  document.getElementById('detail-book-name').textContent = book.name;
  document.getElementById('detail-book-desc').textContent = book.description || '';

  const words = await api.getWords(bookId);
  const wrap = document.getElementById('word-list');
  if (!words.length) {
    wrap.innerHTML = '<div class="empty-state"><p>暂无单词，点击右上角添加</p></div>';
    return;
  }
  wrap.innerHTML = `<table class="word-table">
    <thead><tr><th>单词</th><th>音标</th><th>释义</th><th>例句</th><th></th></tr></thead>
    <tbody>${words.map(w => `<tr>
      <td><span class="word-spelling">${esc(w.spelling)}</span></td>
      <td class="word-phonetic">${esc(w.phonetic || '-')}</td>
      <td>${esc(w.meaning)}</td>
      <td>${esc(w.exampleSentence || '-')}</td>
      <td><button class="btn btn-outline-danger btn-sm btn-del-word" data-book="${bookId}" data-id="${w.id}">删除</button></td>
    </tr>`).join('')}</tbody></table>`;

  wrap.querySelectorAll('.btn-del-word').forEach(btn => {
    btn.addEventListener('click', async () => {
      if (!confirm('确定删除该单词？')) return;
      await api.deleteWord(Number(btn.dataset.book), Number(btn.dataset.id));
      toast('已删除');
      openBookDetail(bookId);
    });
  });
}

document.getElementById('btn-back-books').addEventListener('click', () => loadBooks());

document.getElementById('btn-import-words').addEventListener('click', () => {
  const bookId = getSelectedBookId();
  showModal('批量导入单词', `
    <div class="import-hint">
      每行一个单词，格式：<code>单词,音标,释义,例句</code><br>
      最少格式：<code>单词,释义</code><br>
      示例：<code>hello,/həˈloʊ/,int. 你好,Hello world!</code>
    </div>
    <label>粘贴 CSV 内容<textarea name="csv" rows="10" placeholder="abandon,/əˈbændən/,v. 放弃；遗弃,He abandoned his plan.&#10;ability,/əˈbɪləti/,n. 能力；才能,"></textarea></label>`, async () => {
    const csv = document.getElementById('modal-content').querySelector('[name=csv]').value;
    try {
      const result = await api.importWords(bookId, csv);
      closeModal();
      toast(`导入成功 ${result.imported} 个，跳过 ${result.skipped} 个`);
      openBookDetail(bookId);
    } catch (err) { toast(err.message, true); }
  });
});

document.getElementById('btn-add-word').addEventListener('click', () => {
  const bookId = getSelectedBookId();
  showModal('添加单词', `
    <label>单词<input name="spelling" required placeholder="abandon"></label>
    <label>音标<input name="phonetic" placeholder="/əˈbændən/"></label>
    <label>释义<input name="meaning" required placeholder="v. 放弃"></label>
    <label>例句<textarea name="exampleSentence" placeholder="He abandoned his plan."></textarea></label>`, async () => {
    const m = document.getElementById('modal-content');
    try {
      await api.addWord(bookId, {
        spelling: m.querySelector('[name=spelling]').value.trim(),
        phonetic: m.querySelector('[name=phonetic]').value.trim(),
        meaning: m.querySelector('[name=meaning]').value.trim(),
        exampleSentence: m.querySelector('[name=exampleSentence]').value.trim(),
      });
      closeModal();
      toast('添加成功');
      openBookDetail(bookId);
    } catch (err) { toast(err.message, true); }
  });
});

// ---- Memorize ----
async function renderEbbinghausCurve(bookId) {
  const panel = document.getElementById('ebbinghaus-panel');
  try {
    const curve = await api.getEbbinghausCurve(bookId);
    const maxCount = Math.max(1, ...curve.userDistribution.map(d => d.count), 1);
    const intervals = curve.stages.map(s => s.interval);
    const maxIntervalIdx = intervals.length - 1;

    const bars = curve.stages.map((s, i) => {
      const dist = curve.userDistribution.find(d => d.stage === i + 1);
      const count = dist ? dist.count : 0;
      const height = count > 0 ? Math.max(12, (count / maxCount) * 90) : 8;
      return `<div class="curve-bar-wrap">
        <span class="curve-bar-count">${count || ''}</span>
        <div class="curve-bar" style="height:${height}px" title="阶段${s.stage}: ${s.interval}"></div>
        <span class="curve-bar-label">T${s.stage}<br>${s.interval}</span>
      </div>`;
    }).join('');

    const stage0 = curve.userDistribution.find(d => d.stage === 0);
    const newCount = stage0 ? stage0.count : 0;

    panel.innerHTML = `
      <h3>📈 艾宾浩斯记忆曲线</h3>
      <p class="curve-desc">
        答对进入下一阶段，复习间隔依次延长：5分钟 → 30分钟 → 12小时 → 1天 → 2天 → 4天 → 7天 → 15天 → 30天。
        答错则重置到第1阶段。新词 / 未学习：<strong>${newCount}</strong> 个
      </p>
      <div class="curve-timeline">${bars}</div>
      <div class="curve-legend">
        <span>柱高 = 该阶段单词数量</span>
        <span>实现：SpacedRepetitionUtil.java</span>
      </div>`;
  } catch (err) {
    panel.innerHTML = '';
  }
}

function renderStageTrack(stage, isNew) {
  const labels = ['5分', '30分', '12时', '1天', '2天', '4天', '7天', '15天', '30天'];
  const current = isNew ? 0 : Math.min(stage, labels.length - 1);
  return `<div class="stage-track">${labels.map((l, i) =>
    `<span class="stage-dot ${i < current ? 'done' : ''} ${i === current ? 'current' : ''}">${l}</span>`
  ).join('')}</div>`;
}

async function loadMemorize() {
  await loadWordBooks();
  const bookId = getSelectedBookId();
  document.getElementById('memorize-book-select').value = bookId || '';

  if (!bookId) {
    document.getElementById('memorize-empty').classList.remove('hidden');
    document.getElementById('memorize-card').classList.add('hidden');
    document.getElementById('ebbinghaus-panel').innerHTML = '';
    return;
  }

  await renderEbbinghausCurve(bookId);

  try {
    const progress = await api.getProgress(bookId);
    document.getElementById('memorize-progress-text').textContent =
      `进度 ${progress.progressPercent}% · 今日待复习 ${progress.dueToday} 个 · 已掌握 ${progress.masteredWords} 个`;

    memorizeQueue = await api.getTodayWords(bookId, 20);
    memorizeIndex = 0;
    memorizeRevealed = false;

    if (!memorizeQueue.length) {
      document.getElementById('memorize-empty').classList.remove('hidden');
      document.getElementById('memorize-card').classList.add('hidden');
    } else {
      document.getElementById('memorize-empty').classList.add('hidden');
      document.getElementById('memorize-card').classList.remove('hidden');
      renderMemorizeCard();
    }
  } catch (err) { toast(err.message, true); }
}

function renderMemorizeCard() {
  const card = document.getElementById('memorize-card');
  if (memorizeIndex >= memorizeQueue.length) {
    card.classList.add('hidden');
    document.getElementById('memorize-empty').classList.remove('hidden');
    document.getElementById('memorize-empty').querySelector('p').textContent = '🎉 本轮复习完成！';
    return;
  }

  const w = memorizeQueue[memorizeIndex];
  const remaining = memorizeQueue.length - memorizeIndex;

  if (!memorizeRevealed) {
    card.innerHTML = `
      <div class="word">${esc(w.spelling)}</div>
      <div class="phonetic">${esc(w.phonetic || '')}</div>
      ${renderStageTrack(w.reviewStage || 0, w.new)}
      <p style="color:var(--text-muted);margin:12px 0 24px">还剩 ${remaining} 个 · ${w.new ? '新词 · 首次间隔 5 分钟' : `第 ${w.reviewStage} 阶段 · 下次 ${w.nextIntervalLabel || ''} 后复习`}</p>
      <button class="btn-reveal" id="btn-reveal">显示释义</button>`;
    document.getElementById('btn-reveal').onclick = () => { memorizeRevealed = true; renderMemorizeCard(); };
  } else {
    card.innerHTML = `
      <div class="word">${esc(w.spelling)}</div>
      <div class="phonetic">${esc(w.phonetic || '')}</div>
      <div class="meaning">${esc(w.meaning)}</div>
      ${w.exampleSentence ? `<div class="example">${esc(w.exampleSentence)}</div>` : ''}
      <div class="flashcard-actions">
        <button class="btn-forget" id="btn-forget">不认识（重置到 5 分钟）</button>
        <button class="btn-know" id="btn-know">认识（延长间隔）</button>
      </div>
      <span class="stage-badge">熟悉度 ${w.familiarity ?? 0}% · 下次复习 ${formatDate(w.nextReviewAt)}</span>`;
    document.getElementById('btn-know').onclick = () => submitMemorize(true);
    document.getElementById('btn-forget').onclick = () => submitMemorize(false);
  }
}

async function submitMemorize(correct) {
  const w = memorizeQueue[memorizeIndex];
  try {
    const result = await api.submitReview(w.wordId, correct);
    toast(correct
      ? `✓ 进入第 ${result.reviewStage} 阶段，${result.nextIntervalLabel || ''}后复习`
      : '已重置到第 1 阶段，5 分钟后复习');
    memorizeIndex++;
    memorizeRevealed = false;
    const bookId = getSelectedBookId();
    if (bookId) renderEbbinghausCurve(bookId);
    renderMemorizeCard();
  } catch (err) { toast(err.message, true); }
}

document.getElementById('memorize-book-select').addEventListener('change', (e) => {
  selectedBookId = e.target.value;
  localStorage.setItem('selectedBookId', selectedBookId);
  loadMemorize();
});

// ---- Quiz ----
function resetQuiz() {
  document.getElementById('quiz-setup').classList.remove('hidden');
  document.getElementById('quiz-active').classList.add('hidden');
  document.getElementById('quiz-result').classList.add('hidden');
  loadWordBooks();
}

document.getElementById('btn-start-quiz').addEventListener('click', async () => {
  const bookId = Number(document.getElementById('quiz-book-select').value) || getSelectedBookId();
  const count = Number(document.getElementById('quiz-count').value) || 10;
  if (!bookId) { toast('请选择单词书', true); return; }

  try {
    quizQuestions = await api.generateQuiz(bookId, count);
    quizIndex = 0;
    quizAnswers = [];
    quizStartTime = Date.now();
    document.getElementById('quiz-setup').classList.add('hidden');
    document.getElementById('quiz-result').classList.add('hidden');
    document.getElementById('quiz-active').classList.remove('hidden');
    renderQuizQuestion();
  } catch (err) { toast(err.message, true); }
});

function renderQuizQuestion() {
  const q = quizQuestions[quizIndex];
  const total = quizQuestions.length;
  document.getElementById('quiz-progress-label').textContent = `${quizIndex + 1} / ${total}`;
  document.getElementById('quiz-progress-bar').style.width = `${((quizIndex + 1) / total) * 100}%`;

  const container = document.getElementById('quiz-question');
  container.innerHTML = `
    <div class="quiz-word">${esc(q.spelling)}</div>
    <div class="quiz-phonetic">${esc(q.phonetic || '')}</div>
    <div class="quiz-options">${q.options.map((opt, i) =>
      `<button class="quiz-option" data-idx="${i}">${esc(opt)}</button>`).join('')}</div>`;

  container.querySelectorAll('.quiz-option').forEach(btn => {
    btn.addEventListener('click', () => selectQuizOption(btn, q));
  });
}

async function selectQuizOption(btn, question) {
  const selected = btn.textContent;
  quizAnswers.push({ wordId: question.wordId, userAnswer: selected });

  document.querySelectorAll('.quiz-option').forEach(b => {
    b.disabled = true;
    if (b.textContent === selected) b.classList.add('selected');
  });

  await new Promise(r => setTimeout(r, 400));
  quizIndex++;
  if (quizIndex >= quizQuestions.length) {
    await finishQuiz();
  } else {
    renderQuizQuestion();
  }
}

async function finishQuiz() {
  const bookId = Number(document.getElementById('quiz-book-select').value) || getSelectedBookId();
  const duration = Math.round((Date.now() - quizStartTime) / 1000);
  try {
    const result = await api.submitQuiz(bookId, quizAnswers, duration);
    document.getElementById('quiz-active').classList.add('hidden');
    document.getElementById('quiz-result').classList.remove('hidden');
    document.getElementById('result-score').textContent = result.score;
    document.getElementById('result-detail').textContent =
      `答对 ${result.correctCount} / ${result.totalCount} 题 · 用时 ${duration} 秒`;
  } catch (err) { toast(err.message, true); }
}

document.getElementById('btn-quiz-retry').addEventListener('click', resetQuiz);

// ---- Wrong words ----
async function loadWrong() {
  await loadWordBooks();
  const bookId = document.getElementById('wrong-book-select').value;

  try {
    const [stats, words] = await Promise.all([
      api.getWrongStats(),
      api.getWrongWords(bookId || undefined),
    ]);

    document.getElementById('wrong-stats').innerHTML = `
      <div class="stat-card card"><div class="label">错词种类</div><div class="value">${stats.totalWrongWords}</div></div>
      <div class="stat-card card"><div class="label">累计错误</div><div class="value">${stats.totalWrongTimes}</div></div>
      <div class="stat-card card"><div class="label">已复习</div><div class="value">${stats.reviewedWords}</div></div>
      <div class="stat-card card"><div class="label">已掌握</div><div class="value">${stats.masteredWords}</div></div>`;

    const wrap = document.getElementById('wrong-list');
    if (!words.length) {
      wrap.innerHTML = '<div class="empty-state"><p>暂无错词，继续保持！</p></div>';
      return;
    }
    wrap.innerHTML = `<table class="word-table">
      <thead><tr><th>单词</th><th>释义</th><th>单词书</th><th>错误次数</th><th>最近错误</th></tr></thead>
      <tbody>${words.map(w => `<tr>
        <td><span class="word-spelling">${esc(w.spelling)}</span><br><span class="word-phonetic">${esc(w.phonetic || '')}</span></td>
        <td>${esc(w.meaning)}</td>
        <td>${esc(w.wordBookName || '-')}</td>
        <td><strong style="color:#c0392b">${w.wrongCount}</strong></td>
        <td>${formatDate(w.lastWrongAt)}</td>
      </tr>`).join('')}</tbody></table>`;
  } catch (err) { toast(err.message, true); }
}

document.getElementById('wrong-book-select').addEventListener('change', loadWrong);

function formatDate(str) {
  if (!str) return '-';
  return str.replace('T', ' ').slice(0, 16);
}

// ---- Exams ----
async function loadExams() {
  try {
    const exams = await api.getExams();
    const list = document.getElementById('exam-list');
    if (!exams.length) {
      list.innerHTML = '<div class="empty-state card"><p>还没有考试记录，去做一次自测吧</p><a href="#/quiz" class="btn btn-primary">开始自测</a></div>';
      return;
    }
    list.innerHTML = exams.map(e => `
      <div class="exam-item card" data-id="${e.id}">
        <div>
          <h4>${esc(e.wordBookName || '单词书')} · ${e.examType === 'SELF_TEST' ? '自测' : '背诵'}</h4>
          <p>${formatDate(e.createdAt)} · 答对 ${e.correctCount}/${e.totalCount} · 用时 ${e.durationSeconds || '-'} 秒</p>
        </div>
        <div class="exam-score">${e.score}</div>
      </div>`).join('');

    list.querySelectorAll('.exam-item').forEach(item => {
      item.addEventListener('click', () => showExamDetail(Number(item.dataset.id)));
    });
  } catch (err) { toast(err.message, true); }
}

async function showExamDetail(id) {
  try {
    const exam = await api.getExam(id);
    const answersHtml = (exam.answers || []).map(a => `
      <div class="detail-answer-item ${a.isCorrect ? 'correct' : 'wrong'}">
        <strong>${esc(a.spelling)}</strong> — 你的答案：${esc(a.userAnswer)} · 正确答案：${esc(a.correctAnswer)}
        ${a.isCorrect ? ' ✓' : ' ✗'}
      </div>`).join('');

    showModal(`考试详情 · ${exam.score} 分`, `
      <p style="color:var(--text-muted);margin-bottom:12px">${esc(exam.wordBookName)} · ${formatDate(exam.createdAt)}</p>
      <div class="detail-answers">${answersHtml || '<p>无答题明细</p>'}</div>`, closeModal);
    document.getElementById('modal-confirm').textContent = '关闭';
    document.getElementById('modal-confirm').onclick = closeModal;
    document.getElementById('modal-cancel').classList.add('hidden');
  } catch (err) { toast(err.message, true); }
}

// ---- Init ----
window.addEventListener('hashchange', onHashChange);

if (api.getToken()) {
  location.hash = location.hash || '#/home';
} else {
  location.hash = '#/login';
}
onHashChange();
