#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="${SCRIPT_DIR}/../backend"

cd "${BACKEND_DIR}"

echo "准备启动后端，端口固定为 8081..."
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
