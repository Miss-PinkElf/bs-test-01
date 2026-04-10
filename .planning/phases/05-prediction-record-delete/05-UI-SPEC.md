# Phase 5 UI 设计约定（prediction-record-delete）

**Status:** 与 `05-CONTEXT.md` 对齐  
**页面:** `frontend/src/views/PredictionView.vue`  

**继承:** `04-UI-SPEC.md` 中单表「预测记录」信息架构与 Phase 4 操作列行为。

## 预测记录表 — 多选与工具栏

- **多选列**：`el-table` **最左侧**第一列为 **`type="selection"`**（在「任务号」等数据列之前）。
- **表格勾选状态**与 **当前查看任务**（`selectedTaskId` / 曲线与摘要）**分离**：勾选仅用于批量删除；实现可用独立 ref（如 `historyTableRef` + `historySelectedRows`），避免与「切换任务」心智混淆。
- **工具栏**：在现有搜索框同一 `toolbar-row`（或紧邻）增加 **`批量删除`** 按钮：
  - **`type="danger"`**
  - **无勾选时禁用**（推荐）或点击时 `ElMessage.warning`，项目内择一并统一。

## 操作列（在 Phase 4 基础上扩展）

| 操作 | 行为 |
|------|------|
| 查看摘要 | 同 Phase 4：`focusTaskSummary` |
| 切换任务 | 同 Phase 4：`selectPredictionTask` |
| **删除** | `danger` **link**，`@click.stop`；先 **`ElMessageBox.confirm`**（不可恢复），再调单删 API |

## 批量删除

- 点击 **批量删除**前：**`ElMessageBox.confirm`**，文案须包含 **已选条数**（至少数量，可附任务号摘要）。
- 成功后：`ElMessage.success`；**清空表格勾选**；`loadPredictionHistory`；若删除集合含当前 `selectedTaskId`，按 CONTEXT **清空主视觉**。

## 样式

- 遵循 `panel-card`、`table-toolbar`、无行内样式；全局样式仍在 `frontend/src/styles.css`。

## 数据源

- 列表仍 **`GET /api/predictions/tasks`**；删除 **`DELETE /api/predictions/tasks/{id}`**、**`POST /api/predictions/tasks/batch-delete`**。

## 参考

- `.planning/phases/05-prediction-record-delete/05-CONTEXT.md`
- `.planning/phases/04-prediction-page-polish/04-UI-SPEC.md`
