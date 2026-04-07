#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

chmod +x "${SCRIPT_DIR}/start-backend.sh" "${SCRIPT_DIR}/start-frontend.sh"

echo "准备启动前后端（联调端口固定为 8081）..."
echo "后端启动前会自动检查并清理 8081 端口占用。"
echo "将打开两个新的 Terminal 窗口分别启动前后端。"

osascript <<EOF
 tell application "Terminal"
   do script "cd \"${SCRIPT_DIR}\" && ./start-backend.sh"
   do script "cd \"${SCRIPT_DIR}\" && ./start-frontend.sh"
   activate
 end tell
EOF

echo "如果启动成功，可访问："
echo "前端: http://localhost:5173"
echo "后端: http://localhost:8081"
echo "前端请求将自动指向: http://localhost:8081"
