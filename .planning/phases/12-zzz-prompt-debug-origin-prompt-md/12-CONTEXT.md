# Phase 12: zzz-prompt-debug-origin-prompt-md - Context

**Gathered:** 2026-04-12
**Status:** Ready for planning

<domain>
## Phase Boundary

本阶段聚焦正式 Vue 前端里“图表时间轴过长、x 轴展示不全”的问题，收口用户明确要求保留并增强的三张核心图：

1. `frontend/src/views/DataView.vue` 中的 **粮温汇总趋势图**
2. `frontend/src/views/PredictionView.vue` 中的 **实际值 / 预测值双线图**
3. `frontend/src/views/BigScreenView.vue` 中的 **粮温趋势主图**

目标是在**不删图、不改页面主结构、不扩到 `frontend-next`** 的前提下，让这三张图都支持或继续复用用户可控的时间范围，并通过合理的默认时间窗口与图表交互兜底，解决首屏拥挤、标签过长和展示不全问题。

</domain>

<decisions>
## Implementation Decisions

### 范围与页面边界

- **D-01:** Phase 12 覆盖用户明确指定的三张图：`DataView.vue` 的粮温汇总趋势图、`PredictionView.vue` 的实际值 / 预测值双线图，以及 `BigScreenView.vue` 的粮温趋势主图。
- **D-02:** 本阶段不扩展为“全站所有 ECharts 图统一治理”，也不改 `frontend-next`。
- **D-03:** `BigScreenView.vue` 只收口**粮温趋势主图**；大屏里的预警趋势、预测趋势、仓库对比等其它图表不在本阶段范围内。

### 时间范围能力

- **D-04:** 上述三张图都必须建立在用户可控的时间范围之上，不能只靠标签旋转或缩写硬撑。
- **D-05:** 主要策略采用“**时间范围筛选 + 图表兜底交互**”的组合方案：先通过时间范围减少默认数据密度，再通过 ECharts 的标签抽样、旋转、省略或 `dataZoom` 处理剩余拥挤场景。
- **D-06:** 不采用“只保留全部历史，再完全依赖图表缩放交互”的方案；首屏可读性优先。

### 默认时间窗口

- **D-07:** 默认时间范围按页面职责区分，而不是全站统一一个窗口。
- **D-08:** `PredictionView.vue` 的双线图默认时间范围采用较短窗口，优先保证当前任务首屏可读；具体天数可由 planner 基于现有任务数据密度决定。
- **D-09:** `DataView.vue` 的粮温汇总趋势图默认时间范围可比预测图更长，以兼顾排查和趋势观察；具体天数可由 planner 结合现有汇总数据口径决定。
- **D-09a:** `BigScreenView.vue` 的粮温趋势主图不新增独立时间范围状态，继续复用大屏页面现有的共享 `screenFilters.timeRange` 查询口径。

### PredictionView 语义边界

- **D-10:** `PredictionView.vue` 的时间范围只影响**双线图显示范围**，不改当前任务摘要、任务本身的预测区间语义，也不重构下面的任务列表和历史记录口径。
- **D-11:** 用户看到的是“对当前任务图表的查看窗口筛选”，不是“重新定义任务本身的数据范围”。

### the agent's Discretion

- `DataView.vue` 与 `PredictionView.vue` 各自默认时间范围的具体数值，可在不违背 D-07 ~ D-11 的前提下由 planner / executor 结合真实数据量确定。
- 图表兜底交互使用 `axisLabel`、`interval`、`rotate`、`formatter`、`dataZoom` 中哪些组合，由后续规划决定，但目标必须是首屏先可读、长窗口仍可查看。

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### 原始诉求
- `zzz-prompt-debug/origin/优化图表/prompt.md` — Phase 12 原始问题描述：x 轴过长、展示不全，需要让图支持时间或时间范围选择

### 路线图与项目约束
- `.planning/ROADMAP.md` — Phase 12 条目、依赖关系与阶段边界
- `.planning/PROJECT.md` — 毕设 / MVP 约束，避免把图表修复扩成无关重构
- `.planning/REQUIREMENTS.md` — 现有已锁定能力边界，尤其是 Phase 7 与 Phase 8 的既有要求
- `.planning/STATE.md` — 当前 milestone 状态与最近 phase 演进

### 上游 phase 决策
- `.planning/phases/06-overflow-x/06-CONTEXT.md` — 主内容层 overflow 风险与“不回到高风险滚动方案”的约束
- `.planning/phases/07-zzz-prompt-debug-origin-prompt-md/07-CONTEXT.md` — `DataView.vue` 粮温汇总趋势图的既有查询边界与共享查询约束
- `.planning/phases/08-zzz-prompt-debug-origin/08-CONTEXT.md` — `BigScreenView.vue` 的既有边界，明确其不是本阶段必做范围

### 当前前端实现
- `frontend/src/views/DataView.vue` — 粮温汇总趋势图当前实现
- `frontend/src/views/PredictionView.vue` — 实际值 / 预测值双线图当前实现
- `frontend/src/views/BigScreenView.vue` — 展示大屏主图与共享时间范围当前实现
- `frontend/src/api/grain.js` — 粮温汇总、预测任务及相关前端 API 封装

### 当前后端实现
- `backend/src/main/java/com/grain/platform/controller/GrainTempController.java` — 粮温汇总图当前查询入口
- `backend/src/main/java/com/grain/platform/service/GrainTempService.java` — 粮温汇总时间序列服务逻辑
- `backend/src/main/java/com/grain/platform/controller/PredictionController.java` — 预测任务相关接口入口
- `backend/src/main/java/com/grain/platform/service/PredictionService.java` — 预测结果序列与任务详情事实源

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `frontend/src/views/DataView.vue` 已具备仓库 + 时间范围查询骨架，粮温汇总趋势图已有 ECharts 实现，可在现有卡片和查询区上增强。
- `frontend/src/views/PredictionView.vue` 已具备双线图与当前任务上下文，适合补一个“只影响图表”的时间范围状态，而不是重构整个页面。
- `frontend/src/api/grain.js` 已统一承接前端接口封装与数据归一化，新增或扩展时间范围参数应沿用这里的封装模式。

### Established Patterns
- 正式前端主栈是 `Vue 3 + Element Plus + ECharts`，页面继续沿用现有卡片、查询栏和 `chart.setOption()` 结构，不引入新图表库。
- 现有项目偏向“在现有页面内局部增强”，而不是为了单个体验问题拆新路由或大改组件树。
- 之前 Phase 6 已明确避开高风险的整页滚动回退方案，本阶段应继续保持这一风险边界。

### Integration Points
- `DataView.vue` 的粮温汇总趋势图需要在现有共享查询链路上补更明确的时间范围控制与 x 轴兜底策略。
- `PredictionView.vue` 的双线图需要增加图表级时间范围状态，并保证其只影响图，不回流污染当前任务其它区域。
- `BigScreenView.vue` 的粮温趋势主图需要继续复用现有大屏共享查询时间范围，并补和前两张图一致的 x 轴密度收口策略。
- 若现有后端接口不能直接支撑时间范围，需要在 `GrainTempController / GrainTempService / PredictionController / PredictionService` 现有链路上补参数，而不是另造旁路接口。

</code_context>

<specifics>
## Specific Ideas

- 用户明确要求必须保留并增强三张图：粮温汇总趋势图、预测页实际值 / 预测值双线图，以及展示大屏里的粮温趋势主图。
- 用户明确要求前两张图支持用户选择时间范围，大屏粮温趋势主图继续沿用页面已有共享时间范围。
- 用户接受推荐方案：范围收口到最直接受影响的正式页面；主要解法是“时间范围优先，图表交互兜底”；`PredictionView` 的时间范围只裁图、不改任务语义；`BigScreenView` 不新增第二套查询状态。

</specifics>

<deferred>
## Deferred Ideas

- 正式前端所有 ECharts 图统一排查和统一治理
- `BigScreenView.vue` 其它图表（预警趋势、预测趋势、仓库对比）的统一 x 轴收口
- `frontend-next` 的图表一致性治理
- 将图表时间范围与页面其余查询条件做更深度的一体化重构

</deferred>

---

*Phase: 12-zzz-prompt-debug-origin-prompt-md*
*Context gathered: 2026-04-12*
