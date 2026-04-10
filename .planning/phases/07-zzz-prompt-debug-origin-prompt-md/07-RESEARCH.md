# Phase 7: zzz-prompt-debug-origin-prompt-md - Research

**Date:** 2026-04-11
**Status:** Complete

## Research Goal

为 Phase 7 规划提供可执行依据：在不重构 `DataView.vue` 页面整体结构、且不回归普通环境模式的前提下，为粮温主线补齐 **汇总结果后端分页/筛选** 与 **趋势图时间范围 + 层级目标切换**。

## Key Findings

### 1. 当前前端的汇总区与趋势图区高度耦合在同一份全量汇总数组上

- `frontend/src/views/DataView.vue` 当前通过 `fetchGrainTempSummaries({ warehouseId })` 一次取回全量汇总数据。
- `filteredGrainSummaries` 同时服务于：
  - 趋势图 `xAxis` / `avgTemp` / `maxTemp`
  - 汇总表
  - `latestGrainSummary`
- 汇总表分页仍是 `useClientPagination(filteredGrainSummaries)`，说明分页完全在浏览器内完成。

**Implication:**
Phase 7 必须把“图表共享查询条件”和“表格专属筛选条件”拆开；否则一旦把关键词/预警等级/温度范围继续直接挂在 `filteredGrainSummaries` 上，就会违背 `07-CONTEXT.md` 的 D-11，导致图表被表格细筛条件误伤。

### 2. 后端已经有成熟的分页/筛选模式，可直接照搬到粮温汇总链路

- `backend/src/main/java/com/grain/platform/service/GrainTempService.java` 中的 `listRecordPage(...)` 已形成统一模式：
  - service 层做 `pageNum/pageSize` 兜底与 `count + offset + limit`
  - mapper 分成 `countByCondition` 与 `selectPageByCondition`
  - controller 返回 `ApiResponse<PageResult<T>>`
- `SensorDataService.listPage(...)` 与 `GrainTempRecordMapper.xml` / `SensorDataMapper.xml` 使用的是同一分页语义。

**Implication:**
粮温汇总分页最稳妥的实现不是引入新框架，而是直接为 `grain_temp_summary` 新增与原始测点页一致的分页 SQL 和 service/controller 接口。

### 3. 现有 `GrainTempSummaryItemDto` 已经具备 Phase 7 所需字段，不需要再扩 DTO

`backend/src/main/java/com/grain/platform/dto/grain/GrainTempSummaryItemDto.java` 已包含：
- `avgTemp`
- `maxTemp`
- `minTemp`
- `layer1Avg` ~ `layer4Avg`
- `warningLevel`
- `warningMessage`
- `collectedAt`
- `warehouseName`

**Implication:**
图表“层级目标单选 + 最高温参考线”可以直接基于现有 DTO 在前端切换，不需要为图表专门新增字段。

### 4. 最小后端契约变更应该是“保留现有汇总列表接口给图表，再新增分页接口给表格”

现有 `GET /api/grain-temp/summaries` 已被 `DataView.vue` 用于“全量汇总列表 + 图表”。如果直接把它改成 `PageResult`：
- 会破坏当前调用方
- 也不利于趋势图拿到完整时间序列

更稳妥的方案是：
- 保留 `GET /api/grain-temp/summaries`：继续作为**图表序列接口**，承载共享的 `warehouseId + startTime + endTime`
- 新增 `GET /api/grain-temp/summaries/page`：仅给汇总表使用，额外承载 `keyword + warningLevel + tempMin + tempMax + pageNum + pageSize`

**Implication:**
这能天然满足 D-10 / D-11：图表和表格共享仓库/时间范围，但表格专属细筛不影响图表。

### 5. 风险点不在“能不能做”，而在 `DataView.vue` 的状态拆分是否干净

当前页面还同时承载：
- 普通环境模式（`mode === "env"`）
- 粮温原始测点记录分页/筛选
- 粮温汇总表
- 粮温趋势图

**Primary risk:**
如果直接复用现有 `grainCollectedRange`、`handleSearch`、`loading` 而不区分“共享查询”与“原始测点筛选”，很容易把 Phase 7 的汇总条件误串到原始测点记录查询里，或者让 env 模式也被迫适配粮温专属逻辑。

**Recommended boundary:**
为 Phase 7 增加独立的 grain-summary 查询状态：
- 共享条件：仓库、时间范围
- 图表条件：层级目标
- 表格条件：关键词、预警等级、整仓均温范围、分页态

### 6. `latestGrainSummary` 不应继续绑定到“表格过滤后的当前页结果”

当前 `latestGrainSummary = filteredGrainSummaries.at(-1)`。

在 Phase 7 下，如果表格再加关键词/预警等级/温度范围：
- 这个“最新汇总”提示会随着表格细筛变化
- 用户会误以为趋势图也被同样过滤了

**Recommendation:**
将 `latestGrainSummary` 改为基于“共享仓库 + 时间范围”的汇总序列结果，而不是基于表格细筛后的分页结果。

## Recommended Planning Shape

### Execution order

1. 后端先补 `grain_temp_summary` 分页/筛选接口
2. 前端 API 层新增 summary page 请求封装
3. `DataView.vue` 拆分粮温汇总的共享查询态、图表态、表格态
4. 趋势图改为“所选目标主线 + 最高温”
5. 同步 GSD 文档与 REQUIREMENTS / ROADMAP 占位信息

### Concrete contract recommendation

- Keep: `GET /api/grain-temp/summaries`
  - params: `warehouseId`, `startTime`, `endTime`
  - use: chart series + latest summary hint
- Add: `GET /api/grain-temp/summaries/page`
  - params: `warehouseId`, `startTime`, `endTime`, `keyword`, `warningLevel`, `tempMin`, `tempMax`, `pageNum`, `pageSize`
  - use: summary result table only

### Files most likely to change

- `backend/src/main/java/com/grain/platform/controller/GrainTempController.java`
- `backend/src/main/java/com/grain/platform/service/GrainTempService.java`
- `backend/src/main/java/com/grain/platform/mapper/GrainTempSummaryMapper.java`
- `backend/src/main/resources/mapper/GrainTempSummaryMapper.xml`
- `frontend/src/api/grain.js`
- `frontend/src/views/DataView.vue`
- `frontend/src/styles.css`（only if layout classes are needed）

## Regression Watchlist

- `mode === "env"` 的查询与趋势逻辑不能回归
- 粮温原始测点记录现有分页/筛选不能被共享时间范围误伤
- 共享查询条件变更时，图表和汇总表必须一起刷新；表格细筛变更时，只刷新表格
- 新 summary page 接口必须继续遵守现有 `PageResult<T>` 形态，避免前端再造分页协议

## Research Conclusion

Phase 7 是一个**中等复杂度的前后端协同页面优化**，但不需要新架构。最佳策略是：
- **后端沿用现有 PageResult 分页模式**
- **前端把共享查询态与表格细筛态明确拆开**
- **图表继续吃汇总序列，表格改吃分页接口**

这样最贴合当前仓库结构，也最不容易引入 env 模式回归。

## RESEARCH COMPLETE
