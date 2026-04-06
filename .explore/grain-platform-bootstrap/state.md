# 当前状态

## 当前阶段
- Handoff / Ready for resume

## 已确认的事实
- 用户要求使用 `context-budget-explore` 记录过程。
- 当前仓库已是 Git 仓库，当前分支为 `main`。
- 本地已检测到 `java`、`node`、`npm`，未检测到 `mvn`。
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

## 工作假设
- 以毕业设计 MVP 为目标，先做可演示的软件平台，不接入真实硬件。
- 预测功能采用简单回归或移动平均，符合开题报告范围。
- 项目结构采用前后端分离：`backend/` + `frontend/`。

## 待解决的问题
- Maven 未安装时，一键启动脚本只能优先使用 `mvnw.cmd` 或提示用户使用 IDEA 内置 Maven。
- 正式实现路线已转向 Vue 技术栈，`frontend-next/` 现在只作为静态原型和答辩成品预期参考，而不是最终正式前端。
- 后端主链路仍主要依赖 `DemoDataService`，尚未按数据库定稿接入真实 MyBatis 持久层。
- 用户管理、角色、指标选项、预测历史等正式接口虽然已设计，但后端尚未落真实实现。

## 下一步
- 基于数据库定稿和接口设计，开始替换后端 `DemoDataService`，优先打通登录、仓库、环境数据、预测归档的真实持久层。
- 再把用户管理、角色选项、指标选项、预测历史等正式接口补齐，随后进入前后端联调。

## 最新 handoff
- `.explore/grain-platform-bootstrap/handoffs/2026-04-06-003-database-api-vue-resume-ready.md`

## 最小活跃上下文摘要
- 已完成项目骨架、文档基础、高保真静态原型、数据库 SQL 定稿、后端接口设计，以及正式版 Vue 前端骨架升级并通过构建；下一步应把后端主链路从 demo 数据服务替换为真实 MyBatis 持久层。
