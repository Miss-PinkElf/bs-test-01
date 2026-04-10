# Phase 4 UI设计约定（prediction-page-polish）

**Status:** 与 `04-CONTEXT.md`、当前 `PredictionView.vue` 对齐  
**页面:** `frontend/src/views/PredictionView.vue`

## 信息架构（已更新）

- **底部仅一张表 — 「预测记录」**：全宽（`el-col :span="24"`）；**任务级一行一条**，合并原「历史归档」心智；**不再**单独展示左侧「预测结果列表」分步表。
- **参数区 + 任务摘要 + 双线图**：与 Phase 2/3 一致；曲线对应当前选中任务的 `resultList`（数据仍来自后端详情/列表项中的结果合并逻辑）。

## 操作列（预测记录表）

| 操作 | 行为（与 CONTEXT D-03～D-06 一致） |
|------|-----------------------------------|
| 切换任务 | `selectPredictionTask(row)`：更新摘要、曲线；曲线区 `scrollIntoView` 保持 Phase 3。 |
| 查看摘要 | `focusTaskSummary(row)`：必要时先切换任务，再滚至任务摘要卡片并短暂 `prediction-summary-flash`。 |

## 列定义

- **预测区间**：`forecastStartTime` ~ `forecastEndTime`（`formatDateTime`）；搜索过滤包含该列文本。
- **任务号、预测对象、仓库、风险等级、预测天数、执行时间**：保持可读展示。
- **操作**：固定右侧，link 主色按钮。

## 样式

- 遵循 `panel-card`、`panel-title`、`panel-subtitle`、`page-stack`；强调类在 `frontend/src/styles.css`，禁止行内样式。

## 数据源

- 表数据：**非 mock**；`fetchPredictionTasks()` → `GET /api/predictions/tasks`，后端 `PredictionService` 读库。

## 参考

- `zzz-prompt-debug/origin/优化温度预测页/prompt.md`
