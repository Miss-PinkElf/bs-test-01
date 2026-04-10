---
status: pending
phase: 05-prediction-record-delete
verified: null
source:
  - .planning/phases/05-prediction-record-delete/05-01-PLAN.md
  - .planning/phases/05-prediction-record-delete/05-CONTEXT.md
---

# Phase 05 目标验证

## Phase 目标

预测记录表 **多选列** + **工具栏批量删除** + 操作列 **单删**；**全部删除路径二次确认**（批量确认含 **数量**）；后端 **DELETE 单条** + **POST batch-delete**；删后刷新列表、清空勾选、删到当前任务时清空主视觉。

## must_haves.truths（对照 PLAN）

| 条目 | 证据（实现后填写） |
|------|-------------------|
| selection 列 + 批量删除按钮 + 无选禁用策略 | `PredictionView.vue` |
| 单删/批量删均有 confirm；批量含条数 | 同上 + `ElMessageBox` |
| 后端路由与事务删除 | `PredictionController` / `PredictionService` / Mapper XML |
| 删后状态与 `grain.js` 封装 | `PredictionView.vue` / `grain.js` |

## 自动化

- （执行后）`mvn -DskipTests compile`、`npm run build`

## 需求追溯

- `DEL-05-01`、`DEL-05-02`（`.planning/REQUIREMENTS.md`）

## 手工（建议）

- 多选 2 条 → 批量删除 → 确认文案条数为 2；取消无请求。
- 删除当前选中任务 → 图表与摘要清空。
- 单条删除 → 确认后出现成功提示、列表更新。

## Gaps

执行前留空；完成后更新 `status` 与 `verified`。
