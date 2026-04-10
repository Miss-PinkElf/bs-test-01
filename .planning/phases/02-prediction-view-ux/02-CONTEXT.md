# Phase 2: 温度预测页体验优化 - Context

**Gathered:** 2026-04-10  
**Status:** Ready for planning

<domain>
## Phase Boundary

在既有 MVP 内优化 `PredictionView`：任务元数据集中展示；预测请求可选训练数据时间窗口（贯通现有 `PredictionRequest`）；错误提示可读。不引入多算法、不改库表。保留「预测结果列表」与「历史归档记录」**两张表**，调整区块顺序以减轻纵向割裂感。
</domain>

<decisions>
## Implementation Decisions

### 任务摘要（PRED-01）

- **D-01:** 使用 **Element Plus `el-descriptions`**（`border`、多列）展示任务号、仓库、预测对象、算法、风险等级、预测天数；用 **第二组 `el-descriptions`**（或紧邻块 + 小标题）展示训练区间、预测区间、执行时间、文字摘要，避免原先 `detail-grid` 散行。

### 训练区间参数（PRED-02）

- **D-02:** 在「滚动预测参数」卡片内使用 **`el-collapse`**，默认折叠，标题 **「高级：训练数据区间」**。
- **D-03:** 使用 **`el-date-picker` `type="datetimerange"`**，`value-format` 采用 **`YYYY-MM-DDTHH:mm:ss`**，与 Spring默认 `LocalDateTime` JSON 反序列化一致；映射到 `predictMetric` 的 `trainStartTime` / `trainEndTime`。
- **D-04:** 提供 **「清空」**：清空区间即 **不传** 训练起止字段，行为与当前「全量可用历史」一致。

### 校验与错误（PRED-03）

- **D-05:** 前端校验：若填写区间，**开始早于结束**；否则 `ElMessage.warning`。
- **D-06:** 请求失败沿用 **`ElMessage.error`**，展示 `http` 拦截器解析后的 `message`（后端业务异常文案）。

### 双表与图表布局（用户补充）

- **D-07:** **保留两个独立表格**（当前任务明细 vs 历史归档），不在本期合并为单表 + Tab。
- **D-08:** 将 **「实际值 / 预测值双线图」整块移到「预测结果列表 + 历史归档」行之下**，避免高图表插在双表之上造成「历史归档离结果表过远」的滚动体验；宽屏仍为左结果 / 右历史并排。

### the agent's Discretion

- `el-descriptions` 具体列数（2～3）与 `size`（`small`）以实际换行为准。
- 折叠面板与日期选择器的辅助 class命名；若需可放入 `frontend/src/styles.css` 的预测页局部类，避免新增行内 `style` 宽度（与仓库样式约定一致）。

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### 规划与需求

- `.planning/phases/02-prediction-view-ux/ALIGNMENT.md` — 本期范围/不做/验收口径
- `.planning/REQUIREMENTS.md` — PRED-01 — PRED-03
- `.planning/ROADMAP.md` — Phase 2 Goal与 Success criteria

### 代码与接口

- `frontend/src/views/PredictionView.vue` — 主改动面
- `frontend/src/api/grain.js` — `predictMetric` 已支持 `trainStartTime` / `trainEndTime`
- `frontend/src/views/DataView.vue` — `datetimerange` + `value-format` 参考（本项目既有模式）
- `backend/src/main/java/com/grain/platform/dto/prediction/PredictionRequest.java` — 训练窗口字段
- `backend/src/main/java/com/grain/platform/service/PredictionService.java` — `loadActualSeries` 按区间过滤

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets

- `DataView.vue` 中 `el-date-picker` + `value-format="YYYY-MM-DD HH:mm:ss"`（查询场景）；预测 POST体优先 **ISO `T` 格式** 以减少 Jackson 配置依赖。

### Established Patterns

- 页面纵向栈：`page-stack` + `gap: 16px`（`frontend/src/styles.css`）。
- 图表容器：`.chart-box` 固定高度 360px。
- API错误：`frontend/src/api/http.js` 将后端 `message` 转为 `Error`。

### Integration Points

- 预测入口：`POST /api/predictions`（`PredictionController`）。

</code_context>

<specifics>
## Specific Ideas

- 用户反馈：历史归档与预测结果表纵向距离过大；结论为 **图表位置导致**，通过 **区块重排** 缓解，**不合并为一张表**。

</specifics>

<deferred>
## Deferred Ideas

- 多算法选择、模型升级 — 仍属独立阶段 / Out of Scope（见 `REQUIREMENTS.md`）。
- 窄屏下双表改 Tab 切换 — 未列入本期；若后续单列过长可再开 REQ。

</deferred>

---

*Phase: 02-prediction-view-ux*  
*Context gathered: 2026-04-10*
