# Phase 6: 预测页过宽与主内容 overflow-x 收口 - Context

**Gathered:** 2026-04-10  
**Updated:** 2026-04-10（代码与文档同步）  
**Status:** 已与实现对齐（见 `06-01-PLAN.md` / `06-VERIFICATION.md`）

<domain>
## Phase Boundary

本阶段不再讨论“是否继续用 `el-scrollbar` 承载主内容滚动”这一大方向，而是在 **BUG-2026-04-10-001** 已止住“横向宽度持续增长”之后，收口其副作用：**主内容区被全局 `overflow-x: hidden` 禁掉后，真实超宽内容无法正常横向滚动；预测页右侧任务摘要在窄列 + 长时间文本下出现被直接裁切的视觉问题。** 本阶段目标是恢复“真实超宽时的正常横向滚动能力”，同时保持对 `el-scrollbar + ECharts + ResizeObserver` 反馈循环的规避，不把上个 bug 的高风险组合重新引回主内容层。

</domain>

<decisions>
## Implementation Decisions

### 布局层

- **D-01:** 主内容区继续使用 `ConsoleLayout.vue` 中的 **原生滚动容器** `.console-main-native`，**不**恢复 `el-scrollbar` 作为主内容滚动承载。
- **D-02:** `.console-main-native` 从 **`overflow-x: hidden`** 调整为 **`overflow-x: auto`**；小屏断点同样允许 `overflow-x: auto`，让真实超宽场景具备正常横向滚动，而非裁切。

### 组件层

- **D-03:** 预测记录表继续维持 **局部横向滚动**：由 `.prediction-history-table-wrap { overflow-x: auto; }` 承担；不依赖整页横向滚动来托底。
- **D-04:** 右侧任务摘要卡片增加**局部收口保护**：
  - 卡片 body 与摘要 wrapper 补 **`min-width: 0`**
  - 摘要表外包 `task-summary-table-wrap`
  - `el-descriptions__table` 使用 **`table-layout: fixed`**
  - 内容单元格允许 **换行 / breaking**
- **D-05:** 任务摘要应优先表现为“在窄列中稳定可读”，而不是必须通过整页横向滚动才能看到完整内容。

### 风险规避

- **D-06:** 继续保留上轮修复中对主内容层的规避：**不**把 `el-scrollbar` 放回主内容区，避免其更新链与 ECharts `resize` 再次叠加。
- **D-07:** 预测页图表仍沿用既有 `requestAnimationFrame` 后显式 `chart.resize(...)` 的做法，不在本阶段回退。

### Claude's Discretion

- `task-summary-table-wrap` 是否使用局部横向滚动或纯换行优先，只要满足“窄列不裁切、整页不误撑宽”即可。
- Phase 6 为布局收口，不扩展到删除逻辑、预测算法或其它页面视觉重构。

</decisions>

<canonical_refs>
## Canonical References

- `.planning/ROADMAP.md` — Phase 6 条目
- `.planning/REQUIREMENTS.md` — `OVERFLOW-06-*`
- `.planning/STATE.md` — 当前 quick / phase 记录
- `.planning/quick/260410-k32-vue-scrollbar-echarts-layout-fix/260410-k32-SUMMARY.md` — 上游 bug 的 quick 归档
- `frontend/src/layout/ConsoleLayout.vue`
- `frontend/src/styles.css`
- `frontend/src/views/PredictionView.vue`
- `.devflow/grain-platform-bootstrap/bug-log.md` — BUG-2026-04-10-001 / 002

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets

- 主内容滚动容器：`ConsoleLayout.vue` 中 `.console-main-native`
- 宽表局部滚动：`.prediction-history-table-wrap`
- 预测页图表稳定化：`PredictionView.vue` 中 `requestAnimationFrame` + `chart.resize`

### Integration Points

- 主内容层滚动策略在 `frontend/src/styles.css`
- 任务摘要结构在 `frontend/src/views/PredictionView.vue`
- 上游 bug 的风险描述与经验条目在 devflow / quick 工件

</code_context>

<specifics>
## Specific Ideas

- 用户明确诉求：恢复“正常页面真实超宽时的左右滚动”，但不回退到会触发上次无限变宽问题的方案。
- 用户同时指出：预测页当前截图里更像是任务摘要右侧被裁切，而不是底部表格的问题。

</specifics>

<deferred>
## Deferred Ideas

- 是否为其它后台页统一补“局部超宽组件 checklist”（表格 wrapper、卡片 body `min-width: 0` 等）
- 是否把这类布局约束总结进前端通用规范文档

</deferred>

---

*Phase: 06-overflow-x*  
*Context gathered: 2026-04-10; aligned with implementation: 2026-04-10*
