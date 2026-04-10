# Phase 5: 温度预测页预测记录表操作列增加删除 - Context

**Gathered:** 2026-04-10  
**Updated:** 2026-04-10（多选列 + 批量删除 + 二次确认强化）  
**Status:** Planned（`05-01-PLAN.md`）

<domain>
## Phase Boundary

在 Phase 4 已落地的**单表「预测记录」**上，提供**清理无用或脏数据**能力：**表前多选列**、工具栏**批量删除**、操作列**单条删除**；**凡删除操作均须二次确认**（`ElMessageBox.confirm`）。范围包含：**后端持久化删除**（任务及其关联预测结果，支持单条与批量）+ **前端**多选状态、工具栏与列表刷新、与**当前选中任务**（曲线/摘要）状态一致。不包含：软删除/回收站、按条件自动清理、权限模型扩展、仪表盘等其它模块对归档数据的联动修订（若存在需在执行前由 planner 对照代码确认）。

</domain>

<decisions>
## Implementation Decisions

### 删除语义与数据一致性

- **D-01:** 删除「预测记录」=删除数据库中对应的 **`prediction_task`** 行及其 **`prediction_result`** 行（**物理删除**）。采用服务层事务：先删子表再删父表，或等价级联策略；**不做**软删除、不做回收站。
- **D-02:** 单条删除：若任务 id 不存在，接口返回 **404**（或项目统一的「未找到」错误形态），前端提示可读错误即可。批量删除：若部分 id 不存在，**推荐**整批在**同一事务**中处理——要么全部删除成功，要么回滚（避免「静默跳过」导致用户误以为已删）；具体错误文案由 planner 与现有 `ApiResponse` 风格对齐。

### HTTP API

- **D-03:** 新增 **`DELETE /api/predictions/tasks/{taskId}`**，成功返回统一 **`ApiResponse`** 成功体；实现位置与风格对齐 **`PredictionController`** / **`PredictionService`**。
- **D-03b:** 新增批量删除接口（与单条同级、同鉴权），**推荐**形态： **`POST /api/predictions/tasks/batch-delete`**，请求体为 **`taskId` 列表**（如 JSON `{ "taskIds": [1, 2, 3] }`）。服务层**单事务**内删除多任务及其 `prediction_result`；空列表应在后端校验并返回 **400** 可读错误。选用 `POST` 而非带 body 的 `DELETE`，以符合常见 Spring / 网关习惯；若实现时项目已有统一 batch约定可再对齐。

### 表前列多选与工具栏

- **D-04:** 在 **`el-table` 最左侧**（任务号等列之前）增加 **`type="selection"`** 列，支持跨行多选（与 Element Plus 表格选择行为一致）。**注意：** 行选择用于「批量删除」；**勿**与 Phase 4「当前查看哪条任务曲线」的 **`selectedTaskId`** 混为一谈——若需避免概念混淆，实现上可用独立 ref（如 `historySelection`）承载表格勾选行。
- **D-04b:** 在预测记录表**上方工具栏**（与搜索框同一 `toolbar-row` 区域或紧邻）增加 **`批量删除`** 按钮：**`type="danger"`**；**未勾选任何行时禁用**（或点击时 `ElMessage.warning` 提示先勾选，二选一由实现选定，需行为一致）。勾选后点击批量删除，**必须先走二次确认**（见 D-05）。

### 操作列单条删除

- **D-05a:** 在现有 **「查看摘要」「切换任务」** 旁增加 **「删除」** 按钮：使用 **`el-button link`**，**`type="danger"`**，**`@click.stop`**。
- **D-05b（二次确认 — 强制）：** **每一条**删除路径（操作列单删、工具栏批量删）在调用接口前**必须**弹出 **`ElMessageBox.confirm`**；文案需说明**不可恢复**；批量删除时确认框**必须展示选中数量**（或任务号摘要，至少含数量）。用户取消则不调用接口。成功后 **`ElMessage.success`**；失败 **`ElMessage.error`**。

### 删除后 UI 与选中态

- **D-06:** 删除成功后 **重新拉取** `GET /api/predictions/tasks` 并更新本地列表；**清空表格多选**（避免残留勾选已不存在的行）。
- **D-07:** 若本次删除集合中包含当前 **`selectedTaskId`**（用户正在查看该任务曲线/摘要）：删除成功后应 **清空当前预测展示**（`prediction` 回到空/默认结构、`selectedTaskId` 置空）并 **销毁或清空图表**；若删除的均非当前任务，仅刷新列表与清除多选，**不改变**当前主视觉任务。

### 权限与范围

- **D-08:** **不**新增 RBAC；与现有预测接口同一认证与调用假设（管理端 MVP）。

### Claude's Discretion

- MyBatis 批量删除（`deleteByTaskIds` /循环单删在事务内）与 **`@Transactional`** 边界。
- 操作列 **宽度**（当前约 `184px`）是否略增以容纳「删除」按钮。
- 分页与多选：仅当前页勾选或翻页后勾选行为（Element Plus 默认）；若需「跨页全选」本阶段**不做**，除非用户另提需求。
- 批量删除接口路径命名若与项目其它 batch 接口不一致时的微调。

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### 路线图与阶段契约

- `.planning/ROADMAP.md` — Phase 5 条目；Depends on Phase 4
- `.planning/phases/04-prediction-page-polish/04-CONTEXT.md` — 单表「预测记录」、操作列既有行为、接口事实源
- `.planning/REQUIREMENTS.md` — POLISH-04-*（Phase 4 基线）；Phase 5 验收条目待 `/gsd-plan-phase 5` 补充

### 代码（集成与事实源）

- `backend/src/main/java/com/grain/platform/controller/PredictionController.java` — 现有 `POST` / `GET` 预测与任务列表
- `backend/src/main/java/com/grain/platform/service/PredictionService.java` — 落库与 `listTasks` / `getTask`
- `backend/src/main/java/com/grain/platform/mapper/PredictionTaskMapper.java` — 需扩展删除 / 批量删除
- `backend/src/main/java/com/grain/platform/mapper/PredictionResultMapper.java` — 需按 taskId / taskIds 删除
- `frontend/src/api/grain.js` — `fetchPredictionTasks` 等；新增单删与批量删封装
- `frontend/src/views/PredictionView.vue` — 「预测记录」表、工具栏与操作列

### 项目级

- `.planning/PROJECT.md` — MVP 边界与毕设约束

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets

- **列表加载：** `loadPredictionHistory`、`fetchPredictionTasks`、`predictionHistory`、`historyLoading`。
- **当前任务：** `selectedTaskId`、`selectPredictionTask`、`prediction` ref、`resolveHistoryRowClassName`。
- **反馈：** 已有 `ElMessage`；需 **`ElMessageBox`**（二次确认）。
- **工具栏：** 现有 `table-toolbar` + 搜索框，可并列放置批量删除按钮。

### Established Patterns

- REST：**`/api/predictions`** 前缀；**`ApiResponse`** 包装。
- 表：**操作列** 固定右侧、**`@click.stop`**、无整行 row-click（Phase 4）；本阶段在**左侧**增加 selection 列。

### Integration Points

- 后端当前**无** 删除类接口；**Mapper** 需补删除与批量删除。
- 前端需区分 **表格勾选** vs **当前查看任务**，避免与 `selectPredictionTask` 语义冲突。

</code_context>

<specifics>
## Specific Ideas

- 用户在 add-phase 中的表述：脏数据/无用记录需可删；操作列单删。
- **增补：** 表前**多选** + 工具栏 **批量删除**；**所有删除必须二次确认**（含批量时展示数量）。

</specifics>

<deferred>
## Deferred Ideas

- **按条件清理**（按时间/仓库一键清）— 本阶段仅手动勾选或逐条删。
- **软删除 / 审计日志** — 本阶段明确物理删除；若答辩需可追溯删除动作，另开阶段。
- **仪表盘「归档预测数」等统计** 是否在删除后需联动 — 执行前读 `DashboardMapper` / 相关 SQL，本 CONTEXT 不预先改产品口径。
- **跨页全选** — 未要求；默认仅当前页多选。

</deferred>

---

*Phase: 05-prediction-record-delete*  
*Context gathered: 2026-04-10*
