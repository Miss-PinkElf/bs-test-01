#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FRONTEND_DIR="${SCRIPT_DIR}/../frontend"

cd "${FRONTEND_DIR}"

if [ ! -d node_modules ]; then
  echo "首次启动，正在执行 npm install ..."
  npm install
fi

export VITE_API_BASE="http://localhost:8081"
echo "当前前端接口地址：${VITE_API_BASE}"
npm run dev -- --host 0.0.0.0
