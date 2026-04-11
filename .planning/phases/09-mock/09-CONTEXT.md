# Phase 9: 管理端列表去 mock 并收口服务端分页查询 - Context

**Gathered:** 2026-04-11
**Status:** Ready for planning

<domain>
## Phase Boundary

本阶段聚焦前端管理端剩余的“前端内存分页 / 前端本地过滤 / 残留 mock”收口工作，不新增业务能力。范围包括：

1. `UsersView.vue`
2. `WarehouseView.vue`
3. `PredictionView.vue`
4. `DashboardView.vue`
5. 未使用的 `frontend/src/mock/platform.js`

目标是把这些页面统一收口到“后端分页 + 后端 keyword 查询 + 前端按页请求”的模式，并删除已经不参与运行时的 mock 文件。`DataView.vue` 当前已完成的服务端分页范式不重做；`/screen` 大屏 Phase 8 已完成真实化，本阶段不回退也不重谈其 mock。

</domain>

<decisions>
## Implementation Decisions

### 范围与覆盖页面

- **D-01:** 本阶段覆盖 `UsersView.vue`、`WarehouseView.vue`、`PredictionView.vue`、`DashboardView.vue` 四个仍存在前端分页/前端过滤的页面，并同步清理未使用的 `frontend/src/mock/platform.js`。
- **D-02:** `DataView.vue` 不纳入功能性重构，因为它已经是本仓库服务端分页、后端筛选、前端分页状态联动的既有范式；Phase 9 以它为参考，不推倒重做。
- **D-03:** `/screen` 不纳入本阶段范围；Phase 8 已经把大屏从 mock/演示型页面收口到真实接口驱动，本阶段不重复改造。

### 查询与分页口径

- **D-04:** 用户、仓库、预测任务、Dashboard 下的分页列表统一采用“**一个关键词搜索框 + 后端 keyword + pageNum/pageSize**”的轻量查询形态，不在本阶段扩展为复杂高级筛选面板。
- **D-05:** keyword 只覆盖当前表格或列表里**已经展示给用户的主要字段**，保证实现成本和答辩解释都保持简单直接；更细的结构化筛选留待后续阶段。
- **D-06:** 前端在切页、改 pageSize、输入 keyword 后，应重新向后端请求当前页数据，而不是继续对全量数组执行 `filterRows()` / `slice()` / `useClientPagination()`。

### PredictionView 列表接口

- **D-07:** `PredictionView.vue` 的预测任务列表继续使用任务级完整详情结构，列表接口**仍返回完整任务详情**（包括当前页面已依赖的完整任务字段和 `resultList`），本阶段只新增分页参数和后端分页能力，不拆成“轻量列表 + 单独详情接口”两段式。
- **D-08:** 现有“查看摘要 / 切换任务 / 删除 / 批量删除”交互保持不变；Phase 9 只调整列表数据获取与分页来源，不重新设计任务详情交互。
- **D-09:** Phase 4 已锁定“预测记录数据源以后端持久化为准、不是前端 mock”，本阶段必须延续该原则，只做服务端分页化，不允许回退到前端本地任务数组方案。

### DashboardView 收口方式

- **D-10:** `DashboardView.vue` 也纳入本阶段，不能继续维持“`fetchOverview()` 返回大包数组，前端再本地分页/增量加载”的模式。
- **D-11:** Dashboard 的顶部概览指标可以继续保留聚合接口语义，但“近期预警”“仓库运行健康度”“最新粮温汇总”三块列表/表格需要改为**独立后端接口**，分别支持分页与 keyword。
- **D-12:** Dashboard 页面改造后，前端应以“概览聚合 + 多个分页接口并行请求”的模式运行，而不是把所有列表都塞回单一 overview 响应里。

### Mock 清理

- **D-13:** `frontend/src/mock/platform.js` 在确认没有运行时引用后直接删除，不保留为运行时备用数据或新的 fixtures 目录。
- **D-14:** 本阶段“去 mock”的含义是删除当前未使用的前端残留 mock 文件，并确保相关页面完全以后端真实接口为准；不要求建立新的演示假数据体系。

### the agent's Discretion

- 用户、仓库、预测任务、Dashboard 各分页接口的命名与 DTO 形状，只要符合当前 Spring Boot + MyBatis + `ApiResponse` / `PageResult` 体系即可。
- 前端页面是否保留 `filterRows` 作为极小范围的本地兜底能力，可由 planner 结合实际依赖决定；但分页主链必须以后端返回为准。
- `useClientPagination.js` 和 `useIncrementalList.js` 是否仍保留给其他未改页面使用，由 planner 根据实际引用决定；本阶段不要求为了“零 unused”额外扩大战线。

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### 路线图与项目约束

- `.planning/ROADMAP.md` — Phase 9 条目与当前 milestone 位置
- `.planning/PROJECT.md` — 毕设 / 数据库优先 MVP 的边界，要求避免无关大重构
- `.planning/REQUIREMENTS.md` — 既有 Phase 4、7、8 的已锁定行为与服务端分页演进基线
- `.planning/STATE.md` — Phase 9 加入 roadmap 的原因和本轮检查结论

### 前置阶段上下文

- `.planning/phases/04-prediction-page-polish/04-CONTEXT.md` — 预测记录列表必须以后端接口与持久化为准，非前端 mock
- `.planning/phases/05-prediction-record-delete/05-CONTEXT.md` — PredictionView 当前任务级操作列、多选删除与任务状态一致性约束
- `.planning/phases/07-zzz-prompt-debug-origin-prompt-md/07-CONTEXT.md` — `DataView.vue` 服务端分页与共享查询态的最新模式
- `.planning/phases/08-zzz-prompt-debug-origin/08-CONTEXT.md` — `/screen` 已确立“能真实就真实”的去 mock 原则

### Devflow 真相源

- `.devflow/grain-platform-bootstrap/state.md` — 管理端列表统一、数据页后端分页与数据库优先 MVP 的阶段事实
- `.devflow/grain-platform-bootstrap/handoffs/2026-04-10-022-pause-ready-after-layout-grain-filter-handoff-commit.md` — “结构化条件走后端 SQL”的既有决策

### 代码与实现入口

- `frontend/src/views/UsersView.vue` — 当前用户页前端分页 / 本地过滤实现
- `frontend/src/views/WarehouseView.vue` — 当前仓库页前端分页 / 本地过滤实现
- `frontend/src/views/PredictionView.vue` — 当前预测记录页前端分页实现与任务详情依赖
- `frontend/src/views/DashboardView.vue` — 当前首页 overview 聚合后本地分页 / 增量加载实现
- `frontend/src/views/DataView.vue` — 已落地的服务端分页参考范式
- `frontend/src/api/grain.js` — 当前 API 封装、`normalizePageResult`、列表接口入口
- `frontend/src/mock/platform.js` — 当前残留 mock 文件，Phase 9 目标之一
- `backend/src/main/java/com/grain/platform/common/PageResult.java` — 统一分页返回结构
- `backend/src/main/java/com/grain/platform/controller/UserController.java` — 用户列表接口入口，当前为全量返回
- `backend/src/main/java/com/grain/platform/controller/WarehouseController.java` — 仓库列表接口入口，当前为全量返回
- `backend/src/main/java/com/grain/platform/controller/PredictionController.java` — 预测任务列表接口入口，当前为全量返回
- `backend/src/main/java/com/grain/platform/controller/DashboardController.java` — Dashboard overview / screen 聚合接口入口

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets

- `frontend/src/views/DataView.vue` 已经完整实现了 `pageNum`、`pageSize`、`total`、查询条件、翻页回源、后端 `PageResult` 对接，是本阶段最直接的前端参考。
- `frontend/src/api/grain.js` 中的 `normalizePageResult()`、`fetchSensorData()`、`fetchGrainTempSummaryPage()`、`fetchGrainTempRecords()` 已经定义了当前仓库对分页接口的前端接入模式。
- `backend/src/main/java/com/grain/platform/common/PageResult.java` 以及 `GrainTempController` / `SensorDataController` 的分页写法，是用户、仓库、预测任务、Dashboard 新分页接口的后端参考。

### Established Patterns

- 前端统一通过 `grain.js` + `request()` 调后端，页面内只维护分页状态和查询状态，不直接拼接低层请求逻辑。
- 后端当前已经有明确的“控制器收 query 参数 -> service 计算 pageNum/pageSize/offset -> `PageResult<T>` 返回”的实现模式。
- `PredictionView.vue` 已经把“任务列表行数据”和“当前选中任务详情”耦合在一个完整任务对象结构上，因此本阶段不拆轻量 DTO。

### Integration Points

- `UsersView.vue` 需要从“全量 `fetchUsers()` + `useClientPagination`”切到“后端分页列表 + 后端 keyword”。
- `WarehouseView.vue` 需要从“全量 `fetchWarehouses()` + `useClientPagination`”切到分页列表，同时保留当前右侧详情联动。
- `PredictionView.vue` 需要在不破坏删除、多选、摘要、切换任务行为的前提下，把 `fetchPredictionTasks()` 改成分页接口。
- `DashboardView.vue` 需要从单一 `fetchOverview()` 聚合数组改成“概览聚合 + 独立分页接口”并行加载。

</code_context>

<specifics>
## Specific Ideas

- 用户明确要求：**DashboardView 也纳入本阶段**，不能把首页留在前端内存分页模式。
- 用户明确要求：**预测任务列表继续返回完整任务详情，只是新增分页参数**，不拆详情接口。
- 用户明确同意其余推荐项：`keyword + pageNum/pageSize` 轻量查询、直接删除残留 mock 文件。
- 本阶段的真实目标不是“为了代码洁癖统一所有页面”，而是把当前还能看见的管理端前端分页/残留 mock 收口到数据库优先 MVP 的一致口径。

</specifics>

<deferred>
## Deferred Ideas

- 用户、仓库、预测任务页面若未来需要更细的结构化筛选（状态、角色、风险等级、时间范围等），应作为后续阶段单独规划。
- 若未来预测任务列表需要显著降载，再评估拆成“轻量列表 DTO + 详情接口”的两段式。
- Dashboard 是否进一步拆成更多专用统计接口或缓存层，不在本阶段讨论。
- `useClientPagination.js` / `useIncrementalList.js` 是否彻底退役，取决于本轮改造后的剩余引用，不单独扩大为清理工程。

</deferred>

---

*Phase: 09-mock*
*Context gathered: 2026-04-11*
