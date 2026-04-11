# 粮仓环境数据预测管理平台后端接口与 DTO/VO/Mapper 设计（当前实现对齐版）

## 1. 文档定位

本文档用于记录 **当前已经实现** 的后端接口、核心 DTO/Response 与 Mapper 分工，服务于：

- 论文技术路线与系统设计章节
- 前后端联调说明
- 后续需求变更时的边界确认

本版已按 **2026-04-11** 当前代码核对，真相源优先级如下：

1. `backend/src/main/java/com/grain/platform/controller/`
2. `backend/src/main/java/com/grain/platform/dto/`
3. `backend/src/main/resources/mapper/`
4. `.planning/REQUIREMENTS.md` / `.devflow/grain-platform-bootstrap/state.md`

说明：

- 旧迁移期文档中的 `/api/predictions/{taskId}/correct` 不属于当前已实现接口
- 修正链相关字段继续保留在数据库和响应对象中，作为扩展预留

## 2. 基础约定

- 统一前缀：`/api`
- 统一响应：`ApiResponse<T>`
- 分页响应：`PageResult<T>`
- Query 时间格式：`yyyy-MM-dd HH:mm:ss`
- 预测请求时间格式：`yyyy-MM-dd'T'HH:mm:ss`
- 主线数据来源：
  - 粮温预测优先来自 `grain_temp_summary`
  - 湿度、二氧化碳继续来自 `sensor_data`

## 3. 已实现接口清单

## 3.1 认证与基础选项

| 方法 | 路径 | 说明 | 请求 DTO | 返回 DTO |
| --- | --- | --- | --- | --- |
| `POST` | `/api/auth/login` | 登录 | `LoginRequest` | `LoginResponse` |
| `GET` | `/api/auth/me` | 获取当前用户 | Header `X-Demo-Username`（可选） | `CurrentUserResponse` |
| `GET` | `/api/metrics/options` | 获取启用中的指标选项 | 无 | `List<MetricOptionResponse>` |

## 3.2 用户与角色

| 方法 | 路径 | 说明 | 请求 DTO | 返回 DTO |
| --- | --- | --- | --- | --- |
| `GET` | `/api/users` | 用户全量列表 | 无 | `List<UserListItemResponse>` |
| `GET` | `/api/users/page` | 用户分页列表 | `keyword/pageNum/pageSize` | `PageResult<UserListItemResponse>` |
| `GET` | `/api/users/stats` | 用户统计 | 无 | `UserListStatsResponse` |
| `POST` | `/api/users` | 新增用户 | `UserCreateRequest` | `IdVO` |
| `PUT` | `/api/users/{id}` | 编辑用户 | `UserUpdateRequest` | `IdVO` |
| `PUT` | `/api/users/{id}/password` | 重置密码 | `UserPasswordResetRequest` | `IdVO` |
| `DELETE` | `/api/users/{id}` | 删除用户 | 无 | `Void` |
| `GET` | `/api/roles/options` | 角色选项 | 无 | `List<RoleOptionResponse>` |

## 3.3 仓库管理

| 方法 | 路径 | 说明 | 请求 DTO | 返回 DTO |
| --- | --- | --- | --- | --- |
| `GET` | `/api/warehouses` | 仓库全量列表 | 无 | `List<WarehouseDto>` |
| `GET` | `/api/warehouses/page` | 仓库分页列表 | `keyword/pageNum/pageSize` | `PageResult<WarehouseDto>` |
| `GET` | `/api/warehouses/stats` | 仓库统计 | 无 | `WarehouseStatsResponse` |
| `GET` | `/api/warehouses/options` | 仓库下拉选项 | 无 | `List<WarehouseDto>` |
| `POST` | `/api/warehouses` | 新增仓库 | `WarehouseDto` | `IdVO` |
| `PUT` | `/api/warehouses/{id}` | 编辑仓库 | `WarehouseDto` | `IdVO` |
| `DELETE` | `/api/warehouses/{id}` | 删除仓库 | 无 | `Void` |

## 3.4 粮温导入与数据管理

| 方法 | 路径 | 说明 | 请求 DTO | 返回 DTO |
| --- | --- | --- | --- | --- |
| `POST` | `/api/grain-temp/import` | 导入固定模板粮温文件 | `MultipartFile` | `GrainTempImportResultDto` |
| `GET` | `/api/grain-temp/import/template` | 下载固定模板 | 无 | 文件流 |
| `POST` | `/api/grain-temp/records` | 新增粮温原始记录 | `GrainTempRecordUpsertRequest` | `IdVO` |
| `PUT` | `/api/grain-temp/records/{id}` | 编辑粮温原始记录 | `GrainTempRecordUpsertRequest` | `IdVO` |
| `DELETE` | `/api/grain-temp/records/{id}` | 删除粮温原始记录 | 无 | `Void` |
| `GET` | `/api/grain-temp/records/filter-options` | 粮温筛选项 | `warehouseId`（可选） | `GrainTempRecordFilterOptionsDto` |
| `GET` | `/api/grain-temp/records` | 粮温原始记录分页查询 | `warehouseId/startTime/endTime/zoneCode/layerNo/pointNo/tempMin/tempMax/keyword/pageNum/pageSize` | `PageResult<GrainTempRecordItemDto>` |
| `GET` | `/api/grain-temp/summaries` | 粮温汇总趋势序列 | `warehouseId/startTime/endTime` | `List<GrainTempSummaryItemDto>` |
| `GET` | `/api/grain-temp/summaries/page` | 粮温汇总分页查询 | `warehouseId/startTime/endTime/warningLevel/tempMin/tempMax/keyword/pageNum/pageSize` | `PageResult<GrainTempSummaryItemDto>` |

### 粮温模块说明

- `/summaries` 主要服务于图表序列
- `/summaries/page` 主要服务于汇总表分页
- `/records/filter-options` 用于区域、层号、点位下拉选项

## 3.5 普通环境数据

| 方法 | 路径 | 说明 | 请求 DTO | 返回 DTO |
| --- | --- | --- | --- | --- |
| `GET` | `/api/sensor-data` | 环境数据分页列表 | `warehouseId/metricCode(metricType)/keyword/pageNum/pageSize` | `PageResult<SensorDataPointDto>` |
| `GET` | `/api/sensor-data/trend` | 环境趋势图数据 | `warehouseId/metricCode(metricType)` | `SensorTrendResponse` |
| `POST` | `/api/sensor-data` | 新增环境数据 | `SensorDataCreateRequest` | `IdVO` |
| `PUT` | `/api/sensor-data/{id}` | 编辑环境数据 | `SensorDataUpdateRequest` | `IdVO` |
| `DELETE` | `/api/sensor-data/{id}` | 删除环境数据 | 无 | `Void` |
| `POST` | `/api/sensor-data/import` | 批量导入环境数据 | `MultipartFile` | `SensorDataImportResultDto` |
| `GET` | `/api/sensor-data/import/template` | 下载导入模板 | 无 | CSV 文件流 |

## 3.6 预测与归档

| 方法 | 路径 | 说明 | 请求 DTO | 返回 DTO |
| --- | --- | --- | --- | --- |
| `POST` | `/api/predictions` | 创建预测任务 | `PredictionRequest` | `PredictionTaskResponse` |
| `GET` | `/api/predictions/tasks` | 预测任务全量列表 | 无 | `List<PredictionTaskResponse>` |
| `GET` | `/api/predictions/tasks/page` | 预测任务分页列表 | `keyword/pageNum/pageSize` | `PageResult<PredictionTaskResponse>` |
| `GET` | `/api/predictions/tasks/{taskId}` | 预测任务详情 | 无 | `PredictionTaskResponse` |
| `DELETE` | `/api/predictions/tasks/{taskId}` | 删除单条预测任务 | 无 | `Void` |
| `POST` | `/api/predictions/tasks/batch-delete` | 批量删除预测任务 | `PredictionBatchDeleteRequest` | `Void` |

### 预测模块说明

- 当前预测请求只要求：
  - `warehouseId`
  - `metricCode`
  - `targetType`
  - `trainStartTime`
  - `trainEndTime`
  - `forecastDays`
- 删除接口已落地，用于清理预测记录表中的脏数据或无效数据
- 当前没有 `/correct` 修正预测接口

## 3.7 仪表盘与大屏

| 方法 | 路径 | 说明 | 请求 DTO | 返回 DTO |
| --- | --- | --- | --- | --- |
| `GET` | `/api/dashboard/overview` | 仪表盘顶部概览 | 无 | `DashboardOverviewResponse` |
| `GET` | `/api/dashboard/alerts` | 近期预警分页 | `keyword/pageNum/pageSize` | `PageResult<DashboardAlertItemResponse>` |
| `GET` | `/api/dashboard/warehouse-health` | 仓库健康度分页 | `keyword/pageNum/pageSize` | `PageResult<DashboardWarehouseHealthResponse>` |
| `GET` | `/api/dashboard/grain-summaries` | 最新粮温汇总分页 | `keyword/pageNum/pageSize` | `PageResult<DashboardLatestSummaryResponse>` |
| `GET` | `/api/dashboard/screen` | 展示大屏聚合接口 | `warehouseId/startTime/endTime` | `ScreenDashboardResponse` |

### 仪表盘与大屏说明

- Dashboard 已拆为 `overview + 3 个分页模块`
- `/screen` 为大屏独立聚合接口，不复用旧 mock 数据

## 4. 核心 DTO / Response 说明

## 4.1 预测相关

### `PredictionRequest`

字段：

- `warehouseId`
- `metricCode`
- `targetType`
- `trainStartTime`
- `trainEndTime`
- `forecastDays`

### `PredictionTaskResponse`

字段：

- `taskId`
- `taskNo`
- `parentTaskId`
- `taskRound`
- `warehouseId`
- `warehouseName`
- `metricCode`
- `metricName`
- `unit`
- `maxThreshold`
- `targetType`
- `dataSourceType`
- `algorithmCode`
- `algorithmName`
- `trainStartTime`
- `trainEndTime`
- `forecastStartTime`
- `forecastEndTime`
- `basedOnActualEndTime`
- `forecastDays`
- `triggerType`
- `adjustStatus`
- `riskLevel`
- `requestedAt`
- `completedAt`
- `summary`
- `resultList`

### `PredictionResultItemDto`

字段：

- `id`
- `phaseType`
- `stepIndex`
- `resultTime`
- `actualValue`
- `predictedValue`
- `errorValue`
- `errorRate`
- `warningLevel`
- `warningFlag`
- `warningMessage`
- `isCorrected`

## 4.2 粮温相关

### `GrainTempImportResultDto`

字段：

- `batchNo`
- `warehouseId`
- `warehouseName`
- `collectedAt`
- `pointCount`
- `summaryGenerated`
- `warningLevel`
- `warningMessage`

### `GrainTempRecordItemDto`

字段：

- `id`
- `warehouseId`
- `warehouseName`
- `pointId`
- `pointName`
- `zoneCode`
- `layerNo`
- `pointNo`
- `collectedAt`
- `temperatureValue`
- `sourceType`
- `qualityFlag`

### `GrainTempSummaryItemDto`

字段：

- `id`
- `warehouseId`
- `warehouseName`
- `collectedAt`
- `avgTemp`
- `maxTemp`
- `minTemp`
- `layer1Avg`
- `layer2Avg`
- `layer3Avg`
- `layer4Avg`
- `warningLevel`
- `warningFlag`
- `warningMessage`
- `analysisResult`

## 4.3 仪表盘与大屏相关

核心响应对象：

- `DashboardOverviewResponse`
- `DashboardAlertItemResponse`
- `DashboardWarehouseHealthResponse`
- `DashboardLatestSummaryResponse`
- `ScreenDashboardResponse`
- `ScreenTrendPointResponse`
- `ScreenWarehouseCompareResponse`
- `ScreenPredictionTaskItemResponse`

作用：

- 支撑 Dashboard 模块化分页
- 支撑 `/screen` 三段式大屏聚合响应

## 5. Mapper 设计与 SQL 分工

## 5.1 用户与仓库

- `UserMapper`
  - 用户列表
  - 用户分页
  - 用户统计
  - 角色绑定维护
- `WarehouseMapper`
  - 仓库列表
  - 仓库分页
  - 仓库统计
  - 仓库引用校验

## 5.2 粮温

- `GrainTempPointMapper`
  - 测点结构查询与维护
- `GrainTempRecordMapper`
  - 原始记录分页
  - 区域 / 层号 / 点位筛选
  - `filter-options` 去重查询
- `GrainTempSummaryMapper`
  - 汇总趋势查询
  - 汇总分页查询
  - 预测输入历史序列查询

## 5.3 普通环境数据

- `SensorDataMapper`
  - 环境数据分页查询
  - 趋势图数据查询
  - 导入与 CRUD 写入

## 5.4 预测

- `PredictionTaskMapper`
  - 任务创建
  - 全量列表
  - 分页列表
  - 删除主表记录
- `PredictionResultMapper`
  - 任务结果查询
  - 批量插入预测结果
  - 删除关联结果

## 5.5 仪表盘

- `DashboardMapper`
  - overview 聚合
  - alerts 分页
  - warehouse-health 分页
  - grain-summaries 分页
  - screen 聚合查询

## 6. 与旧文档相比的关键变化

当前已确认的变化如下：

1. **新增分页接口**：
   - `/api/users/page`
   - `/api/users/stats`
   - `/api/warehouses/page`
   - `/api/warehouses/stats`
   - `/api/predictions/tasks/page`
   - `/api/grain-temp/summaries/page`
   - `/api/dashboard/alerts`
   - `/api/dashboard/warehouse-health`
   - `/api/dashboard/grain-summaries`
2. **新增预测删除接口**：
   - `DELETE /api/predictions/tasks/{taskId}`
   - `POST /api/predictions/tasks/batch-delete`
3. **新增大屏聚合接口**：
   - `GET /api/dashboard/screen`
4. **当前没有修正预测接口**：
   - `/api/predictions/{taskId}/correct` 不应再被视为已实现接口

## 7. 最终结论

当前后端边界已经比较清晰：

- 粮温主线围绕导入、原始记录、汇总分析与预测归档展开
- 普通环境数据维持轻量 CRUD + 趋势结构
- 管理端列表统一收口到服务端分页
- 仪表盘与大屏使用独立真实接口
- 修正链字段继续保留，但接口层面尚未作为本期实现目标

因此，论文和后续设计说明应优先引用本文件，而不是迁移期的旧接口设想。
