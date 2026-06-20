# 词忆 · 单词记忆网站

Spring Boot 单词记忆 Web 应用，端口 **3500**。  
**完整项目文档见 [docs/项目文档.md](docs/项目文档.md)**

## 技术栈

- Spring Boot 3.2
- Spring Data JPA
- H2 内存数据库
- BCrypt 密码加密

## 快速启动

```bash
cd ~/Projects/word-memory-backend
mvn spring-boot:run
```

服务地址：`http://localhost:3500`

浏览器打开上述地址即可使用前端页面（绿色系 UI，无需额外安装前端依赖）。

H2 控制台：`http://localhost:3500/h2-console`（JDBC URL: `jdbc:h2:mem:wordmemory`）

## 演示账号

| 用户名 | 密码 |
|--------|------|
| demo   | password123 |

## 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1718822400000
}
```

## API 概览

除登录/注册外，请求头需携带：`Authorization: Bearer <token>`

### 用户认证

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 注册 |
| POST | `/api/auth/login` | 登录 |
| POST | `/api/auth/logout` | 退出 |
| GET  | `/api/auth/me` | 当前用户信息 |

### 单词书管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET    | `/api/wordbooks` | 单词书列表 |
| GET    | `/api/wordbooks/{id}` | 单词书详情 |
| POST   | `/api/wordbooks` | 创建单词书 |
| PUT    | `/api/wordbooks/{id}` | 更新单词书 |
| DELETE | `/api/wordbooks/{id}` | 删除单词书 |
| GET    | `/api/wordbooks/{id}/words` | 单词列表 |
| POST   | `/api/wordbooks/{id}/words` | 添加单词 |
| PUT    | `/api/wordbooks/{bookId}/words/{wordId}` | 更新单词 |
| DELETE | `/api/wordbooks/{bookId}/words/{wordId}` | 删除单词 |

### 背单词（艾宾浩斯间隔复习）

复习间隔：5 分钟 → 30 分钟 → 12 小时 → 1 天 → 2 天 → 4 天 → 7 天 → 15 天 → 30 天

| 方法 | 路径 | 说明 |
|------|------|------|
| GET  | `/api/memorize/today?wordBookId=1&limit=20` | 今日待复习单词 |
| POST | `/api/memorize/review` | 提交复习结果 `{ "wordId": 1, "correct": true }` |
| GET  | `/api/memorize/progress?wordBookId=1` | 学习进度 |

### 单词自测（四选一）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/quiz/generate` | 生成测验 `{ "wordBookId": 1, "count": 10 }` |
| POST | `/api/quiz/submit` | 提交答案并记录考试 |

### 错词统计

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/wrong-words` | 错词列表（可选 `wordBookId`） |
| GET | `/api/wrong-words/stats` | 错词统计 |

### 考试记录

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/exams` | 考试记录列表（可选 `wordBookId`、`examType`） |
| GET | `/api/exams/{id}` | 考试详情（含答题明细） |

## 跨域

已配置允许所有来源（`allowedOriginPatterns: *`）。

## 文档

| 文档 | 说明 |
|------|------|
| [docs/项目文档.md](docs/项目文档.md) | 项目完整说明（功能、架构、数据库、接口、Git 规范） |
| [docs/交付与答辩清单.md](docs/交付与答辩清单.md) | **6月21日答辩交付清单（按课程要求逐项对照）** |
