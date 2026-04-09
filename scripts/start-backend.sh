#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="${SCRIPT_DIR}/../backend"

clear_port() {
  local port="$1"
  local pids
  pids="$(lsof -ti tcp:"${port}" 2>/dev/null || true)"

  if [ -z "${pids}" ]; then
    echo "端口 ${port} 当前空闲。"
    return
  fi

  for pid in ${pids}; do
    echo "检测到端口 ${port} 被进程占用，正在关闭 PID=${pid} ..."
    kill -9 "${pid}"
  done

  echo "端口 ${port} 已释放。"
}

clear_port 8081

cd "${BACKEND_DIR}"

echo "默认启动当前不会自动重建演示库。"
echo "如果你需要重置演示数据，请先执行 ./scripts/reset-demo-db.ps1"
echo "准备启动后端，端口固定为 8081..."
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
