#!/bin/bash
# 一次性配置 Git 提交模板与校验钩子（在项目根目录执行）

set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

chmod +x .githooks/commit-msg

git config core.hooksPath .githooks
git config commit.template .gitmessage

echo "✅ Git 规范配置完成："
echo "   - 提交模板: .gitmessage"
echo "   - 校验钩子: .githooks/commit-msg"
echo ""
echo "请使用 'git commit'（不加 -m）或 IDEA Git Commit Template 插件提交代码。"
