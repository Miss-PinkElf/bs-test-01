# 当前状态

## 当前阶段
- Apply first round completed for DB-first MVP; Pause-ready for next iteration

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
- 已完成当前主线版本的 PRD 与数据库设计重写。
- 已将旧版开发指导版 PRD、旧版 PRD、旧版数据库定稿和旧版接口设计归档到 `zzz-docs/Archive/`。
- 已确认当前本地 MySQL 可用凭据为 `root/123456`，并已用 `utf8mb4` 成功执行新 `schema.sql`。
- 已完成第一轮数据库优先 MVP 改造：
  - 后端预测链已切到 `grain_temp_summary + prediction_task + prediction_result`
  - 新增粮温导入/汇总查询接口：`/api/grain-temp/import`、`/api/grain-temp/summaries`
  - 前端 `PredictionView.vue`、`DataView.vue`、`frontend/src/api/grain.js` 已切到新主线交互
- 已完成验证：
  - `frontend/` 已通过 `npm run build`
  - 后端已成功启动并通过本地烟雾测试命中粮温汇总接口与预测接口
  - 烟雾测试已生成新预测归档任务：`prediction_task.id = 5`

## 工作假设
- 以毕业设计 MVP 为目标，先做可演示的软件平台，不接入真实硬件。
- 预测功能继续采用简单回归或移动平均，重点在数据库可追溯与归档，而不是算法复杂度。
- 项目结构采用前后端分离：`backend/` + `frontend/`。
- 启动与联调默认使用 `8081`，避免与本地旧进程占用的 `8080` 冲突。
- 后续更像是“以现有项目为基础的半重写”，并以当前新主线作为唯一开发依据。

## 待解决的问题
- 粮温导入当前为数据库优先 MVP 的“行式模板”实现，尚未升级为老师更偏好的复杂矩阵式 XLS 模板解析。
- 仪表盘首页的预警与统计仍主要沿旧查询口径，尚未完全切到“真实高温预警 + 预测高温预警”的新主线展示。
- 预测修正字段已保留，但本期仍未实现修正入口与修正页，这与当前范围收口一致。
- 旧版 PRD、数据库定稿、旧接口设计已归档；历史 handoff/checkpoint 中仍保留旧文件名，属于历史上下文，不应作为当前真相源。

## 下一步
- 补仪表盘首页，让预警与统计优先展示：
  - `grain_temp_summary.warning_*`
  - `prediction_result.warning_*`
- 继续完善粮温导入模板，从当前 MVP 行式模板升级到更贴近老师预期的固定 XLS 模板。
- 增补后端/前端的回归验证清单，并视需要补充测试或脚本化验收。
- 继续收口仍带旧口径的辅助文档，避免后续继续冲突。

## 最新 handoff
- `.explore/grain-platform-bootstrap/handoffs/2026-04-08-014-db-first-mvp-apply-round1-pause-ready.md`

## 最小活跃上下文摘要
- 当前已完成数据库优先 MVP 的第一轮落地：新 schema 已导入验证，后端与前端主链已切到粮温导入/汇总/按天预测/双线图/预测归档口径；下一次应优先补首页口径与固定 XLS 模板细化。
