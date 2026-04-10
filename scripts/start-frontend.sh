#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FRONTEND_DIR="${SCRIPT_DIR}/../frontend"
FRONTEND_PORT=5174

if command -v lsof >/dev/null 2>&1; then
  PORT_PIDS="$(lsof -ti tcp:${FRONTEND_PORT} 2>/dev/null || true)"
  if [ -n "${PORT_PIDS}" ]; then
    echo "Port ${FRONTEND_PORT} is occupied. Stopping: ${PORT_PIDS}"
    kill -9 ${PORT_PIDS}
    echo "Port ${FRONTEND_PORT} has been released."
  else
    echo "Port ${FRONTEND_PORT} is available."
  fi
fi

cd "${FRONTEND_DIR}"

if [ ! -d node_modules ]; then
  echo "首次启动，正在执行 npm install ..."
  npm install
fi

export VITE_API_BASE="http://localhost:8081"
echo "当前前端接口地址：${VITE_API_BASE}"
npm run dev -- --host 0.0.0.0
