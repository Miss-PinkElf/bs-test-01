# 当前状态

## 当前阶段
- Handoff / Ready for resume

## 已确认的事实
- 用户要求使用 `context-budget-explore` 记录过程。
- 当前仓库已是 Git 仓库，当前分支为 `main`。
- 当前环境已确认可用：`java`、`node`、`npm`、`mvn`、`mysql`。
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
- 已为后端核心 Controller 增加简短接口日志与简单注释，相关提交为 `f97f08c`。
- 本轮已完成环境数据导入、CSV 模板、mock 数据补充、8081 收口与启动脚本端口清理修复。
- 本轮已完成两个新提交：`1cf835e`、`1a30b6e`。
- 当前本地 MySQL `root` 密码以 `123456` 为准。

## 工作假设
- 以毕业设计 MVP 为目标，先做可演示的软件平台，不接入真实硬件。
- 预测功能采用简单回归或移动平均，符合开题报告范围。
- 项目结构采用前后端分离：`backend/` + `frontend/`。
- 启动与联调默认使用 `8081`，避免与本地旧进程占用的 `8080` 冲突。
- 当前先通过 Controller 层日志提高联调可观测性，后续再决定是否需要统一日志切面。

## 待解决的问题
- 用户管理、角色选项、指标选项、预测历史等正式接口仍未接入真实持久层。
- `frontend/` 当前仍有部分页面与组件处于“真实接口 + mock 展示”混合状态。
- 仪表盘目前只接了最小真实统计，健康度、最近采样、更多聚合仍是后续增强项。
- Windows / mac 启动脚本尚未完整跑一轮端到端启动验证。
- mac 侧 `8081` 端口清理逻辑尚未实机验证。
- `start-backend.ps1` 当前提示文案为英文，为规避编码问题；如要恢复中文需再补测编码兼容性。

## 下一步
- 先刷新并验证前端页面是否已成功读取新导入的数据库 mock 数据。
- 再验证 Windows 与 mac 两套启动脚本是否都能顺利拉起前后端。
- 继续把 `frontend/` 登录、仓库、环境数据、预测页面逐步切到真实 API。
- 再补用户管理、角色选项、指标选项、预测历史等正式接口与前端联调。
- 联调稳定后再处理仪表盘增强、端口收口和演示流程优化。

## 最新 handoff
- `.explore/grain-platform-bootstrap/handoffs/2026-04-07-007-import-mock-data-and-startup-fixes-resume-ready.md`

## 最小活跃上下文摘要
- 已完成环境数据导入、CSV 模板、mock 数据补充、前端录入弹窗化、8081 收口和启动脚本端口清理修复；数据库已导入 mock 数据（warehouse=6、sensor_data=36、prediction_task=4），Windows `start-backend.ps1` 已实测可释放 8081 后成功启动。下一步应优先验证前端页面是否正常读取这些数据，再继续剩余页面和接口联调。
