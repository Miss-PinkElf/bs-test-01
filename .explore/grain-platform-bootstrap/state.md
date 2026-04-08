# 当前状态

## 当前阶段
- Apply data CRUD completed / Pause-ready

## 已确认的事实
- 用户要求使用 `context-budget-explore` 记录过程。
- 当前仓库已是 Git 仓库，当前分支为 `shuowang/dev2.0`。
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
- 已确认当前本机 MySQL `root` 密码已恢复为 `123456`，与 `backend/src/main/resources/application.yml` 保持一致。
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
- 已补数据主线 CRUD 实施计划：`docs/superpowers/plans/2026-04-09-data-crud-and-temperature-recompute.md`
- 已完成验证：
  - `frontend/` 已通过 `npm run build`
  - `backend/` 已通过 `mvn -q -DskipTests compile`
  - 后端已按默认 datasource 配置在 `8081` 成功启动
  - `/api/dashboard/overview` 运行态 smoke 已通过，返回真实预警、预测预警和首页聚合字段
  - `/api/grain-temp/import/template` 下载 smoke 已通过，固定模板 `XLSX` 可正常生成
  - 固定模板导入 smoke 已通过，新增批次 `BATCH-GRAIN-47204552`，并写入 `grain_temp_summary.id = 28`
  - 旧 CSV 兼容 smoke 已通过，新增批次 `BATCH-GRAIN-FDEFEE5A`，并写入 `grain_temp_summary.id = 29`
  - 旧行式 `.xls` 兼容 smoke 已通过，新增批次 `BATCH-GRAIN-F9CEB1BF`，并写入 `grain_temp_summary.id = 30`
  - 首页联动 smoke 已通过；再次请求 `/api/dashboard/overview` 后，`grainSummaryCount` 已更新为 `30`，`latestGrainSummaries` 与 `latestAlerts` 已反映最新导入
  - 预测归档只读 smoke 已通过：`/api/predictions/tasks` 与 `/api/predictions/tasks/1` 均可返回历史任务与结果列表
- 已完成数据管理 CRUD 第二轮：
  - 后端已新增 `grain_temp_record` 的新增 / 编辑 / 删除接口
  - 后端已新增 `sensor_data` 的编辑 / 删除接口
  - 温度原始记录变更后，已联动重算同仓库同采集时间的 `grain_temp_summary` 与真实预警
  - `frontend/src/views/DataView.vue` 已升级为数据维护页：粮温模式支持原始测点记录 CRUD，普通环境模式支持湿度 / 二氧化碳 CRUD
  - 已完成静态验证：`backend/` 再次编译通过，`frontend/` 再次构建通过
  - 已完成运行态 smoke：温度记录 create / update / delete 能联动首页真实预警，环境数据 create / update / delete 能联动列表查询

## 工作假设
- 以毕业设计 MVP 为目标，先做可演示的软件平台，不接入真实硬件。
- 预测功能继续采用简单回归或移动平均，重点在数据库可追溯与归档，而不是算法复杂度。
- 项目结构采用前后端分离：`backend/` + `frontend/`。
- 启动与联调默认使用 `8081`，避免与本地旧进程占用的 `8080` 冲突。
- 后续更像是“以现有项目为基础的半重写”，并以当前新主线作为唯一开发依据。

## 待解决的问题
- 当前 MySQL 凭据与仓库默认配置已一致，但后端仍配置 `spring.sql.init.mode=always`，每次启动都会重建并回填演示库。
- 首页与导入链路的运行态 smoke 已补齐，但自动化 PowerShell 验收脚本仍未落地。
- 用户 / 仓库管理仍未补完整 CRUD，本轮只完成了数据主线 CRUD。
- 预测修正字段已保留，但本期仍未实现修正入口与修正页，这与当前范围收口一致。
- 旧版 PRD、数据库定稿、旧接口设计已归档；历史 handoff/checkpoint 中仍保留旧文件名，属于历史上下文，不应作为当前真相源。

## 下一步
- 评估是否保留 `spring.sql.init.mode=always`；若后续要做稳定联调或保留导入结果，需要把初始化策略改为更可控的方式。
- 若继续补后台管理闭环，下一优先级应是仓库 CRUD，再是用户 CRUD。
- 补一个 PowerShell 验收脚本，串起后端启动、固定模板下载、固定模板导入、旧 CSV / 旧行式 Excel 导入和首页概览检查。
- 继续收口仍带旧口径的辅助文档，避免后续继续冲突。

## 当前参考计划
- `docs/superpowers/plans/2026-04-09-data-crud-and-temperature-recompute.md`

## 最新 handoff
- `.explore/grain-platform-bootstrap/handoffs/2026-04-09-016-data-crud-and-handoff-ready.md`

## 最小活跃上下文摘要
- 当前已解除运行态阻塞：本机 MySQL `root` 密码已恢复为 `123456`，后端可按默认配置直接启动；数据库优先 MVP 的首页概览、固定模板下载、固定模板导入、旧 CSV 和旧行式 `.xls` 兼容 smoke 已全部通过。当前还额外完成了数据主线 CRUD：`grain_temp_record` 和 `sensor_data` 已支持页面维护，且温度记录变更会联动重算汇总与真实预警。下一步更值得收口的是初始化策略、后台管理 CRUD 与 PowerShell 验收脚本。


