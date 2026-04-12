# ROADMAP: 粮仓平台（GSD）

**Milestone:** 毕设数据库优先 MVP — 体验优化波次  
**Updated:** 2026-04-12

## Overview

| # | Phase | Goal | Requirements | Success Criteria |
| --- | --- | --- | --- | --- |
| 1 | 粮温 Excel 模板与导入一致化 | 模板人类可读、生成与解析同源、说明同步 | DATA-01 — DATA-04 | 已完成（模板 v2） |
| 2 | 温度预测页体验优化 | 任务信息集中可读；预测前可配置训练区间 | PRED-01 — PRED-03 | **已交付**（UAT 见 REQ） |
| 3 | 温度预测页交互二次优化 | 图表先达、双表语义可读 | UX2-01 — UX2-03（见 REQUIREMENTS） | **已交付**（UAT 见 REQ）；底部表形态见 Phase 4 演进 |
| 4 | 温度预测页单表与预测区间 | 单表、预测区间列、操作列；后端可追溯 | POLISH-04-01 — POLISH-04-02 | **已落地**（待 UAT） |
| 5 | 预测记录表支持删除 | 多选 + 批量删 + 单删；二次确认；DEL-05-* | **已完成**（2026-04-11） |
| 6 | 预测页 overflow-x 收口 | 恢复正常横向滚动；摘要局部收口；不回退高风险滚动模型 | OVERFLOW-06-01 — OVERFLOW-06-03 | **已落地**（待 UAT） |
| 7 | 环境数据页 / 粮温汇总优化 | 1/1 | Complete   | 2026-04-10 |
| 8 | 展示大屏优化 | `/screen` 稳定打开并升级为答辩展示大屏 | SCREEN-08-01 — SCREEN-08-03 | **已完成**（2026-04-11） |
| 9 | 管理端列表去 mock 并收口服务端分页查询 | 移除残留 mock，并把 Users/Warehouse/Prediction/Dashboard 收口到后端分页与 keyword 查询 | MOCK-09-01 — MOCK-09-04 | **已完成**（2026-04-11） |
| 10 | 前后端补充简单注释提升可读性 | 在关键逻辑处补简单注释，降低阅读与接手成本 | COMMENT-10-01 — COMMENT-10-03 | **已完成**（2026-04-11） |
| 11 | 河南环境数据 1-4 月 mock 与脏数据清理 | 补齐 Jan-Apr 河南 baseline、清理旧脏数据，并明确 Windows 一键运行延期 | Context decisions D-01 — D-09 | **已完成**（2026-04-11） |
| 12 | 图表时间范围与长 x 轴收口 | 保留 DataView 粮温汇总趋势图、PredictionView 双线图与 BigScreen 粮温趋势主图，同时为三张图补默认窗口或共享时间范围下的长 x 轴可读性方案 | CHART-12-01 — CHART-12-04 | **已落地**（2026-04-12，待 UAT） |

## Phase 1: 粮温 Excel 模板与导入一致化（已完成）

**Delivered:** 固定 XLSX 合并单元格与中文分区；`cellMatchesKey` 兼容双语标签；`DataView` 文案同步。

**可选后续：** DATA-05 验收脚本扩展。

---

## Phase 2: 温度预测页体验优化：任务信息集中展示与预测参数扩展（训练区间等）（已交付）

**阶段目录：** `.planning/phases/02-prediction-view-ux/`

**Delivered（2026-04-10）：** 摘要 `el-descriptions`、折叠训练区间、`ElMessage`、后端 `PredictionRequest`时间格式；详见 `zzz-docs/归档/2026-04-10-温度预测页Phase2-Phase3交付归档.md`。

**Goal:** 解决预测页「信息散、参数少」：摘要区信息架构收紧；表单透出后端已支持的训练时间窗口，默认行为不变。

**Requirements:** PRED-01 — PRED-03

**Depends on:** Phase 1（无硬依赖）

**Success criteria:**

1. 同一视口内任务元数据分组清晰。
2. 用户可选择训练数据起止时间并发起预测；不传区间时与全量历史行为一致。
3. 历史数据不足或区间无效时，错误信息可理解。

**Plans:** 已实现；可选补 `PLAN.md` 仅作文档归档。

---

## Phase 3: 温度预测页交互二次优化：图表可达性与双表语义说明（已交付）

**阶段目录：** `.planning/phases/03-prediction-ux-pass2/`（`ALIGNMENT.md`、`03-CONTEXT.md`）

**Delivered（2026-04-10）：** 图序、图表锚点滚动、两表 `panel-subtitle`、`md` 起 15:9 栅格；详见 `zzz-docs/归档/2026-04-10-温度预测页Phase2-Phase3交付归档.md`。

**Goal:** 执行预测后主视觉（曲线）无需滚到页底即可看到；两张表的职责与「点击历史行切换任务」一眼可读。

**Requirements:** UX2-01 — UX2-03

**Depends on:** Phase 2

**Success criteria:**

1. 常规视口下，点击「执行预测」后图表进入可视区域（布局：参数+摘要 → 图 → 双表）。
2. 两表卡片均有一句副标题，能区分「当前任务分步明细」与「历史任务列表」。
3. **`md` 及以上宽度**下参数与任务摘要左右并排（非仅超宽 `xl` 才并排），与 UX2-03 一致。

**Plans:** 与 ALIGNMENT 同步落地；可选 `/gsd-plan-phase 3` 补 PLAN.md。

## Phase 4: 温度预测页单表打磨（prediction-page-polish）

**阶段目录：** `.planning/phases/04-prediction-page-polish/`（`04-CONTEXT.md`、`04-UI-SPEC.md`、`04-01-PLAN.md`）

**Goal:** 消除底部「预测结果列表」与「历史归档」并列带来的重复感：**仅保留一张任务级「预测记录」表**，含 **预测区间** 列与 **操作列**；列表与区间字段来自后端 **`GET /api/predictions/tasks`**（`PredictionService` 归档，非前端 mock）。

**Requirements:** POLISH-04-01 — POLISH-04-02（`REQUIREMENTS.md`）；与 Phase 3 的 UX2-* 演进说明同条引用。

**Depends on:** Phase 3

**Success criteria:**

1. 页面底部仅有 **一张** 全宽任务表，标题与副标题能说明「每次预测一行」。
2. 表中含 **预测区间**列（forecast 起止），且搜索可命中该列展示内容。
3. 通过 **操作列** 完成查看摘要与切换任务；**无** 整行点击切换。
4. 文档（CONTEXT / VERIFICATION）可追溯到后端接口与落库实现。

**Plans:**

- [x] `04-01-PLAN.md` — 单表、预测区间、操作列、GSD 同步（Wave 1）

### Phase 5: 温度预测页预测记录表操作列增加删除，用于清理无用或脏数据记录（已完成）

**阶段目录：** `.planning/phases/05-prediction-record-delete/`（`05-CONTEXT.md`、`05-UI-SPEC.md`、`05-01-PLAN.md`）

**Goal:** 「预测记录」表支持 **表前多选**、**批量删除** 与 **操作列单条删除**；**凡删除二次确认**（批量须展示数量）；后端 **单删 + 批量删** API，物理删除任务与关联结果。

**Requirements:** DEL-05-01 — DEL-05-02（`REQUIREMENTS.md`）

**Depends on:** Phase 4

**Success criteria:**

1. 表最左列为多选；工具栏有「批量删除」且与 CONTEXT 中无选禁用策略一致。
2. 单删、批量删均有 `ElMessageBox.confirm`；批量确认含选中数量。
3. `DELETE /api/predictions/tasks/{id}` 与 `POST /api/predictions/tasks/batch-delete` 可用且与持久化一致。
4. 删后列表与勾选状态、当前任务主视觉行为符合 `05-CONTEXT.md`。

**Plans:**

- [x] `05-01-PLAN.md` — 后端删除、前端 API、PredictionView 多选与批量删、文档同步（Wave 1）

### Phase 6: 预测页过宽：任务摘要横向滚动与主内容区 overflow-x 裁切问题

**阶段目录：** `.planning/phases/06-overflow-x/`（`06-CONTEXT.md`、`06-UI-SPEC.md`、`06-01-PLAN.md`、`06-VERIFICATION.md`）

**Delivered（2026-04-10）：** 主内容 `.console-main-native` 恢复 `overflow-x: auto`；预测页任务摘要补 `task-summary-card` / `task-summary-table-wrap`、`min-width: 0`、fixed-layout 与内容换行；保留主内容原生滚动与预测页 `chart.resize` 规避方案。

**Goal:** 在不回退到主内容 `el-scrollbar` 的前提下，恢复后台页面在**真实超宽内容**下的正常横向滚动；同时让预测页右侧任务摘要在窄列中稳定显示，不再因全局 `overflow-x: hidden` 被直接裁切。

**Requirements**: OVERFLOW-06-01 — OVERFLOW-06-03
**Depends on:** Phase 5

**Success criteria:**

1. 主内容区在真实超宽时出现正常横向滚动，而非统一裁切。
2. 预测页底部宽表继续走局部横向滚动；右侧任务摘要不再直接裁切。
3. 主内容层仍不恢复 `el-scrollbar`，保留对 `el-scrollbar + ECharts + ResizeObserver` 反馈循环的规避。

Plans:
- [x] `06-01-PLAN.md` — 主内容 overflow-x 收口、预测页摘要局部收口、GSD 同步（Wave 1）

### Phase 7: zzz-prompt-debug/origin/环境数据页面优化/prompt.md

**阶段目录：** `.planning/phases/07-zzz-prompt-debug-origin-prompt-md/`（`07-CONTEXT.md`、`07-RESEARCH.md`、`07-01-PLAN.md`、`07-VERIFICATION.md`）

**Goal:** 把环境数据页中 **粮温主线** 的“粮温汇总趋势图 + 粮温汇总结果”从演示型静态查询，升级为可用于排查与答辩展示的查询页：汇总结果改为**后端分页 + 多条件筛选**；趋势图支持**仓库、时间范围、层级目标**，且固定保留**最高温**参考线。

**Requirements**: ENV-07-01 — ENV-07-03
**Depends on:** Phase 6

**Success criteria:**

1. 汇总结果表走服务端分页，支持关键词、预警等级、整仓均温范围，不再使用前端内存分页。
2. 趋势图支持共享的仓库 + 时间范围查询，并支持 `整仓均温 / 一层 / 二层 / 三层 / 四层` 目标切换，同时保留“最高温”对照线。
3. 图表与汇总表共享仓库 + 时间范围，但表格细筛不影响图表；普通环境模式和粮温原始测点记录链路不回归。

**Plans:**

1/1 plans complete

### Phase 8: zzz-prompt-debug\origin\展示大屏优化（已完成）

**阶段目录：** `.planning/phases/08-zzz-prompt-debug-origin/`（`08-CONTEXT.md`、`08-UI-SPEC.md`、`08-01-PLAN.md`、`08-02-PLAN.md`、`08-VERIFICATION.md`）

**Goal:** 将现有公开路由 `/screen` 从“指标卡 + 说明表 + 预警表”的轻量页，升级为适合答辩展示的 **信息型大屏**：优先解决卡住 / 白屏风险，在 **上总览 / 中图表 / 下明细** 骨架下用真实数据展示 **粮温趋势、预警趋势、预测趋势、仓库对比**，并保留最新预警、最近预测任务与重点仓库等明细。

**Requirements**: SCREEN-08-01 — SCREEN-08-03
**Depends on:** Phase 7

**Success criteria:**

1. `/screen` 首屏先渲染标题、操作区与模块骨架；任一模块失败不导致整页白屏，图表初始化不再引发明显卡顿。
2. 页面稳定落为 **上总览 / 中图表 / 下明细** 三段式，且中部存在 **粮温趋势、预警趋势、预测趋势、仓库对比** 4 个真实信息位。
3. 顶部指标、重点仓库、最新预警、最近预测任务与主图表优先来自真实接口，并支持共享 **仓库 + 时间范围** 查询。

**Delivered（2026-04-11）：** `/api/dashboard/screen` 聚合接口、`fetchScreenDashboard()`、`/screen` 三段式大屏、四块图表位、四块底部明细位、文字对比度收口。

**Plans:** 2/2 plans complete

Plans:
- [x] `08-01-PLAN.md` — 大屏真实数据聚合接口与前端 API 封装（Wave 1）
- [x] `08-02-PLAN.md` — `/screen` 三段式重构、图表稳定渲染与底部明细区（Wave 2）

### Phase 9: 管理端列表去 mock 并收口服务端分页查询（已完成）

**阶段目录：** `.planning/phases/09-mock/`（`09-CONTEXT.md`、`09-RESEARCH.md`、`09-UI-SPEC.md`、`09-01-PLAN.md`、`09-02-PLAN.md`、`09-VERIFICATION.md`）

**Delivered（2026-04-11）：** Users / Warehouse / Prediction 补齐后端分页与统计接口；Dashboard 拆为 `overview + alerts + warehouse-health + grain-summaries` 独立分页接口；四个目标页面切到服务端分页；`frontend/src/mock/platform.js` 已删除；人工验收通过。

**Goal:** 移除管理端剩余前端 mock 痕迹，并把 `UsersView`、`WarehouseView`、`PredictionView`、`DashboardView` 从前端内存分页/本地过滤收口到后端驱动的 `keyword + pageNum + pageSize` 模式，同时保持现有页面语义与布局不变。
**Requirements**: MOCK-09-01 — MOCK-09-04
**Depends on:** Phase 8
**Success criteria:**

1. `UsersView`、`WarehouseView`、`PredictionView`、`DashboardView` 不再以后端全量数组作为分页主链，切页和 keyword 都会回源。
2. `PredictionView` 分页接口仍返回完整任务详情与 `resultList`，不拆成轻量列表 + 详情接口。
3. `DashboardView` 运行方式改为 `overview + 近期预警分页 + 仓库健康度分页 + 最新粮温汇总分页`，而不是 `overview` 大包数组本地切片。
4. `frontend/src/mock/platform.js` 被删除，`DataView.vue` 与 `/screen` 不被本阶段回归。

**Plans:** 2/2 plans complete

Plans:
- [x] `09-01-PLAN.md` — 后端补齐用户、仓库、预测任务分页与统计接口（Wave 1）
- [x] `09-02-PLAN.md` — Dashboard 拆分分页接口、四页前端迁移与 mock 清理（Wave 2）

### Phase 10: 前后端补充简单注释提升可读性（已完成）

**阶段目录：** `.planning/phases/10-code-comment-readability/`

**Delivered（2026-04-11）：** 前端 `grain.js` 与 Prediction/Data/Dashboard/Users/Warehouse 视图补齐状态同步与归一化注释；后端 Prediction/Dashboard/GrainTemp/SensorDataImport/User/Warehouse 服务与相关控制器补齐分页、导入、汇总与统计边界注释；`npm run build` 与 `mvn -q -DskipTests compile` 通过。

**Goal:** 在前后端高阅读成本、但当前注释偏少的关键逻辑文件中，补充简短中文注释，帮助后续维护者理解数据归一化、分页状态、任务切换、导入校验、服务编排等非显而易见流程；严格避免行为改动、注释泛滥、以及把显而易见代码重复解释一遍。
**Requirements**: COMMENT-10-01 — COMMENT-10-03
**Depends on:** Phase 9
**Success criteria:**

1. 前端目标文件只在数据归一化、分页状态回源、任务切换、导入刷新、选中态兜底等非显而易见逻辑块前补充短中文注释。
2. 后端目标文件只在预测编排、分页归一化、导入校验、汇总重算、统计口径等非显而易见逻辑块前补充短中文注释。
3. `npm run build` 与 `mvn -q -DskipTests compile` 均通过，且 `rg` 可证明注释落在计划指定文件与指定逻辑附近。

**Plans:** 2/2 plans complete

Plans:
- [x] `10-01-PLAN.md` — 前端归一化、分页状态与任务切换注释收口（Wave 1）
- [x] `10-02-PLAN.md` — 后端预测编排、导入校验与统计边界注释收口（Wave 1）

### Phase 11: 河南环境数据 1-4 月 mock、脏数据清理与 Windows 一键运行

**Goal:** 把 demo 库收口为可重建的标准演示库：为 2 到 3 个重点仓库固化 1 月 1 日到 4 月 30 日的河南场景温度 / 湿度 / co2 连续 mock 数据，清理与之冲突的旧脏数据，并以 `schema.sql + reset-demo-db.ps1` 作为唯一重置入口；Windows 一键运行已延期到后续 phase。
**Requirements**: Context decisions D-01 — D-09 (`.planning/phases/11-1-4-mock-windows/11-CONTEXT.md`)
**Depends on:** Phase 10
**Delivered（2026-04-11）：** `schema.sql + reset-demo-db.ps1 + verify-demo-baseline.ps1` 已收口 Jan-Apr 河南 baseline；`run-acceptance-smoke.ps1` 已串起 reset / baseline verifier / API / dashboard / grain import 验收；固定模板粮温导入误判也已随 smoke 修复。用户已确认可直接归档，Phase 11 完成。

**Plans:** 2/2 plans complete

Plans:
- [x] `11-01-PLAN.md` — 重写标准演示库种子并收口 reset-demo-db 校验（Wave 1）
- [x] `11-02-PLAN.md` — 接入 Phase 11 baseline smoke 校验并同步 Phase 文档（Wave 2）

### Phase 12: zzz-prompt-debug/origin/优化图表/prompt.md

**阶段目录：** `.planning/phases/12-zzz-prompt-debug-origin-prompt-md/`（`12-CONTEXT.md`、`12-01-PLAN.md`、`12-VERIFICATION.md`、`12-HUMAN-UAT.md`）

**Delivered（2026-04-12）：** `DataView.vue` 粮温汇总趋势图首次进入默认收口到最近 30 天，并补齐 `axisLabel + rotate + dataZoom`；`PredictionView.vue` 双线图新增图表级时间范围控件，默认显示最近 7 天、清空恢复完整时间线；`BigScreenView.vue` 的粮温趋势主图继续复用大屏共享时间范围，并同步补齐 `axisLabel + rotate + dataZoom`；`frontend` `npm run build` 通过，人工验收待执行。

**Goal:** 保留并增强 `DataView.vue` 的粮温汇总趋势图、`PredictionView.vue` 的实际值 / 预测值双线图，以及 `BigScreenView.vue` 的粮温趋势主图，让三张图都建立在可控时间范围上，并通过默认时间窗口或共享时间范围与 ECharts 交互兜底解决“时间轴过长、展示不全”的问题。
**Requirements**: CHART-12-01 — CHART-12-04
**Depends on:** Phase 11
**Success criteria:**

1. `DataView.vue` 粮温汇总趋势图继续沿用现有共享仓库 + 时间范围查询口径，首次进入粮温主线默认最近 30 天，长时间轴仍可通过缩放 / 滑动查看。
2. `PredictionView.vue` 的图表时间范围只影响双线图显示，不污染任务摘要中的 `trainStartTime / trainEndTime / forecastStartTime / forecastEndTime` 语义；默认窗口优先保证首屏可读。
3. `BigScreenView.vue` 的粮温趋势主图继续复用大屏已有共享时间范围，不额外新增一套图表查询态；时间窗口拉长后仍可缩放 / 滑动查看。
4. Phase 12 的 `REQUIREMENTS / ROADMAP / STATE / VERIFICATION / HUMAN-UAT / SUMMARY` 不再停留在 `TBD` 或缺失状态。

**Plans:** 1/1 plans complete

Plans:
- [x] `12-01-PLAN.md` — DataView / PredictionView / BigScreen 粮温趋势图时间范围与 x 轴收口、Phase 文档同步（Wave 1）

---

*Roadmap updated: 2026-04-12（Phase 12：图表时间范围与长 x 轴收口已扩到大屏粮温趋势并待 UAT；Phase 11：河南环境数据 1-4 月 mock、脏数据清理与 Windows 一键运行；Phase 10：前后端补充简单注释提升可读性；Phase 9：管理端列表去 mock 并收口服务端分页查询；Phase 8：展示大屏优化；Phase 7：环境数据页 / 粮温汇总优化；Phase 6：overflow-x 收口；Phase 5：预测记录删除；Phase 4 单表打磨；Phase 2/3 归档见 zzz-docs/归档）*
