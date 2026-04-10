# Phase 7: zzz-prompt-debug-origin-prompt-md - Context

**Gathered:** 2026-04-10
**Status:** Ready for planning

<domain>
## Phase Boundary

本阶段只优化 `frontend/src/views/DataView.vue` 中 **粮温主线** 的两块内容：

1. **粮温汇总趋势图**
2. **粮温汇总结果**

目标是把当前“图表查询维度太少、汇总表仍为前端内存分页”的状态，升级为适合演示和日常排查的查询页：**汇总结果改为后端分页 + 搜索/筛选，趋势图支持仓库、时间范围、层级目标选择**。普通环境模式、导入弹窗、原始测点记录 CRUD、预测页等不在本阶段范围内。

</domain>

<decisions>
## Implementation Decisions

### 范围与页面边界

- **D-01:** 本阶段只动 **粮温主线** 下的“汇总趋势图 + 汇总结果”两块，**普通环境模式不做功能性改造**；若为了统一视觉需要做少量样式对齐，可做不改行为的小修，但不得把 Phase 7 扩成整页重做。
- **D-02:** 页面继续保留 `DataView.vue` 当前的主结构与卡片分区，不另开新页面、不拆成独立路由；优化以现有 `panel-card`、`toolbar-row`、ECharts + Element Plus 表格体系为基础。

### 汇总结果表

- **D-03:** `粮温汇总结果` 改为 **后端分页**，不再使用当前前端 `useClientPagination` 对全量汇总结果做内存分页。
- **D-04:** 汇总表查询口径采用“两层结构”：
  - **共享查询条件：** 仓库、检测时间范围
  - **表格专属条件：** 关键词、预警等级、温度范围
- **D-05:** `关键词` 仍服务于模糊检索，覆盖仓库、预警等级、温度相关展示值、时间等当前用户能直接看到的汇总字段。
- **D-06:** `温度范围` 作为 **单一数值区间条件** 保留在汇总表筛选区，默认作用于 **整仓均温**，避免把筛选面板扩成多组温度条件。

### 趋势图

- **D-07:** 趋势图新增查询维度：**仓库**、**时间范围**、**层级目标**。
- **D-08:** 层级目标采用 **单选模式**，候选至少包含：`整仓均温`、`一层均温`、`二层均温`、`三层均温`、`四层均温`；默认值为 **整仓均温**。
- **D-09:** 图表固定保留 **最高温** 作为对照线；即页面展示为“当前所选目标主线 + 最高温参考线”，而不是开放任意多选多线对比。

### 图表与表格联动

- **D-10:** 图表与汇总表 **共享同一组仓库 + 时间范围条件**，保证用户看到的曲线与表格来自同一数据窗口；不做图表、表格各自独立的查询状态。
- **D-11:** 表格专属的 `关键词 / 预警等级 / 温度范围` 只影响汇总表，不反向影响图表曲线。

### the agent's Discretion

- 后端接口是扩展现有 `GET /api/grain-temp/summaries` 还是新增分页接口，只要前后端契约清晰且不破坏当前页面其它调用即可。
- 共享查询区与表格专属筛选区的具体排版、是否拆成两行工具栏、按钮文案与交互节奏。
- 时间范围默认值、是否允许空值查询全部历史、以及查询触发方式（显式“查询”按钮或局部防抖）在不违背 D-10 / D-11 的前提下可由 planner 决定。

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### 原始诉求与视觉参考

- `zzz-prompt-debug/origin/环境数据页面优化/prompt.md` — Phase 7 的原始诉求：汇总表分页、搜索筛选、温度范围、趋势图按时间/仓库/层级切换
- `zzz-prompt-debug/origin/环境数据页面优化/image.png` — 当前页面效果截图，供 planner / executor 对照优化前状态

### 阶段与项目约束

- `.planning/ROADMAP.md` — Phase 7 条目与当前 milestone 依赖关系
- `.planning/PROJECT.md` — 毕设/MVP 范围约束，避免把页面优化扩成大重构
- `.planning/STATE.md` — 当前里程碑与近期阶段演进记录

### 现有前端实现

- `frontend/src/views/DataView.vue` — 环境数据页主实现；当前粮温/普通环境模式、图表、汇总结果、原始记录都在这里
- `frontend/src/api/grain.js` — `fetchGrainTempSummaries`、`fetchGrainTempRecords`、`fetchWarehouses` 等接口封装；当前汇总表与记录表的数据入口

### 现有后端事实源

- `backend/src/main/java/com/grain/platform/controller/GrainTempController.java` — 粮温汇总与记录查询接口入口
- `backend/src/main/java/com/grain/platform/service/GrainTempService.java` — 汇总列表、原始记录分页、预测序列读取逻辑
- `backend/src/main/java/com/grain/platform/mapper/GrainTempSummaryMapper.java` — 粮温汇总 Mapper 接口
- `backend/src/main/resources/mapper/GrainTempSummaryMapper.xml` — 当前汇总查询 SQL；现阶段只有按仓库/时间范围查全量列表
- `backend/src/main/java/com/grain/platform/mapper/GrainTempRecordMapper.java` — 原始测点分页与多条件筛选接口，可复用其分页模式
- `backend/src/main/resources/mapper/GrainTempRecordMapper.xml` — 原始测点分页、多条件筛选、关键词查询 SQL 参考

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets

- `DataView.vue` 已有 `filters.warehouseId`、`grainCollectedRange`、`grainRecordFilters`、`handleSearch`、`renderChart`，说明页面已经具备“共享查询状态 + 图表刷新 + 表格刷新”的基本骨架。
- 原始测点记录已实现 `PageResult` 驱动的服务端分页、多条件筛选与关键词查询，可直接复用到汇总表这条链路。
- 粮温趋势图已基于 ECharts 实现，只需把数据源从“全量 summaries + 固定两条线”升级为“可选目标 + 共享过滤条件”。

### Established Patterns

- 前端接口层统一走 `frontend/src/api/grain.js` 的 `request()` 封装与数据 normalize 逻辑。
- 后端分页模式统一使用 `PageResult<T>`，并在 service 中做 `count + offset + limit` 计算。
- 当前 `DataView.vue` 采用“顶部查询卡片 + 独立业务卡片 + 表格分页”的管理台布局，Phase 7 应沿用这一交互语言。

### Integration Points

- 汇总表链路需要在 `GrainTempController` / `GrainTempService` / `GrainTempSummaryMapper(.xml)` 增加服务端分页与筛选能力。
- 图表链路会依赖 `grain_temp_summary` 的时间窗口查询能力；层级目标切换可直接复用汇总表已有字段 `avgTemp`、`layer1Avg` ~ `layer4Avg`、`maxTemp`。
- `DataView.vue` 当前同时承载粮温与普通环境模式，实施时要确保 Phase 7 的改动不回归普通环境数据页现有 CRUD / 趋势行为。

</code_context>

<specifics>
## Specific Ideas

- 推荐决策已确认：`1A 2A 3B 4A`
- 用户希望的最终体验是：**图表和表格围绕同一组仓库/时间窗口看数据**，但表格比图表再多一层“查问题记录”的筛选能力。
- 截图显示当前页面已经是“趋势图在上、汇总表在下”的结构，本阶段是在该结构上强化查询与可读性，而不是推翻重排。

</specifics>

<deferred>
## Deferred Ideas

- 普通环境模式的查询区、趋势图与记录表统一重做
- 汇总图支持任意多线多选对比
- 将汇总表筛选扩展为多组温度字段（例如最高温/最低温/各层均温分别独立筛）
- 拆分 `DataView.vue` 为多个子组件或独立路由

</deferred>

---

*Phase: 07-zzz-prompt-debug-origin-prompt-md*
*Context gathered: 2026-04-10*
