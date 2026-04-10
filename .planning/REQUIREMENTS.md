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

- [ ] **DEL-05-01**: 「预测记录」表 **最左侧多选列**；工具栏 **「批量删除」**（`danger`）；操作列在 Phase 4 基础上增加 **「删除」**；**单删与批量删**在请求接口前均需 **`ElMessageBox.confirm`**，且批量确认须展示 **选中数量**（或等价可读摘要）；删除成功后刷新列表并 **清空表格勾选**；若删除集合包含当前查看的 `taskId`，须 **清空**摘要区与图表主视觉（见 `05-CONTEXT.md` D-05～D-07）
- [ ] **DEL-05-02**: 后端提供 **`DELETE /api/predictions/tasks/{taskId}`** 与 **`POST /api/predictions/tasks/batch-delete`**（请求体 `taskIds` 列表）；**物理删除**对应 `prediction_result` 与 `prediction_task`；**单事务**；空列表 **400**；单条不存在 **404**（或与项目统一错误形态一致）；批量部分 id 无效时推荐 **整批失败回滚**（见 `05-CONTEXT.md` D-02～D-03b）

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
| DEL-05-01 | Phase 5 | Planned |
| DEL-05-02 | Phase 5 | Planned |
| OVERFLOW-06-01 | Phase 6 | Done（待 UAT） |
| OVERFLOW-06-02 | Phase 6 | Done（待 UAT） |
| OVERFLOW-06-03 | Phase 6 | Done（待 UAT） |
| ENV-07-01 | Phase 7 | Done |
| ENV-07-02 | Phase 7 | Done |
| ENV-07-03 | Phase 7 | Done |

**Coverage:**

- Phase 1 / 模板 v1：4 条（DATA-*，已交付）
- Phase 2 / 预测页：3 条（PRED-*）
- Phase 3 / 预测交互：3 条（UX2-*）
- Phase 4 / 预测页打磨：2 条（POLISH-04-*）
- Phase 5 / 预测记录删除：2 条（DEL-05-*）
- Phase 6 / overflow-x 收口：3 条（OVERFLOW-06-*）
- Phase 7 / 粮温汇总优化：3 条（ENV-07-*）
- Mapped to phases: 20
- Unmapped: 0

---
*Last updated: 2026-04-11 Phase 7 增加 ENV-07-*（粮温汇总服务端分页、共享时间范围、层级目标切换与图表/表格查询态拆分）*

