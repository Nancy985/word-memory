# Git 分支管理与提交规范

> 完整项目说明请阅读 **[项目文档.md](./项目文档.md)** 第 8、9 节。  
> 本文档仅保留 Git 相关内容的项目速查。

## 本项目分支

| 分支 | 用途 |
|------|------|
| main | 稳定可交付版本 |
| develop | 日常开发（当前在此分支） |

功能开发：`develop` → `feature-功能名` → 合并回 `develop`

## 本项目提交格式

```
类型(模块): 做了什么
```

**模块对照：** auth / wordbook / memorize / quiz / wrong / exam / frontend / backend

**示例：**
```
feat(quiz): 添加自测倒计时功能
fix(memorize): 修复复习单词顺序错乱
docs: 更新项目文档
```

## 参考文章

- [Git 分支管理策略](https://www.ruanyifeng.com/blog/2012/07/git.html)
- [Commit Message 规范](https://blog.csdn.net/hzf0701/article/details/134367234)
- [IDEA Git Commit Template](https://blog.csdn.net/qq_20042935/article/details/108167597)
