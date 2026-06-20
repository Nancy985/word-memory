# Git 分支管理与提交规范

> 参考：[阮一峰 Git 分支管理策略](https://www.ruanyifeng.com/blog/2012/07/git.html)、[Commit Message 规范](https://blog.csdn.net/hzf0701/article/details/134367234)、[IDEA Git Commit Template 插件](https://blog.csdn.net/qq_20042935/article/details/108167597)

---

## 一、要求总结（@全体成员 必遵守）

### 1. 分支管理（Git Flow）

| 分支 | 用途 | 命名 | 说明 |
|------|------|------|------|
| **main** | 正式发布 | `main` | 仅存放稳定、可交付版本；禁止直接在上面开发 |
| **develop** | 日常开发 | `develop` | 所有功能集成分支；日常开发的主线 |
| **feature** | 新功能 | `feature-功能名` | 从 `develop` 分出，完成后 `--no-ff` 合并回 `develop` |
| **release** | 预发布 | `release-版本号` | 从 `develop` 分出，测试通过后合并到 `main` 和 `develop` |
| **fixbug / hotfix** | 紧急修 bug | `fixbug-描述` | 从 `main` 分出，修复后合并到 `main` 和 `develop` |

**核心原则：**
- 常设分支只有 **main** 和 **develop**
- 临时分支用完即删
- 合并到 main 时使用 `git merge --no-ff`，保留分支历史
- 发布版本在 main 上打 tag，如 `git tag -a v1.0.0`

### 2. 日常开发流程

```bash
# 1. 从 develop 拉功能分支
git checkout develop
git pull
git checkout -b feature-user-login develop

# 2. 开发并提交（见下方提交规范）
git add .
git commit -m "feat(auth): 实现用户登录接口"

# 3. 合并回 develop（禁止快进）
git checkout develop
git merge --no-ff feature-user-login
git branch -d feature-user-login

# 4. 发布时：develop → release → main
git checkout -b release-1.0 develop
# 测试通过后
git checkout main
git merge --no-ff release-1.0
git tag -a v1.0.0 -m "v1.0.0 首次发布"
git checkout develop
git merge --no-ff release-1.0
git branch -d release-1.0
```

### 3. 提交信息规范（Conventional Commits）

**格式：**

```
<type>(<scope>): <subject>

<body>

<footer>
```

- **Header 必填**，Body / Footer 可选
- **subject** 不超过 50 字，动词开头，首字母小写，结尾不加句号
- 建议使用**中文**描述 subject（IDEA 插件推荐）

**type 类型：**

| type | 含义 |
|------|------|
| `feat` | 新功能 |
| `fix` | 修复 bug（一次提交直接修好） |
| `to` | 部分修复，多次提交中的中间步骤 |
| `docs` | 文档变更 |
| `style` | 代码格式（不影响逻辑） |
| `refactor` | 重构 |
| `perf` | 性能优化 |
| `test` | 测试相关 |
| `chore` | 构建/工具/依赖变更 |
| `revert` | 回滚 |
| `merge` | 分支合并 |
| `sync` | 同步主线 bug 修复 |

**scope 示例：** `controller`、`service`、`frontend`、`auth`、`wordbook`

**示例：**

```
feat(auth): 实现用户登录注册功能

添加 JWT Token 认证与 BCrypt 密码加密，
支持 /api/auth/login 和 /api/auth/register 接口。

Closes #12
```

```
fix(quiz): 修复四选一选项不足 4 个时的报错
```

### 4. IDEA 插件配置

1. 打开 **Settings → Plugins**
2. 搜索 **Git Commit Template** 并安装
3. 重启 IDEA
4. 提交时点击 **Create Commit Message** 图标（而非直接写 `-m`）
5. 按模板填写：type → scope → subject → body

---

## 二、本项目分支结构

```
main          ← 正式发布（当前 v1.0.0）
  └── develop ← 日常开发主线
        ├── feature-backend   （已完成，已合并）
        └── feature-frontend  （已完成，已合并）
```

---

## 三、本地一次性配置

在项目根目录执行：

```bash
chmod +x scripts/setup-git.sh .githooks/commit-msg
./scripts/setup-git.sh
```

将自动配置：
- 提交模板 `.gitmessage`
- 提交校验钩子 `.githooks/commit-msg`

> 之后请使用 `git commit`（不加 `-m`）或 IDEA 的 Git Commit Template 插件填写提交信息。

---

## 四、禁止事项

- ❌ 直接在 `main` 上开发
- ❌ 提交信息写成 `update`、`fix bug`、`修改` 等无意义描述
- ❌ 使用 `git commit -m "xxx"` 跳过规范（本地钩子配置后会被拒绝）
- ❌ 功能分支合并时使用快进合并（不加 `--no-ff`）
