# Phase 9: 管理端列表去 mock 并收口服务端分页查询 - Research

**Date:** 2026-04-11
**Status:** Complete

## Research Goal

回答本阶段 planning 最关键的 4 个问题：

1. 仓库里已有的服务端分页范式是什么，哪些实现可以直接复用
2. `UsersView` / `WarehouseView` / `PredictionView` 如何在不改变当前交互语义的前提下切到后端分页
3. `DashboardView` 应如何从单一 overview 聚合响应拆成“概览 + 独立分页接口”
4. `PredictionView` 保留完整任务详情列表 payload 的风险和边界是什么

## Findings

### 1. 仓库已经有稳定的服务端分页范式，可直接复用

**Evidence**

- 前端分页返回结构已经统一为 `PageResult<T>`，见 `backend/src/main/java/com/grain/platform/common/PageResult.java`
- `GrainTempController` / `SensorDataController` 已经采用 `pageNum`、`pageSize`、`keyword` 查询参数并返回 `PageResult<T>`
- `GrainTempService.listRecordPage()`、`GrainTempService.listSummaryPage()`、`SensorDataService.listPage()` 已经形成统一的 `count + offset + limit + normalize page args` 服务层模式
- 前端 `frontend/src/api/grain.js` 已有 `normalizePageResult()`，并在 `fetchSensorData()`、`fetchGrainTempSummaryPage()`、`fetchGrainTempRecords()` 中复用
- `frontend/src/views/DataView.vue` 已经完整落地“页面本地维护分页状态 -> 请求后端分页 -> 表格按页显示”的模式

**Implication**

Phase 9 不需要重新设计分页协议。最稳妥的做法是让用户、仓库、预测任务、Dashboard 新列表接口全部复用：

- 请求：`keyword`, `pageNum`, `pageSize`，按需加页面特有条件
- 响应：`PageResult<T>`
- 前端：在 `grain.js` 新增对应 `fetchXxxPage()`，页面维护 `pageState`

### 2. Users/Warehouse/Prediction 可以平滑切到后端分页，不需要改用户操作模型

#### Users

**Evidence**

- `frontend/src/views/UsersView.vue` 当前只依赖用户列表数组本身、关键词过滤和 `useClientPagination`
- `backend/src/main/java/com/grain/platform/service/UserService.java` 当前只有 `listUsers()`，直接调用 `UserMapper.selectAllUsers()`
- `backend/src/main/resources/mapper/UserMapper.xml` 的 `selectAllUsers` 已经把页面需要的列和角色聚合逻辑都拼好了

**Recommended approach**

- 在 `UserMapper` 增加 `countUsers(keyword)` + `selectUserPage(keyword, offset, pageSize)`
- 复用现有 `group_concat` 聚合角色写法，不要新设计 DTO
- 前端从 `fetchUsers()` 扩为分页版 `fetchUserPage()`
- `UsersView.vue` 继续保留当前表格、弹窗、摘要卡片和角色说明表，只移除 `useClientPagination`

**Why this is low risk**

页面现有的新增、编辑、删除、重置密码交互都不依赖“全量列表必须在内存里”这个前提。

#### Warehouse

**Evidence**

- `frontend/src/views/WarehouseView.vue` 当前分页和搜索全部在前端完成
- 右侧“当前选中仓库”详情只依赖当前页列表中的选中项
- `backend/src/main/java/com/grain/platform/service/WarehouseService.java` 只有 `list()` 返回全量 DTO
- `backend/src/main/resources/mapper/WarehouseMapper.xml` 当前 `selectAll` 很简单，适合直接扩 keyword + limit/offset

**Recommended approach**

- 新增 `countPage(keyword)` + `selectPage(keyword, offset, pageSize)`
- keyword 覆盖当前列表里可见字段：编码、名称、位置、负责人、状态
- 前端切到后端分页后，仍保留“点击行查看右侧详情”模式

**Key caveat**

分页后如果切页导致当前 `selectedWarehouseId` 不在当前页，页面应继续保留该选中 id，并在新数据回流后决定是否重置；不要退化为“翻页就清空当前选中仓库”。

#### Prediction

**Evidence**

- `frontend/src/views/PredictionView.vue` 当前搜索与分页在前端完成，但删除、多选、切换任务、查看摘要等行为都基于“列表项就是完整任务详情”
- `backend/src/main/java/com/grain/platform/service/PredictionService.java` 的 `listTasks()` 当前已返回完整 `PredictionTaskResponse`
- `backend/src/main/resources/mapper/PredictionTaskMapper.xml` 目前只有 `selectAll`

**Recommended approach**

- 新增 `countPage(keyword)` + `selectPage(keyword, offset, pageSize)` 只分页 task 主表
- 服务层对“当前页 tasks”逐条复用现有 `toResponse(...)` 组装完整 `PredictionTaskResponse`
- 前端把 `fetchPredictionTasks()` 改为支持 `keyword/pageNum/pageSize`
- 保持当前 UI：列表返回完整 task，`selectPredictionTask(row)` / `focusTaskSummary(row)` 无需重写

**Tradeoff**

这不是最省查询次数的方案，因为每页 task 仍会逐条补 `resultList`。但它符合当前 phase 决策：不拆轻量列表 DTO，不改现有前端交互心智。

### 3. Dashboard 应拆成“概览聚合 + 独立分页列表接口”，不要继续把分页列表塞进 overview

**Evidence**

- `frontend/src/views/DashboardView.vue` 当前只调一次 `fetchOverview()`，然后对 `latestAlerts`、`warehouseHealthList`、`latestGrainSummaries` 做本地分页 / 增量加载
- `backend/src/main/java/com/grain/platform/service/DashboardService.java` 当前 `getOverview()` 把计数、预警、汇总、健康度都打包进一个响应
- `backend/src/main/resources/mapper/DashboardMapper.xml` 当前所有 dashboard 列表查询都带硬编码 `limit 4/6`

**Recommended approach**

- 保留 `GET /api/dashboard/overview`，但让它更聚焦“顶部指标/概览摘要”
- 新增独立分页接口，例如：
  - `GET /api/dashboard/alerts`
  - `GET /api/dashboard/warehouse-health`
  - `GET /api/dashboard/grain-summaries`
- 每个接口都支持 `keyword/pageNum/pageSize`
- `DashboardView.vue` 首屏并行请求：
  - `fetchOverview()`
  - `fetchDashboardAlertsPage()`
  - `fetchDashboardWarehouseHealthPage()`
  - `fetchDashboardGrainSummariesPage()`

**Why not keep them inside overview**

如果继续把分页列表塞回 overview，会出现两个问题：

1. 分页和搜索行为语义混乱：overview 既是聚合概览，又要承载带 query 的分页列表
2. 后端接口会越来越像“超级接口”，后续难维护，也和本仓库已经形成的 `PageResult<T>` 模式不一致

### 4. 保留 PredictionView 完整任务详情分页是可行的，但要接受查询成本上升

**Evidence**

- `PredictionService.listTasks()` 当前对每条 task 都会：
  - 查 warehouse
  - 查 metric
  - 查实际序列
  - 查 `prediction_result`
- 这意味着从“全量”改为“分页”后，单次请求成本会降，但单条 task 的装配成本不变

**Risk**

- 如果预测任务数非常大、且单页 pageSize 设置过大，分页接口依然会比较重
- 但相较于当前全量返回，这已经是明显收敛

**Planning consequence**

- Phase 9 计划里应该显式限制默认 `pageSize`
- 不要在本阶段顺手再引入“详情接口 + 延迟加载结果明细”，那会扩大范围并冲突于已锁定决策

## Recommended Planning Shape

最稳妥的 planning 拆分应是两波：

### Wave 1

1. 后端：用户、仓库、预测任务分页接口
2. 前端：`UsersView.vue`、`WarehouseView.vue`、`PredictionView.vue` 切到服务端分页

### Wave 2

3. 后端：Dashboard 独立分页接口
4. 前端：`DashboardView.vue` 切到“overview + 独立分页接口”
5. 清理 `frontend/src/mock/platform.js` 与不再使用的前端分页依赖

这种拆法的好处是：

- 先完成标准 CRUD / 列表页收口
- 再单独处理 Dashboard 这种“聚合 + 多列表”的特殊页
- 更符合验证节奏，避免一口气混太多页面

## Risks To Call Out In Planning

1. `UsersView.vue` 和 `WarehouseView.vue` 目前依赖全量列表长度生成 summary cards；改分页后，这些卡片不能再用“当前页长度”充当总数，必须改成后端 total 或独立 count。
2. `WarehouseView.vue` 的当前选中项逻辑在分页后要防止“翻页即丢详情”。
3. `PredictionView.vue` 的删除后刷新必须继续清理多选状态与当前任务视觉状态，不能因为分页切换把 Phase 5 行为回退。
4. Dashboard 如果同时改 overview 和新分页接口，前端 loading/error 状态需要分层，不要让某一个分页列表失败导致整页看起来像 overview 失败。

## No External Research Needed

本阶段是明确的仓内收口型任务。现有代码已经提供足够证据：

- 分页协议
- 前后端接入模式
- Dashboard 当前聚合方式
- PredictionView 详情装配方式

无需额外查第三方库或外部文档即可进入 planning。

## RESEARCH COMPLETE

- Existing pagination pattern is sufficient and reusable
- Users/Warehouse/Prediction can switch to backend pagination without changing page semantics
- Dashboard should split into overview + paged list endpoints
- Keeping full task payloads for PredictionView is acceptable for this phase, with page-size discipline
