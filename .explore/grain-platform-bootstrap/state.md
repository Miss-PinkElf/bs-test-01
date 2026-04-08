# 当前状态

## 当前阶段
- Apply second round completed / Pause-ready

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
- 已完成第二轮首页口径改造：
  - 后端 `dashboard` 聚合已切到 `grain_temp_summary.warning_*` 与 `prediction_result.warning_*`
  - 前端 `DashboardView.vue` 已优先展示真实预警、预测预警、最新粮温汇总与仓库风险
  - `BigScreenView.vue` 已同步新首页统计口径，避免继续消费旧字段
- 已完成第三轮固定模板升级：
  - 粮温模板下载已切到固定 `XLSX` 模板
  - 后端 `GrainTempImportService` 已支持固定模板的“基础信息 + 层号/点位矩阵”解析
  - 旧 CSV / 行式 Excel 兼容逻辑保留，避免已跑通 MVP 回退
  - `DataView.vue` 已更新固定模板导入说明
- 已补回归验证清单：`zzz-docs/验证/数据库优先MVP-回归验证清单.md`
- 已完成验证：
  - `frontend/` 已通过 `npm run build`
  - `backend/` 已通过 `mvn -q -DskipTests compile`
  - 后端此前已成功启动并通过本地烟雾测试命中粮温汇总接口与预测接口
  - 烟雾测试已生成新预测归档任务：`prediction_task.id = 5`

## 工作假设
- 以毕业设计 MVP 为目标，先做可演示的软件平台，不接入真实硬件。
- 预测功能继续采用简单回归或移动平均，重点在数据库可追溯与归档，而不是算法复杂度。
- 项目结构采用前后端分离：`backend/` + `frontend/`。
- 启动与联调默认使用 `8081`，避免与本地旧进程占用的 `8080` 冲突。
- 后续更像是“以现有项目为基础的半重写”，并以当前新主线作为唯一开发依据。

## 待解决的问题
- 固定 XLSX 模板已实现，但运行态 smoke 目前被本地 MySQL 认证失败阻塞。
- 首页虽然已切到预警优先口径，但接口级 smoke case 与自动化回归脚本仍需补齐。
- 预测修正字段已保留，但本期仍未实现修正入口与修正页，这与当前范围收口一致。
- 旧版 PRD、数据库定稿、旧接口设计已归档；历史 handoff/checkpoint 中仍保留旧文件名，属于历史上下文，不应作为当前真相源。

## 下一步
- 先恢复本地 MySQL 可用凭据，再按 `zzz-docs/验证/数据库优先MVP-回归验证清单.md` 补首页概览、模板下载、固定 XLSX 导入与旧 CSV 兼容的运行态 smoke。
- 视需要补一个 PowerShell 脚本，串起固定模板下载、导入和首页概览检查。
- 继续收口仍带旧口径的辅助文档，避免后续继续冲突。

## 最新 handoff
- `.explore/grain-platform-bootstrap/handoffs/2026-04-08-015-dashboard-and-fixed-template-pause-ready.md`

## 最小活跃上下文摘要
- 当前已完成数据库优先 MVP 第一轮落地、首页仪表盘预警优先切换和固定 XLSX 粮温模板升级；下一次应先恢复 MySQL 认证可用性，再补运行态 smoke 与必要的脚本化验收。


