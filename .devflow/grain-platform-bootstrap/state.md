# 当前状态

## 当前阶段
- Pause-ready after user CRUD and acceptance smoke script

## 已确认的事实
- 用户要求使用 `devflow` 记录过程。
- 已完成从 `.explore/grain-platform-bootstrap/` 到 `.devflow/grain-platform-bootstrap/` 的复制迁移；原 `.explore/` 工作区保留为历史快照，不再作为当前主真相源。
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
- 已确认当前默认初始化策略仍为：
  - 历史问题已完成收口，不再作为当前默认行为
- 已完成初始化策略收口：
  - `backend/src/main/resources/application.yml` 已改为 `spring.sql.init.mode=never`
  - 已新增显式重置入口：`scripts/reset-demo-db.ps1`
  - 当前默认启动不会自动重建演示库；需要重置时手工执行脚本
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
- 已完成后台页列表展示统一第一轮：
  - 新增前端组合式工具：`frontend/src/composables/useClientPagination.js`
  - 新增前端组合式工具：`frontend/src/composables/useIncrementalList.js`
  - `UsersView.vue`、`WarehouseView.vue`、`DataView.vue`、`PredictionView.vue` 已统一补上 `Element Plus` 表格分页
  - `PredictionView.vue` 的历史归档记录已由卡片流改为表格分页
  - `DashboardView.vue` 的仓库健康度已改为表格分页
  - `DashboardView.vue` 的近期预警已改为 `el-scrollbar` + 下滑增量加载
  - 前端构建验证已再次通过：`frontend/` 执行 `npm run build` 成功
- 已完成数据管理页后端分页适配：
  - `GET /api/grain-temp/records` 已切到分页返回：`PageResult<GrainTempRecordItemDto>`
  - `GET /api/sensor-data` 已切到分页返回：`PageResult<SensorDataPointDto>`
  - `DataView.vue` 的粮温原始记录表与环境数据表已改为后端分页联动
  - 环境趋势图已切到 `/api/sensor-data/trend`，避免只渲染当前页数据
  - 静态验证已通过：`backend/` 执行 `mvn -q -DskipTests compile` 成功，`frontend/` 执行 `npm run build` 成功
- 已修复前端列表统一后的两类运行时 bug：
  - `DashboardView.vue` 的近期预警渲染已对空预警项做 API 层和页面层双重兜底，避免 `undefined.sourceType`
  - 分页 / 增量加载 composable 已改为 `proxyRefs` 自动解包，避免 Element Plus 表格收到非数组包装值而触发 `data2 is not iterable`
- 已确认后台管理当前实现边界：
  - `WarehouseController` / `WarehouseView.vue` 已补齐列表、选项、新增、编辑、删除
  - `UserController` / `UsersView.vue` 当前仍只覆盖用户列表、角色选项与角色说明展示
- 已完成新的初始化策略验证：
  - `backend/` 执行 `mvn -q -DskipTests compile` 成功
  - `.\scripts\reset-demo-db.ps1` 执行成功
  - 后端在 `8081` 可正常启动，并成功返回 `/api/dashboard/overview`
  - 写入临时仓库后连续启动两次，`/api/warehouses` 两次都保留该仓库，验证结束后已清理临时数据
- 已完成仓库管理 CRUD：
  - 后端已新增 `PUT /api/warehouses/{id}` 与 `DELETE /api/warehouses/{id}`
  - 仓库删除若被用户、环境数据或粮温数据引用，会返回明确业务提示
  - 前端 `WarehouseView.vue` 已支持编辑、删除、删除确认和行点击详情联动
  - 已完成静态验证：`backend/` 编译通过，`frontend/` 构建通过
  - 已完成运行态 smoke：仓库 create / update / delete API 可正常工作，临时验证数据已清理
- 已完成用户管理 CRUD：
  - 后端已新增 `POST /api/users`、`PUT /api/users/{id}`、`PUT /api/users/{id}/password`、`DELETE /api/users/{id}`
  - 后端已补齐多角色维护、所属仓库维护、密码重置、最后一个启用管理员保护与引用删除拦截
  - 前端 `UsersView.vue` 已支持新增 / 编辑 / 删除 / 重置密码弹窗交互
  - 前端 `frontend/src/api/grain.js` 已补齐用户 CRUD API 封装，用户列表已补手机号与 `warehouseId`
  - 已完成静态验证：`backend/` 执行 `mvn -q -DskipTests compile` 成功，`frontend/` 执行 `npm run build` 成功
  - 已完成运行态 smoke：create / update / reset password / delete 完整链路通过，临时验证数据已清理
- 已完成 PowerShell 验收脚本：
  - 新增 `scripts/run-acceptance-smoke.ps1`，串起后端编译、前端构建、演示库重置、后端启动、用户 CRUD、固定模板下载/导入、旧 CSV/旧行式 Excel 导入、首页概览与预测只读接口检查
  - 新增计划文档：`docs/superpowers/plans/2026-04-09-acceptance-smoke-script.md`
  - 已更新回归验证清单：`zzz-docs/验证/数据库优先MVP-回归验证清单.md`
  - 已完成脚本运行态验证：`powershell` 执行 `./scripts/run-acceptance-smoke.ps1 -SkipStaticChecks` 全链路通过
  - 已完成单独静态验证：`backend/` 执行 `mvn -q -DskipTests compile` 成功，`frontend/` 执行 `npm run build` 成功

## 工作假设
- 以毕业设计 MVP 为目标，先做可演示的软件平台，不接入真实硬件。
- 预测功能继续采用简单回归或移动平均，重点在数据库可追溯与归档，而不是算法复杂度。
- 项目结构采用前后端分离：`backend/` + `frontend/`。
- 启动与联调默认使用 `8081`，避免与本地旧进程占用的 `8080` 冲突。
- 后续更像是“以现有项目为基础的半重写”，并以当前新主线作为唯一开发依据。

## 待解决的问题
- 用户管理 CRUD 已完成，但 `UsersView.vue` 右侧角色说明卡片区仍未做展示统一。
- 首页与导入链路、后台管理关键链路已补齐一键验收脚本，但当前脚本在本沙箱内直接执行前端构建时仍可能命中 `esbuild spawn EPERM`；仓库内单独执行 `npm run build` 已通过。
- 后台页列表样式已做第一轮统一，但 `UsersView.vue` 右侧角色说明仍保留卡片说明区；`/screen` 大屏仍未纳入本轮统一范围。
- 预测修正字段已保留，但本期仍未实现修正入口与修正页，这与当前范围收口一致。
- 旧版 PRD、数据库定稿、旧接口设计已归档；历史 handoff/checkpoint 中仍保留旧文件名，属于历史上下文，不应作为当前真相源。

## 下一步
- 若继续统一前端展示，优先评估是否把 `UsersView.vue` 的角色说明卡片改成表格，以及是否把 `/screen` 大屏纳入同一规范。
- 继续收口仍带旧口径的辅助文档，避免后续继续冲突。
- 如需在沙箱环境里重复跑脚本，可优先使用 `-SkipStaticChecks`，静态命令单独执行。

## 当前参考计划
- `.devflow/grain-platform-bootstrap/plans/active-plan-links.md`
- `.devflow/grain-platform-bootstrap/plans/2026-04-09-init-strategy-and-admin-crud-sequencing.md`
- `docs/superpowers/plans/2026-04-09-init-strategy-closure.md`
- `docs/superpowers/plans/2026-04-09-warehouse-crud.md`
- `docs/superpowers/plans/2026-04-09-user-crud.md`
- `docs/superpowers/plans/2026-04-09-data-crud-and-temperature-recompute.md`
- `docs/superpowers/plans/2026-04-09-frontend-list-unification.md`

## 最新 handoff
- `.devflow/grain-platform-bootstrap/handoffs/2026-04-09-020-pause-ready-after-user-crud-and-acceptance-smoke.md`

## 最小活跃上下文摘要
- 当前已解除运行态阻塞：本机 MySQL `root` 密码已恢复为 `123456`，数据库优先 MVP 的首页概览、固定模板下载、固定模板导入、旧 CSV 和旧行式 `.xls` 兼容 smoke 已全部通过；同时本轮已完成初始化策略收口，默认启动不再自动清库，显式重置走 `scripts/reset-demo-db.ps1`。当前已完成数据主线 CRUD、后台页第一轮列表统一、数据管理页后端分页适配，以及仓库管理 CRUD；`WarehouseView.vue` 现在支持新增 / 编辑 / 删除，仓库接口已补齐 `PUT/DELETE`。当前过程记录已整体迁移到 `.devflow/grain-platform-bootstrap/`，并新增 `NEXT-SESSION-PROMPT.devflow.md` 作为新的恢复提示词副本。下一步应优先评估后台页展示统一与 `/screen` 是否纳入统一规范，并继续收口辅助文档口径。
- 当前已解除运行态阻塞：本机 MySQL `root` 密码已恢复为 `123456`，数据库优先 MVP 的首页概览、固定模板下载、固定模板导入、旧 CSV 和旧行式 `.xls` 兼容 smoke 已全部通过；同时本轮已完成初始化策略收口，默认启动不再自动清库，显式重置走 `scripts/reset-demo-db.ps1`。当前已完成数据主线 CRUD、后台页第一轮列表统一、数据管理页后端分页适配，以及仓库管理 CRUD；`WarehouseView.vue` 已支持新增 / 编辑 / 删除，仓库接口已补齐 `PUT/DELETE`。本次会话中已完成用户 CRUD 的后端与前端实现，并拿到静态验证与运行态 smoke 证据；同时额外修复了 `scripts/start-backend.ps1` 的 UTF-8 编码问题，确认 `powershell.exe` 与 `pwsh.exe` 都可稳定拉起 `8081`。最新恢复入口仍可参考 `2026-04-09-019-pause-ready-after-init-and-warehouse-crud`，但当前主真相源应以本文件的最新状态为准，下一步直接进入展示统一或文档收口，不需要重复实现用户 CRUD 和验收脚本。


