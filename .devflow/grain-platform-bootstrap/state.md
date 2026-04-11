# 当前状态

## 当前阶段
- Pause-ready：handoff **023**（2026-04-11）— GSD / devflow / 论文文档同步已完成；恢复以 023 + `NEXT-SESSION-PROMPT-DEVFLOW.md` 为准

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
- 已补数据主线 CRUD 实施计划：`.devflow/grain-platform-bootstrap/plans/2026-04-09-data-crud-and-temperature-recompute.md`
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
  - `UserController` / `UsersView.vue` 已补齐用户 CRUD、角色说明表与列表侧模糊搜索（列表数据为前端过滤）
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
  - 新增计划文档：`.devflow/grain-platform-bootstrap/plans/2026-04-09-acceptance-smoke-script.md`
  - 已更新回归验证清单：`zzz-docs/验证/数据库优先MVP-回归验证清单.md`
  - 已完成脚本运行态验证：`powershell` 执行 `./scripts/run-acceptance-smoke.ps1 -SkipStaticChecks` 全链路通过
  - 已完成单独静态验证：`backend/` 执行 `mvn -q -DskipTests compile` 成功，`frontend/` 执行 `npm run build` 成功
- 已完成第四轮后台展示统一（用户页角色区 + 答辩大屏）：
  - `UsersView.vue` 右侧角色说明已由多卡片改为单卡片内 `el-table`（角色名称 / 编码 / 说明），底部提示已改为常态化说明文案
  - `BigScreenView.vue` 的「展示说明」「重点预警」已改为 `el-table`，卡片头与后台一致使用 `panel-header` + `panel-title`；`styles.css` 新增 `.screen-page .screen-data-table` 深色表格变量
  - 静态验证：`frontend/` 执行 `npm run build` 成功
- 已部分收口辅助文档口径：
  - `zzz-docs/设计文档/后端接口与DTO-VO-Mapper设计-滚动预测版.md` 中冲突时的真相源已由 `.explore` 更正为 `.devflow/.../state.md` 与 handoff
  - `zzz-docs/设计文档/滚动预测改造-字段与接口变更方案.md` 文首已增加「数据库优先 MVP」口径提示
- 已为主流 CRUD 列表补「模糊搜索」能力：
  - 前端工具：`frontend/src/utils/fuzzyText.js`（`filterRows` 子串匹配，ASCII 忽略大小写）
  - 客户端分页页：`UsersView`（用户 + 角色表）、`WarehouseView`、`DataView` 粮温汇总表、`PredictionView`（结果表 + 历史表）、`DashboardView`（汇总 / 健康度 / 预警列表）均增加 `el-input` 搜索条
  - 服务端分页：`GET /api/grain-temp/records`、`GET /api/sensor-data` 增加可选 `keyword`，MyBatis `LIKE` 模糊匹配仓库名、点位/区域/层号/指标等；`DataView` 原始记录区输入防抖后触发重新拉页
- 已强化 Cursor 仓库规则：
  - `.cursor/rules/project-zh.mdc` §3 增加「实现前对齐」硬约束（触发条件、禁止同轮直接大批量改代码、须先输出理解/方案/待确认、用户豁免语）
- 已修复管理端「侧栏与主体一体滚动」并完成与 Element Plus Container 文档对齐的滚动收口（详见 `.devflow/grain-platform-bootstrap/plans/2026-04-10-console-layout-scroll-and-scrollbar.md`）：
  - **问题现象**：向下滚动时左侧导航与右侧内容同步被带起，像整页一体滚动。
  - **问题原因**：已使用 `el-container` / `el-aside` / `el-main`，但外层仅用 `min-height: 100vh`、未锁定视口高度与 flex 子项 `min-height: 0`，主内容增高时整页容器被撑高，由浏览器文档滚动统一滚动；**不是**「未使用 Element Plus」。
  - **解决方案**：`html`/`body`/`#app` 高度链；`.console-shell` 视口限高与 `overflow: hidden`；`.console-body` / `.console-main` 使用 `flex: 1`、`min-height: 0`；侧栏中部与主内容按官方示例增加 `el-scrollbar`（`ConsoleLayout.vue`、`styles.css`）；小屏断点恢复文档流滚动。
  - **验证**：`frontend/` 执行 `npm run build` 成功。
- 已修复管理端主内容区与温度预测页 **横向宽度持续增长**（详见 `.devflow/grain-platform-bootstrap/bug-log.md` **BUG-2026-04-10-001** 与 `learnings.md` 同日条目）：
  - **问题现象**：进入后台（尤以 `/prediction` 为甚）后页面横向不断变宽，右侧元素持续外移。
  - **问题原因**：主内容使用 `el-scrollbar` 时，其 `ResizeObserver` + `onUpdated` 更新链与 ECharts 容器 resize、flex `min-width: auto` 叠加，形成横向尺寸反馈。
  - **解决方案**：主内容改为原生滚动容器 `.console-main-native`（`ConsoleLayout.vue` / `styles.css`）；`flex: 1 1 0` 与顶栏 `min-width: 0` 等收紧；`PredictionView.vue` 在 `rAF` 后显式 `chart.resize`（零动画）。
  - **GSD quick记录**：`.planning/quick/260410-k32-vue-scrollbar-echarts-layout-fix/`；`.planning/STATE.md` 已增 **260410-k32** 行。
- 已增强「粮温原始测点记录」筛选：由单一关键词扩展为表头向工具栏 + 服务端条件（详见 `.devflow/grain-platform-bootstrap/plans/2026-04-10-grain-temp-records-filter-toolbar.md`）：
  - **目标**：区域 / 层号 / 点位用下拉，温度用区间，采集时间用范围；仓库仍以数据页右上方「查询条件」为准；保留关键词防抖模糊搜索。
  - **后端**：`GET /api/grain-temp/records` 在既有 `warehouseId`、`startTime`、`endTime`、`zoneCode`、`layerNo`、`keyword` 基础上增加 `pointNo`、`tempMin`、`tempMax`；新增 `GET /api/grain-temp/records/filter-options?warehouseId=` 返回去重后的 `zoneCodes`、`layerNos`、`pointNos`；`GrainTempRecordMapper.xml` / `GrainTempService` / `GrainTempController` 与 DTO `GrainTempRecordFilterOptionsDto`。
  - **前端**：`DataView.vue`（粮温模式下工具栏）、`frontend/src/api/grain.js`（`fetchGrainTempRecordFilterOptions`、`fetchGrainTempRecords` 参数）；`styles.css`（`.grain-record-filter-form` 等）。
  - **验证**：`backend/` `mvn -q -DskipTests compile`、`frontend/` `npm run build` 已通过；全量 `run-acceptance-smoke.ps1` 未因本项改写，答辩前可按需手工跑一遍。
- 已优化粮温固定 XLSX 模板版式（2026-04-10，模板 v2）：`GrainTempImportService.getExcelTemplate` 增加合并单元格与中文分区说明；矩阵块标签为「区域编码（zoneCode）」「缆号/探头编码（probeCode）」等；`parseFixedTemplate` 通过 `cellMatchesKey` 兼容旧版纯英文标签与新版双语标签；`DataView.vue` 导入说明已同步。GSD 侧 `.planning/PROJECT.md` / `REQUIREMENTS.md` 已更新；codebase 地图见 `.planning/codebase/`。
- 已完成 2026-04-11 文档与过程同步：
  - 已回看 `.planning` **Phase 1-9** 的需求、Roadmap 与状态，不再只看最新阶段。
  - 已对照 `backend/src/main/resources/db/schema.sql` 与当前 Controller 接口，确认数据库主结构无新增偏差；本轮差异主要在文档口径滞后，而非代码与数据库结构不一致。
  - 已更新 `zzz-docs/设计文档/` 下的 PRD、数据库设计、后端接口设计，使其覆盖固定模板 v2、预测页 Phase 2-6、环境数据页 Phase 7、大屏 Phase 8、去 mock 与服务端分页 Phase 9。
  - 已明确：`prediction_task` / `prediction_result` 中的修正链字段继续作为**扩展预留**，不重新定义为当前必做业务入口。

## 工作假设
- 以毕业设计 MVP 为目标，先做可演示的软件平台，不接入真实硬件。
- 预测功能继续采用简单回归或移动平均，重点在数据库可追溯与归档，而不是算法复杂度。
- 项目结构采用前后端分离：`backend/` + `frontend/`。
- 启动与联调默认使用 `8081`，避免与本地旧进程占用的 `8080` 冲突。
- 后续更像是“以现有项目为基础的半重写”，并以当前新主线作为唯一开发依据。

## 待解决的问题
- 首页与导入链路、后台管理关键链路已补齐一键验收脚本，但当前脚本在本沙箱内直接执行前端构建时仍可能命中 `esbuild spawn EPERM`；仓库内单独执行 `npm run build` 已通过。
- 验收脚本尚未对带 `keyword` 的分页接口做专门断言（可选补一条轻量 smoke）；粮温记录新增 `pointNo`/`tempMin`/`tempMax`/`filter-options` 亦未纳入脚本断言（可选）。
- GSD：`.planning/` 已初始化并含 codebase 地图；`.codex/`、`.cursor/` 下部分工具文件仍可能未跟踪，提交课题代码时勿整包纳入无关工具目录。
- 预测修正字段已保留，但本期仍未实现修正入口与修正页，这与当前范围收口一致。
- 用户/仓库列表当前为全量接口 + 前端搜索；若数据量显著增大，可再评估是否增加后端 `keyword`。
- `/screen` 大屏表格未加本地筛选（可选）。
- 归档目录、历史 handoff（`.explore/`、`zzz-docs/Archive/` 等）仍可能含旧主线表述，检索时以本 `state.md` 与最新 PRD 为准。
- 旧版 PRD、数据库定稿、旧接口设计已归档；历史 handoff/checkpoint 中仍保留旧文件名，属于历史上下文，不应作为当前真相源。

## 下一步
- 休息/恢复：下轮先读 handoff **023** 与根目录 `NEXT-SESSION-PROMPT-DEVFLOW.md`。
- 如继续写论文，可直接以已更新的 PRD、数据库设计、接口设计文档为材料基线，再补章节化描述或截图。
- 可选：扩展验收脚本与回归清单（粮温筛选）；按需 `git push`；初始化 GSD 时保持与 devflow 分工。
- 如需在沙箱环境里重复跑脚本，可优先使用 `-SkipStaticChecks`，静态命令单独执行。

## 当前参考计划
- `.devflow/grain-platform-bootstrap/plans/2026-04-11-gsd-devflow-prd-database-doc-sync.md`
- `.devflow/grain-platform-bootstrap/plans/active-plan-links.md`
- `.devflow/grain-platform-bootstrap/plans/2026-04-10-console-layout-scroll-and-scrollbar.md`
- `.devflow/grain-platform-bootstrap/plans/2026-04-10-grain-temp-records-filter-toolbar.md`
- `.devflow/grain-platform-bootstrap/plans/2026-04-09-init-strategy-and-admin-crud-sequencing.md`
- `.devflow/grain-platform-bootstrap/plans/2026-04-09-init-strategy-closure.md`
- `.devflow/grain-platform-bootstrap/plans/2026-04-09-warehouse-crud.md`
- `.devflow/grain-platform-bootstrap/plans/2026-04-09-user-crud.md`
- `.devflow/grain-platform-bootstrap/plans/2026-04-09-data-crud-and-temperature-recompute.md`
- `.devflow/grain-platform-bootstrap/plans/2026-04-09-frontend-list-unification.md`
- `.devflow/grain-platform-bootstrap/plans/2026-04-10-usersview-and-screen-display-unification.md`

## 最新 handoff
- `.devflow/grain-platform-bootstrap/handoffs/2026-04-11-023-doc-sync-and-thesis-materials-ready.md`

## 最小活跃上下文摘要
- **Git**：`16cabbc` 已提交（布局 + 粮温筛选 + devflow 文档）。需同步远端时本地 `git push`。
- **恢复**：`state.md` + handoff **023** + 根目录 `NEXT-SESSION-PROMPT-DEVFLOW.md`。本轮文档同步计划见 `plans/2026-04-11-gsd-devflow-prd-database-doc-sync.md`。
- **开放**：验收脚本扩展断言、回归清单补粮温筛选步骤、论文正文章节化展开与截图整理，可在 023 handoff 基础上继续。
- 新需求默认先对齐再编码（`project-zh.mdc` §3）；沙箱跑验收可 `-SkipStaticChecks`。


