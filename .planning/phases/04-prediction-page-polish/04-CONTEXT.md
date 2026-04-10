# Phase 4: 温度预测页再打磨（prediction-page-polish）- Context

**Gathered:** 2026-04-10  
**Updated:** 2026-04-10（单表合并与数据源说明）  
**Status:** 已与实现对齐（见 `04-01-PLAN.md` / `PredictionView.vue`）

<domain>
## Phase Boundary

在 Phase 2/3 已交付的预测页上，减轻「割裂」与双表**感知重复**。**2026-04-10 产品决策：** 底部**只保留一张任务级表**（标题 **「预测记录」**），合并原先「预测结果列表（分步明细）」与「历史归档（任务一行）」在心智上的重复；分步明细不再单独占一张表，**曲线仍表示当前选中任务**（与后端返回的 `resultList` 一致）。**数据源：** 列表与任务字段来自后端 **`GET /api/predictions/tasks`**，任务由 **`POST /api/predictions`** 落库（`PredictionService` + Mapper），**不是前端 mock**。
</domain>

<decisions>
## Implementation Decisions

### 单表与列

- **D-01:** **合并**底部双表为单一 **「预测记录」**表：`el-col :span="24"` 全宽；每行对应**一次预测任务**（原历史归档语义）。**移除**原左侧「预测结果列表」整块 UI（分步 ACTUAL/FUTURE 多行表）；用户若需逐步细节，以**双线图 + 任务摘要**为主（明细数据仍在接口 `resultList` 中，仅不再单独表格展示）。
- **D-02:** 表增加 **「预测区间」**列：展示 `forecastStartTime` ~ `forecastEndTime`（与 `normalizePredictionTask` /后端 `PredictionTaskResponse` 一致）；筛选关键字包含该列格式化后的文本。
- **D-03:** 保留 **操作列**：**查看摘要**、**切换任务**；行为与 Phase 4 前一版一致（复用 `selectPredictionTask` / `focusTaskSummary`）。

### 交互（点击行为）

- **D-04:** **取消**任务表 **`@row-click` / 整行点击** 切换；仅按钮触发（`@click.stop`）。
- **D-05:** **当前选中任务**行高亮：继续 `resolveHistoryRowClassName` + `is-active-row`；`.interactive-table-row` 使用 `cursor: default`，避免暗示整行可点。

### 查看摘要

- **D-06:** **「查看摘要」**：滚至 **任务摘要** `el-card`，`taskSummaryCardRef` + `prediction-summary-flash` 短暂强调（与前一版一致）。

### 后端与信任边界

- **D-07:** 本页列表展示字段以后端 **`PredictionService.listTasks()`**（`prediction_task` / `prediction_result` 等持久化）为准；前端仅调用 `grain.js` 中 `/api/predictions`、`/api/predictions/tasks`，不在页面内维护独立 mock 任务列表。

### Claude's Discretion

- 「预测记录」表副标题、搜索框 placeholder 文案。
- 若未来需恢复分步明细，可另开阶段（Drawer / 展开行 / 子表），本阶段明确不做。

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### 需求与参考

- `zzz-prompt-debug/origin/优化温度预测页/prompt.md` — 原始诉求：样式/交互友好、割裂感、时间范围列等
- `zzz-prompt-debug/origin/优化温度预测页/image.png` — 布局/视觉参考（若路径不存在以仓库实际为准）

### 阶段与契约

- `.planning/ROADMAP.md` — Phase 4 条目与 Depends on Phase 3
- `.planning/REQUIREMENTS.md` — **POLISH-04-***（本阶段）；PRED-*、UX2-*（Phase 2/3 基线，部分文案由 Phase 4 演进替代）
- `.planning/phases/03-prediction-ux-pass2/03-CONTEXT.md` — Phase 3：区块顺序、图表锚点等（底部表结构由 Phase 4 调整）

### 代码（后端事实源）

- `backend/src/main/java/com/grain/platform/controller/PredictionController.java` — `/api/predictions`、`/api/predictions/tasks`
- `backend/src/main/java/com/grain/platform/service/PredictionService.java` — 预测落库、`listTasks()` 读库
- `frontend/src/api/grain.js` — `predictMetric`、`fetchPredictionTasks`、`normalizePredictionTask`
- `frontend/src/views/PredictionView.vue` — 预测页主实现
- `frontend/src/styles.css` — `prediction-summary-flash` 等

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets

- 单一任务表：`historyPagination`、`filteredPredictionHistory`、`selectPredictionTask`、`focusTaskSummary`、`resolveHistoryRowClassName`。
- 摘要区：`el-descriptions`（训练区间、预测区间等仍展示当前 `prediction`）。

### Established Patterns

- 卡片：`panel-card`、`panel-title`、`panel-subtitle`；页面 `page-stack`。
- 切换任务后：`renderChart`、`chartAnchorRef` 滚动（Phase 3 行为保留）。

### Integration Points

- 列表：`GET /api/predictions/tasks`；新预测：`POST /api/predictions`。字段不足时再评估后端 DTO（当前含 `forecastStartTime` / `forecastEndTime`）。

</code_context>

<specifics>
## Specific Ideas

- 用户明确：**单表** + **预测区间列**；**数据在后端有**、非 mock。

</specifics>

<deferred>
## Deferred Ideas

- 对照 `image.png` 的细粒度样式走查。
- 若需**分步明细**再次可见：展开行、侧栏或独立阶段。
- Phase 3 已记的 **窄屏双表 Tab**：当前底部仅单表，优先级下降。

### Reviewed Todos (not folded)

- `todo match-phase 4` 无匹配项。

</deferred>

---

*Phase: 04-prediction-page-polish*  
*Context gathered: 2026-04-10; aligned with implementation: 2026-04-10*
