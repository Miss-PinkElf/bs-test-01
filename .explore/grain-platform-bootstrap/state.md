# 当前状态

## 当前阶段
- Handoff / Ready for resume

## 已确认的事实
- 用户要求使用 `context-budget-explore` 记录过程。
- 当前仓库已是 Git 仓库，当前分支为 `main`。
- 当前环境已确认可用：`java`、`node`、`npm`、`mvn`。
- 开题报告已转为 Markdown，核心课题是“基于 Spring Boot 的粮仓环境数据预测管理平台设计与实现”。
- 用户希望同时获得方案设计与项目骨架。
- 已将 `frontend-next/` 升级为高保真静态原型，补齐了仪表盘、用户管理、仓库管理、环境数据、温度预测和展示大屏。
- `frontend-next/README.md` 已补充，用于说明这套原型如何映射最终答辩成品预期。
- 已修复 `frontend-next/` 本地 `Next SWC` 依赖损坏问题，并重新通过 `npm run build` 验证。
- 已完成数据库定稿，新增 `zzz-docs/设计文档/数据库设计定稿.md`，并将 `backend/src/main/resources/db/schema.sql` 升级为正式版本。
- 预测归档结构已确认采用 `prediction_task + prediction_result`，不再使用旧的单表 `prediction_record` 方案。
- 已完成 `zzz-docs/设计文档/后端接口与DTO-VO-Mapper设计.md`，正式明确接口清单、DTO/VO 分包与 Mapper 职责。
- 已完成 `frontend/` 正式骨架升级，接入 `Vue Router + Pinia + Element Plus + Axios`，补齐了登录、仪表盘、用户、仓库、环境数据、预测和展示大屏入口。
- 已在 `frontend/` 执行 `npm install` 与 `npm run build`，构建通过。
- 已完成后端主链路从 `DemoDataService` 到真实 `MyBatis + MySQL` 的替换，并在 `8081` 下完成最小验证。
- 已将 Windows 启动脚本统一收口到 `8081` 联调口径，并新增 mac 版本启动脚本。

## 工作假设
- 以毕业设计 MVP 为目标，先做可演示的软件平台，不接入真实硬件。
- 预测功能采用简单回归或移动平均，符合开题报告范围。
- 项目结构采用前后端分离：`backend/` + `frontend/`。
- 启动与联调默认使用 `8081`，避免与本地旧进程占用的 `8080` 冲突。

## 待解决的问题
- 用户管理、角色选项、指标选项、预测历史等正式接口仍未接入真实持久层。
- `frontend/` 当前只做了部分 API 收口，尚未完整联调到真实后端。
- 前端源码默认回落地址仍是 `http://localhost:8080`，当前主要依赖启动脚本注入 `VITE_API_BASE=http://localhost:8081`。
- 仪表盘目前已接最小真实统计，但健康度、最近采样、更多聚合仍是后续增强项。
- 新增脚本尚未完整跑一轮端到端启动验证。

## 下一步
- 先验证 Windows 与 mac 两套启动脚本是否都能顺利拉起前后端。
- 确认前端请求是否成功指向 `http://localhost:8081`。
- 继续把 `frontend/` 登录、仓库、环境数据、预测页面逐步切到真实 API。
- 再补用户管理、角色选项、指标选项、预测历史等正式接口与前端联调。
- 联调稳定后再处理仪表盘增强、端口收口和演示流程优化。

## 最新 handoff
- `.explore/grain-platform-bootstrap/handoffs/2026-04-06-005-startup-scripts-and-resume-ready.md`

## 最小活跃上下文摘要
- 已完成后端主链路从 `DemoDataService` 到真实 `MyBatis + MySQL` 的替换，数据库切到本地 `grain_env_predict`（root / 空密码），并已在 `8081` 端口实测通过登录、仓库列表、环境数据写入、预测归档四条主链路；本轮又把 Windows 启动脚本统一到 `8081` 并补齐了 mac 启动脚本，下一步应优先验证启动脚本与 `frontend/` 的真实 API 联调。
