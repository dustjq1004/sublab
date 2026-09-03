#!/bin/bash

# Git hooks 설치 스크립트
# 이 스크립트는 pre-commit hook을 .git/hooks 디렉토리에 설치합니다.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
HOOKS_DIR="$SCRIPT_DIR/.git/hooks"
PRE_COMMIT_SOURCE="$SCRIPT_DIR/pre-commit"
PRE_COMMIT_TARGET="$HOOKS_DIR/pre-commit"

# .git/hooks 디렉토리 확인
if [ ! -d "$HOOKS_DIR" ]; then
    echo "❌ .git/hooks 디렉토리를 찾을 수 없습니다."
    exit 1
fi

# pre-commit 파일 확인
if [ ! -f "$PRE_COMMIT_SOURCE" ]; then
    echo "❌ pre-commit 파일을 찾을 수 없습니다: $PRE_COMMIT_SOURCE"
    exit 1
fi

# pre-commit hook 설치
cp "$PRE_COMMIT_SOURCE" "$PRE_COMMIT_TARGET"
chmod +x "$PRE_COMMIT_TARGET"

echo "✅ Git pre-commit hook이 설치되었습니다!"
echo "   위치: $PRE_COMMIT_TARGET"
echo ""
echo "이제 git commit 시 자동으로 테스트가 실행됩니다."

