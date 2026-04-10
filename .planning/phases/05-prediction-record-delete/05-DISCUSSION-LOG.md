# Phase 5: 预测记录删除 - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.  
> Decisions are captured in `05-CONTEXT.md`.

**Date:** 2026-04-10  
**Phase:** 05-prediction-record-delete  
**Areas discussed:** 删除语义、单条/批量 API、表前多选与工具栏、二次确认（含批量条数）、删除后 UI、权限

---

## 同步记录（2026-04-10）

用户修订产品范围后，已更新 `05-CONTEXT.md`，本日志与之一致：

- **表前多选列**（`type="selection"`，独立于 `selectedTaskId`）。
- **工具栏「批量删除」**（`danger`，无勾选时禁用或等价提示）。
- **操作列单条「删除」**。
- **所有删除**均 **`ElMessageBox.confirm`**；**批量确认框须含选中数量**。
- **后端**：`DELETE /api/predictions/tasks/{taskId}` + `POST /api/predictions/tasks/batch-delete`（`taskIds` 数组，单事务）。

---

## 删除语义与一致性

| Option | Description | Selected |
|--------|-------------|----------|
| 物理删除任务 + 关联结果 | 符合「清脏数据」、实现直接 | 是 |
| 仅软删除 | 需新列与列表过滤规则 | |
| 仅删任务保留结果 | 数据不一致 | |

**Notes:** 批量与单条均在服务层事务内处理；部分 id 无效时倾向整批回滚（见 CONTEXT D-02）。

---

## API 形态

| Option | Description | Selected |
|--------|-------------|----------|
| `DELETE /api/predictions/tasks/{taskId}` | 单条删除 | 是 |
| `POST /api/predictions/tasks/batch-delete` + `taskIds` | 批量删除 | 是 |
| `POST .../delete` body 替代 RESTful | 未采用 | |

---

## 表前多选与批量删除入口

| Option | Description | Selected |
|--------|-------------|----------|
| `el-table` 首列 selection + 工具栏批量删除 | 与 CONTEXT D-04 / D-04b一致 | 是 |
| 仅操作列逐条删 | 已扩展为「多选 + 批量」 | |

---

## 二次确认

| Option | Description | Selected |
|--------|-------------|----------|
| 单删 / 批量删均 `MessageBox.confirm`；批量展示数量 | 强制 | 是 |
| 直接删无确认 | 不允许 | |

---

## 删除当前选中任务时

| Option | Description | Selected |
|--------|-------------|----------|
| 若删到当前 `selectedTaskId`，清空主视觉与图表 | 避免已删任务仍展示 | 是 |
| 自动切到列表第一项 | 未采纳 | |

---

## Claude's Discretion

- MyBatis 批量删除实现、操作列宽度、分页与多选、删后页码回退。

## Deferred Ideas

- 按条件清理、软删除、跨页全选、仪表盘统计联动（见 `05-CONTEXT.md` `<deferred>`）
