---
status: passed
phase: 07-zzz-prompt-debug-origin-prompt-md
verified: 2026-04-11
source:
  - .planning/phases/07-zzz-prompt-debug-origin-prompt-md/07-01-PLAN.md
  - .planning/phases/07-zzz-prompt-debug-origin-prompt-md/07-CONTEXT.md
  - .planning/phases/07-zzz-prompt-debug-origin-prompt-md/07-RESEARCH.md
---

# Phase 07 目标验证

## Phase 目标

将环境数据页中 **粮温主线** 的“粮温汇总趋势图 + 粮温汇总结果”升级为可查询、可分页、可筛选的汇总页：图表支持共享仓库/时间范围与层级目标切换，汇总表改为服务端分页，且表格细筛不反向影响图表。

## must_haves.truths（对照 PLAN）

| 条目 | 证据 |
|------|------|
| 后端存在新的粮温汇总分页接口 | `backend/src/main/java/com/grain/platform/controller/GrainTempController.java` 中存在 `GET /api/grain-temp/summaries/page`，返回 `ApiResponse<PageResult<GrainTempSummaryItemDto>>` |
| `grain.js` 同时保留序列查询和分页查询 | `frontend/src/api/grain.js` 中同时存在 `fetchGrainTempSummaries` 与 `fetchGrainTempSummaryPage` |
| 汇总表不再走前端 `useClientPagination` | `frontend/src/views/DataView.vue` 中不存在 `grainSummaryPagination` 和 `useClientPagination(filteredGrainSummaries)` |
| 趋势图存在层级目标切换，且固定保留最高温参考线 | `DataView.vue` 中存在 `grainSummaryTarget` / `AVG_TEMP` ~ `LAYER_4_AVG` 选项，图表 `legend` 和 `series` 保留“最高温” |
| 图表与汇总表共享仓库/时间范围，但表格细筛独立 | `DataView.vue` 中 `grainSummaryRange` 同时驱动 `fetchGrainTempSummaries` 和 `fetchGrainTempSummaryPage`；`grainSummaryFilters` 只进入分页请求 |
| 普通环境模式未被粮温汇总新查询态破坏 | `DataView.vue` 中 env 模式仍调用 `fetchSensorData` / `fetchSensorTrend` |
| 自动化验证通过 | `backend/` 下 `mvn -q -DskipTests compile` 与 `frontend/` 下 `npm run build` 均已通过（2026-04-11） |

## 自动化

- `backend` 下 `mvn -q -DskipTests compile`：通过（2026-04-11）
- `frontend` 下 `npm run build`：通过（2026-04-11）

## 需求追溯

- `ENV-07-01`
- `ENV-07-02`
- `ENV-07-03`

## 手工（建议）

- 在粮温主线切换仓库与时间范围，确认图表与汇总表一起刷新
- 切换“整仓均温 / 一层 / 二层 / 三层 / 四层”，确认主线变化且“最高温”参考线保留
- 修改关键词、预警等级、均温范围，确认只影响汇总表，不影响图表
- 切换到普通环境模式，确认环境趋势图和环境记录列表仍保持原行为

## Gaps

- 浏览器运行态交互仍建议由用户本地手工确认一次，尤其是共享查询与表格细筛的联动边界
