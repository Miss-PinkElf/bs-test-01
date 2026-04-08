# 当前状态

## 当前阶段
- Align completed for DB-first MVP pivot (Route 1)

## 已确认的事实
- 用户要求使用 `context-budget-explore` 记录过程。
- 当前仓库已是 Git 仓库，当前分支为 `main`。
- 当前环境已确认可用：`java`、`node`、`npm`、`mvn`、`mysql`。
- 开题报告已转为 Markdown，核心课题是“基于 Spring Boot 的粮仓环境数据预测管理平台设计与实现”。
- 用户已明确“又一次主线变更”：本期不再把“预测->修正->再预测”作为必做主链。
- 当前本期主线改为：粮温/环境数据导入 -> 数据保存管理 -> 汇总与预测 -> 结果归档 -> 双线图展示 -> 高温预警。
- 修正链路在本期降级为扩展功能：字段可保留，但不做接口与页面入口。
- 已确认本期不拆独立归档表，预测归档继续由 `prediction_task + prediction_result` 承担。
- 已确认湿度与二氧化碳保留简单单值模型，支持文件导入与手工录入两种入口。
- 已确认前端预测页支持“可选预测天数”，图表展示“实际值 + 预测值”两条线。
- 当前正式前端路线仍是 `frontend/`，`frontend-next/` 仅作静态原型参考。
- `backend/src/main/resources/db/schema.sql` 已是新结构，但真实导库验证仍受 MySQL 凭据阻塞。

## 工作假设
- 以毕业设计 MVP 为目标，先做可演示的软件平台，不接入真实硬件。
- 预测功能继续采用简单回归或移动平均，重点在数据库可追溯与归档，而不是算法复杂度。
- 项目结构采用前后端分离：`backend/` + `frontend/`。
- 启动与联调默认使用 `8081`，避免与本地旧进程占用的 `8080` 冲突。
- 后续更像是“以现有项目为基础的半重写”，并以当前新主线作为唯一开发依据。

## 待解决的问题
- 当前 MySQL 凭据与文档中的 `root/123456` 不一致，导致新 `schema.sql` 不能直接导入验证。
- 后端 Java 实体、Mapper、Service、Controller 仍围绕旧预测口径，未切到数据库优先 MVP 口径。
- 前端 `PredictionView.vue`、`DataView.vue`、`frontend/src/api/grain.js` 仍是旧交互与旧接口结构。
- 多份旧文档仍强调修正链，需以最新 handoff 与本状态文件作为恢复优先真相源。

## 下一步
- 先确认可用的 MySQL 账号密码，实际执行新的 `schema.sql`。
- 再按“本期不做修正链入口”的口径改后端实体、Mapper、Service、Controller。
- 同步改前端预测页（可选天数 + 实际/预测双线图）和导入页/API 适配层。
- 最后回写真相源文档中的主线描述，避免后续继续冲突。

## 最新 handoff
- `.explore/grain-platform-bootstrap/handoffs/2026-04-08-013-pause-ready-db-first-mvp.md`

## 最小活跃上下文摘要
- 当前已完成“主线二次变更”对齐：本期数据库优先、修正链降级为扩展、不拆独立归档表；下一次应先完成导库验证，再按新口径改后端和前端。
