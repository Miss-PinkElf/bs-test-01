# Plan

## 背景

- `npm run dev` 在 Windows 上通过 `node ./scripts/dev-inline.cjs` 并行拉起 `start-backend.ps1` 与 `start-frontend.ps1`。
- 用户反馈后端启动立即报 PowerShell 解析错误：`字符串缺少终止符: "`，定位点落在 `scripts/start-backend.ps1:70`。
- 仓库内多个 npm 入口直接硬编码为 `powershell -NoProfile -ExecutionPolicy Bypass -File ...`，存在宿主差异风险。

## 目标

- 消除 `start-backend.ps1` 在 Windows 启动链路中的字符串终止符解析错误。
- 让 `npm run dev`、`npm run backend`、`npm run frontend` 等入口共享同一套 PowerShell 运行选择逻辑。
- 保留当前脚本职责边界，不顺带改动后端业务或前端功能逻辑。

## 方案比较

### 方案 A：仅把 `start-backend.ps1` 改成 BOM 或重写个别中文提示
- 优点：改动最小。
- 缺点：只能止住当前脚本；`check-env.ps1`、`start-frontend.ps1`、npm 入口与 `dev-inline.cjs` 仍然依赖不同宿主行为，后续容易在别的脚本上复发。

### 方案 B：增加统一 PowerShell 运行层，并顺手清理直接被 npm 拉起的脚本文本
- 优点：把问题收口到“入口如何选择并启动 PowerShell”；`dev` 与单脚本入口行为一致，后续维护成本更低。
- 缺点：改动文件数比方案 A 多，需要补一轮回归验证。

## 放弃方案

- 放弃方案 A，因为它只修当前一处报错，不能解决 npm 入口直接绑定 `powershell` 带来的宿主差异。

## 最终选择

- 采用方案 B：新增 `scripts/powershell-runtime.cjs` 与 `scripts/run-powershell-script.cjs`，让 Node 入口统一走共享运行层。
- `scripts/dev-inline.cjs` 改为复用共享参数构造逻辑。
- `package.json` 中 `backend` / `frontend` / `check-env` / `reset-demo-db` 改为 Node 代理入口。
- 把会被 npm 直接拉起的 PowerShell 脚本提示文本改为 ASCII，避免再被 Windows PowerShell 5.1 的 UTF-8 无 BOM 解析坑触发。

## 产出指向

- 代码：`package.json`、`scripts/dev-inline.cjs`、`scripts/powershell-runtime.cjs`、`scripts/run-powershell-script.cjs`、`scripts/start-backend.ps1`、`scripts/start-frontend.ps1`、`scripts/check-env.ps1`
- 记录：`.devflow/grain-platform-bootstrap/bug-log.md`、`decision-log.md`、`state.md`、`checkpoints.md`
- 验证：`powershell -NoProfile -ExecutionPolicy Bypass -File scripts/start-backend.ps1`、`npm run backend`
