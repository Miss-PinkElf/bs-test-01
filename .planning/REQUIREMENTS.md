# Requirements: 粮仓平台（GSD 增量）

**Defined:** 2026-04-10  
**Core Value:** 粮温与环境数据可导入、可查、可汇总，预测结果可追溯展示（见 `PROJECT.md`）

## v1 Requirements

本轮「粮温固定模板」可读性与导入一致性。

### Data Import / Template

- [x] **DATA-01**: 用户下载的粮温固定模板在 Excel 中层级清晰：中文说明、合并单元格、表头分区；关键列仍带英文键便于程序识别
- [x] **DATA-02**: `GrainTempImportService.getExcelTemplate` 与 `parseFixedTemplate` 规则一致；兼容旧版纯英文标签
- [x] **DATA-03**: `DataView.vue` 粮温导入说明与模板结构一致
- [x] **DATA-04**: `mvn compile`、`npm run build` 已通过（2026-04-10）；端到端导入建议在联调环境手工或 smoke 再验

## Phase 2 Requirements（温度预测页）

### Prediction / Console UX

- [x] **PRED-01**: 温度预测页将任务元数据（任务号、仓库、预测对象、算法、风险等级、预测天数、训练/预测区间、执行时间、摘要）以分组 + 栅格化布局展示，避免零散纵向堆叠，一屏内可读性提升（2026-04-10：`el-descriptions` 双块，待你验收）
- [x] **PRED-02**: 预测表单支持可选「训练数据开始时间」「训练数据结束时间」，调用预测接口时映射为 `trainStartTime` / `trainEndTime`；两者皆空时行为与当前「使用全部可用历史」一致（2026-04-10：折叠内 `datetimerange` + 清空）
- [x] **PRED-03**: 训练区间无效或历史数据不足时，用户可见明确错误提示（承接后端 `IllegalArgumentException` 等文案或前端校验）（2026-04-10：前端区间校验 + `ElMessage.error`）

## Phase 3 Requirements（温度预测页 · 交互二次）

### Prediction flow / copy

- [x] **UX2-01**: 页面区块顺序为「参数与摘要 → 双线图 → 预测结果表与历史归档表」；执行预测后用户无需滚到页底即可看到主曲线（2026-04-10：已调整顺序并 `scrollIntoView`）。**演进（Phase 4 / POLISH-04-01）：** 底部改为**单一「预测记录」表**，不再并列两张表；曲线仍在双线图区。
- [x] **UX2-02**: 「预测结果列表」与「历史归档记录」卡片均展示一句副标题，说明各自数据含义及历史表「点击行切换当前任务」（2026-04-10：副标题 + 图表区说明）。**演进（Phase 4 / POLISH-04-02）：** 合并为一张「预测记录」表副标题；切换任务改为**操作列按钮**，无整行点击。
- [x] **UX2-03**: 温度预测页首行（参数 \| 任务摘要）与次行（预测结果 \| 历史归档）在 **`md`（≥992px）起** 即按15:9 并排；`xs`/`sm` 整行叠放。避免仅配置 `xl` 导致 Element Plus 在常见笔记本宽度下将两列堆叠为上下布局（2026-04-10：`PredictionView.vue` `el-col` 断点）。**演进（Phase 4）：** 「次行」仅余**全宽单表**（无左右 15:9 第二行双列）；首行参数与摘要并排仍适用本条的 **`md` 起并排** 要求。

## Phase 4 Requirements（温度预测页 · 单表与数据源）

### Polish / prediction table

- [x] **POLISH-04-01**: 预测页底部仅保留**一张**任务级「预测记录」表（合并原分步明细表与历史归档的心智）；表含 **「预测区间」**列（`forecastStartTime` ~ `forecastEndTime`）；列表数据来自 **`GET /api/predictions/tasks`**（`PredictionService` 读库归档，非前端 mock）（2026-04-10：`PredictionView.vue` + CONTEXT）
- [x] **POLISH-04-02**: 该表提供 **「查看摘要」「切换任务」** 操作列，**禁止**整行 `row-click` 切换；「查看摘要」滚至任务摘要卡片并具备可验证的 **`prediction-summary-flash`** 强调样式（2026-04-10）

## Phase 5 Requirements（温度预测页 · 预测记录删除）

### Delete / prediction archive

- [x] **DEL-05-01**: 「预测记录」表 **最左侧多选列**；工具栏 **「批量删除」**（`danger`）；操作列在 Phase 4 基础上增加 **「删除」**；**单删与批量删**在请求接口前均需 **`ElMessageBox.confirm`**，且批量确认须展示 **选中数量**（或等价可读摘要）；删除成功后刷新列表并 **清空表格勾选**；若删除集合包含当前查看的 `taskId`，须 **清空**摘要区与图表主视觉（见 `05-CONTEXT.md` D-05～D-07）（2026-04-11：`PredictionView.vue` 多选、批量删、单删、删除后状态清理已确认）
- [x] **DEL-05-02**: 后端提供 **`DELETE /api/predictions/tasks/{taskId}`** 与 **`POST /api/predictions/tasks/batch-delete`**（请求体 `taskIds` 列表）；**物理删除**对应 `prediction_result` 与 `prediction_task`；**单事务**；空列表 **400**；单条不存在 **404**（或与项目统一错误形态一致）；批量部分 id 无效时推荐 **整批失败回滚**（见 `05-CONTEXT.md` D-02～D-03b）（2026-04-11：`PredictionController`、`PredictionService`、Mapper 删除链路已确认）

## Phase 6 Requirements（温度预测页 · overflow-x 收口）

### Layout / overflow-x

- [x] **OVERFLOW-06-01**: 管理端主内容区在保留原生纵向滚动、且不恢复 `el-scrollbar` 主内容承载的前提下，**不再一刀切 `overflow-x: hidden`**；当页面出现**真实超宽内容**时，主内容区应能提供正常横向滚动，而非直接裁切（2026-04-10：`.console-main-native` 改为 `overflow-x: auto`）
- [x] **OVERFLOW-06-02**: 温度预测页中**底部「预测记录」表**继续通过局部 wrapper 承担横向滚动；右侧**任务摘要**不依赖整页横向滚动，而应通过 `min-width: 0`、可换行内容和局部 wrapper 控制在窄列内稳定显示，避免出现“超宽后被直接裁切”的表现（2026-04-10：`task-summary-card` / `task-summary-table-wrap` / `el-descriptions` fixed-layout）
- [x] **OVERFLOW-06-03**: 需保持对 **BUG-2026-04-10-001** 根因的规避：主内容层**不重新引入** `el-scrollbar + ECharts + ResizeObserver` 的高风险组合；预测页不再出现“页面进入后横向持续变宽”的回归（2026-04-10：保留主内容原生滚动 + 预测页 `chart.resize` 方案）

## Phase 7 Requirements（环境数据页 / 粮温汇总优化）

### Grain summary / environment data page

- [x] **ENV-07-01**: `DataView.vue` 的 **粮温汇总结果** 改为服务端分页，不再使用前端 `useClientPagination(filteredGrainSummaries)`；后端提供分页接口，支持 `warehouseId`、时间范围、关键词、预警等级、整仓均温范围、`pageNum/pageSize`（2026-04-11：`/api/grain-temp/summaries/page` + `PageResult`）
- [x] **ENV-07-02**: **粮温汇总趋势图** 支持共享的 **仓库 + 时间范围** 查询，并支持 **层级目标** 切换：`整仓均温 / 一层均温 / 二层均温 / 三层均温 / 四层均温`；图表固定保留 **最高温** 参考线（2026-04-11：`DataView.vue` 图例与 series 改造）
- [x] **ENV-07-03**: 图表与汇总表 **共享仓库 + 时间范围**，但汇总表的 **关键词 / 预警等级 / 温度范围** 只影响表格，不反向影响图表；普通环境模式与粮温原始测点记录链路不回归（2026-04-11：summary series / summary page / record page 状态拆分）

## Phase 8 Requirements（展示大屏优化）

### Big screen / defense board

- [x] **SCREEN-08-01**: `/screen` 继续沿用现有公开路由与 `BigScreenView.vue`，但首屏必须先渲染标题、操作区与模块骨架；任一图表或明细模块失败时不得整页白屏；页面不再以 `mockScreenMetrics` / `mockScreenAlerts` 作为常态展示源，图表初始化必须在容器可用后执行（2026-04-11：`/api/dashboard/screen` + `BigScreenView.vue` 首屏壳层 + 图表生命周期安全初始化）
- [x] **SCREEN-08-02**: 页面结构固定为 **上总览 / 中图表 / 下明细**；中部至少有 **粮温趋势、预警趋势、预测趋势、仓库对比** 4 个稳定信息位，且粮温趋势为主图；底部必须展示 **最新预警、最近预测任务、重点仓库、说明**（2026-04-11：`BigScreenView.vue` 三段式布局与四图四明细完成）
- [x] **SCREEN-08-03**: 大屏遵循“能真实就真实”：顶部指标、重点仓库、最新预警、最近预测任务与图表序列优先来自真实接口；新增或扩展的大屏统计接口需支持共享 **仓库 + 时间范围** 查询，并保持“后台面板增强版”信息风格而不是回退为说明页（2026-04-11：screen 聚合接口 + 共享筛选 + 人工验收通过）

## Phase 9 Requirements（管理端列表去 mock 与分页收口）

### Admin list / dashboard pagination

- [x] **MOCK-09-01**: `UsersView.vue`、`WarehouseView.vue`、`PredictionView.vue` 对应后端提供真实分页接口与 keyword 查询，页面切页与关键字变化均回源，不再对全量数组执行本地分页主链（2026-04-11：`/api/users/page`、`/api/warehouses/page`、`/api/predictions/tasks/page`）
- [x] **MOCK-09-02**: `PredictionView.vue` 分页接口继续返回完整 `PredictionTaskResponse` 与 `resultList`，且查看摘要、切换任务、单删、批量删交互不回退（2026-04-11：`PredictionService.listTaskPage()` + `PredictionView.vue` 服务端分页迁移）
- [x] **MOCK-09-03**: `DashboardView.vue` 改为 `overview + 近期预警分页 + 仓库运行健康度分页 + 最新粮温汇总分页`，三块模块各自维护 `keyword / pageNum / pageSize / total / loading / error`（2026-04-11：`/api/dashboard/alerts`、`/warehouse-health`、`/grain-summaries` + `DashboardView.vue`）
- [x] **MOCK-09-04**: `frontend/src/mock/platform.js` 删除，目标页面运行时不再以本地 mock 数据兜底，接口失败时显式显示模块错误态（2026-04-11：mock 文件删除，Dashboard / Users / Warehouse / Prediction 页清理完成）

## Phase 10 Requirements（前后端简单注释可读性）

### Comment readability / maintenance handoff

- [ ] **COMMENT-10-01**: `frontend/src/api/grain.js`、`PredictionView.vue`、`DataView.vue`、`DashboardView.vue`、`UsersView.vue`、`WarehouseView.vue` 只在数据归一化、分页状态回源、任务切换、导入刷新、选中态兜底等非显而易见逻辑块前补充简短中文注释；不注释显而易见模板或简单赋值，不改行为
- [ ] **COMMENT-10-02**: `PredictionService.java`、`DashboardService.java`、`GrainTempService.java`、`SensorDataImportService.java`、`UserService.java`、`WarehouseService.java`、`PredictionController.java`、`GrainTempController.java` 只在预测编排、分页归一化、导入校验、汇总重算、统计口径与接口边界等非显而易见逻辑块前补充简短中文注释；不写大段 Javadoc，不改行为
- [ ] **COMMENT-10-03**: Phase 10 验证必须同时包含 `cd frontend && npm run build`、`cd backend && mvn -q -DskipTests compile` 通过，以及 `rg` 证据证明注释确实加在计划指定文件与指定逻辑附近
## v2 Requirements

- [ ] **DATA-05**: 验收脚本 `run-acceptance-smoke.ps1` 增加针对新版模板版式或关键标签的断言（可选）

## Out of Scope

| Feature | Reason |
| --- | --- |
| 重写非粮温导入链路 | 未在本次诉求中 |
| 多算法切换与预测模型替换 | 本期 Phase 2 仅 UX + 训练窗口；算法仍线性回归 MVP |

## Traceability

| Requirement | Phase | Status |
| --- | --- | --- |
| DATA-01 | Phase 1 | Done |
| DATA-02 | Phase 1 | Done |
| DATA-03 | Phase 1 | Done |
| DATA-04 | Phase 1 | Done |
| PRED-01 | Phase 2 | Done（待 UAT） |
| PRED-02 | Phase 2 | Done（待 UAT） |
| PRED-03 | Phase 2 | Done（待 UAT） |
| UX2-01 | Phase 3 / 4演进 | Done（待 UAT） |
| UX2-02 | Phase 3 / 4 演进 | Done（待 UAT） |
| UX2-03 | Phase 3 / 4 演进 | Done（待 UAT） |
| POLISH-04-01 | Phase 4 | Done（待 UAT） |
| POLISH-04-02 | Phase 4 | Done（待 UAT） |
| DEL-05-01 | Phase 5 | Done |
| DEL-05-02 | Phase 5 | Done |
| OVERFLOW-06-01 | Phase 6 | Done（待 UAT） |
| OVERFLOW-06-02 | Phase 6 | Done（待 UAT） |
| OVERFLOW-06-03 | Phase 6 | Done（待 UAT） |
| ENV-07-01 | Phase 7 | Done |
| ENV-07-02 | Phase 7 | Done |
| ENV-07-03 | Phase 7 | Done |
| SCREEN-08-01 | Phase 8 | Done |
| SCREEN-08-02 | Phase 8 | Done |
| SCREEN-08-03 | Phase 8 | Done |
| MOCK-09-01 | Phase 9 | Done |
| MOCK-09-02 | Phase 9 | Done |
| MOCK-09-03 | Phase 9 | Done |
| MOCK-09-04 | Phase 9 | Done |
| COMMENT-10-01 | Phase 10 | Planned |
| COMMENT-10-02 | Phase 10 | Planned |
| COMMENT-10-03 | Phase 10 | Planned |

**Coverage:**

- Phase 1 / 模板 v1：4 条（DATA-*，已交付）
- Phase 2 / 预测页：3 条（PRED-*）
- Phase 3 / 预测交互：3 条（UX2-*）
- Phase 4 / 预测页打磨：2 条（POLISH-04-*）
- Phase 5 / 预测记录删除：2 条（DEL-05-*）
- Phase 6 / overflow-x 收口：3 条（OVERFLOW-06-*）
- Phase 7 / 粮温汇总优化：3 条（ENV-07-*）
- Phase 8 / 展示大屏优化：3 条（SCREEN-08-*）
- Phase 9 / 管理端列表去 mock：4 条（MOCK-09-*）
- Phase 10 / 前后端简单注释可读性：3 条（COMMENT-10-*）
- Mapped to phases: 30
- Unmapped: 0

---
*Last updated: 2026-04-11 Phase 10 COMMENT-10-* 已规划；Phase 9 MOCK-09-* 已完成，Phase 5 DEL-05-* 与 Phase 8 SCREEN-08-* 已完成*





