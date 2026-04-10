# Phase 3: 温度预测页交互二次优化 - Context

**Gathered:** 2026-04-10  
**Status:** Ready for planning（实现已先行落地，本文档为追溯对齐；可与 `/gsd-plan-phase 3` 补 PLAN归档）

<domain>
## Phase Boundary

在 Phase 2 已交付的预测页能力之上，优化**主流程可达性**与**双表语义**：执行预测后曲线尽快进入视野；用户能区分「当前任务分步明细」与「历史任务列表」及点击切换行为。不新增后端能力、不合并两张业务含义不同的表。补充 **`md` 起栅格并排**，避免仅配置 `xl` 导致常见笔记本宽度下参数与摘要误堆为上下布局（UX2-03）。
</domain>

<decisions>
## Implementation Decisions

### 页面区块顺序（UX2-01）

- **D-01:** 纵向顺序固定为：**滚动预测参数 + 任务摘要（同排）→实际值/预测值双线图 → 预测结果列表 + 历史归档记录（同排）**。曲线在明细双表之上，避免「先滚很长才看到主视觉」。

### 滚动与反馈

- **D-02:** **`runPrediction`** 成功并完成 `renderChart` 后，对 **`chartAnchorRef`** 执行 **`scrollIntoView({ behavior: "smooth", block: "start" })`**。
- **D-03:** **`selectPredictionTask`**（点击历史表行）在 `renderChart` 后执行 **`scrollIntoView({ behavior: "smooth", block: "nearest" })`**，减少切换任务后找不到曲线的情况。

### 文案与信息架构（UX2-02）

- **D-04:** 图表卡片标题下使用 **`panel-subtitle`**，说明对应当前任务摘要，并提示执行预测后会滚到此处。
- **D-05:** 「预测结果列表」副标题说明：当前任务的 ACTUAL/FUTURE 分步明细，与曲线一致。
- **D-06:** 「历史归档记录」副标题说明：历次任务，**点击行**加载任务并同步摘要、曲线、左侧明细。

### 响应式栅格（UX2-03）

- **D-07:** 首行与次行两列均采用 **`:xs="24" :sm="24"`**（窄屏叠放）+ **`:md="15" :lg="15" :xl="15"`** 与 **`:md="9" :lg="9" :xl="9"`**（≥`md` 起15:9 并排）。禁止仅依赖 `:xl`，因 Element Plus 默认 **`xl` 断点对应约 1920px**，会导致笔记本宽度下两列均为整行、摘要掉到参数下方。

### the agent's Discretion

- `panel-subtitle` 具体措辞可随答辩反馈微调，需保持「两表差异 + 历史表可点击」不丢失。
- 若未来需在768～991px 也强制并排，可再评估 `sm` 栅格（当前刻意小屏叠放）。

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### 本阶段

- `.planning/phases/03-prediction-ux-pass2/ALIGNMENT.md` — 问题、目标、验收、落实记录
- `.planning/REQUIREMENTS.md` — UX2-01、UX2-02、UX2-03
- `.planning/ROADMAP.md` — Phase 3 Goal 与 Success criteria

### 上游与代码

- `.planning/phases/02-prediction-view-ux/02-CONTEXT.md` — Phase 2 已定能力（摘要、训练区间、错误提示等）
- `frontend/src/views/PredictionView.vue` — 唯一主实现文件
- `frontend/src/styles.css` — `.panel-subtitle`、`.prediction-*` 辅助类

</canonical_refs>

<code_context>
## Existing Code Insights

### 关键 refs / 行为

- `chartRef`：ECharts 挂载点；`chartAnchorRef`：滚动锚点（包在图表卡片外层的 `div.prediction-chart-anchor`）。
- 训练区间、摘要结构、API 调用等延续 Phase 2 实现，Phase 3 不重复改造除非回归缺陷。

### Established Patterns

- 卡片标题区：主标题 `panel-title` + 次要说明 `panel-subtitle`（全局样式）。
- 页面栈：`page-stack` + `gap: 16px`。

### Integration Points

- 预测 API不变：`frontend/src/api/grain.js` → `POST /api/predictions`。

</code_context>

<specifics>
## Specific Ideas

- 用户要求：任务摘要与参数在桌面宽度下**左右并排**，点击预测即可在同屏看到摘要更新；根因之一为栅格断点仅 `xl`，已在 UX2-03 修正。

</specifics>

<deferred>
## Deferred Ideas

- 窄屏下双表改为 Tab — 未纳入 Phase 3；见 Phase 2 CONTEXT deferred。
- 多算法 — `REQUIREMENTS.md` Out of Scope。

</deferred>

---

*Phase: 03-prediction-ux-pass2*  
*Context gathered: 2026-04-10*
