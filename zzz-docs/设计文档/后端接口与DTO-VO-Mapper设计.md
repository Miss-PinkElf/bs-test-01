# 粮仓环境数据预测管理平台后端接口与 DTO/VO/Mapper 设计

> 2026-04-08 版本说明：
> - 本文档原本服务于“多指标独立短期预测”路线。
> - 当前滚动预测闭环路线的最新接口真相源已补充为：
>   - `zzz-docs/设计文档/后端接口与DTO-VO-Mapper设计-滚动预测版.md`
>   - `zzz-docs/设计文档/滚动预测改造-字段与接口变更方案.md`
> - 若与上述新文档冲突，以新文档为准。

## 1. 文档定位

本文档用于承接数据库定稿，继续明确：

- 后端正式接口清单
- DTO / VO 分包与命名
- MyBatis Mapper 职责与核心查询

目标不是一次把代码全部写完，而是先把正式实现的接口边界固定下来，便于后续按模块落 Spring Boot + MyBatis。

## 2. 设计约束

- 严格以 `开发指导版 PRD` 为准
- 优先完成登录、仓库、环境数据、图表、预测、归档这条主链路
- 不扩展机器人、数字孪生、LoRa/5G/GSM、区块链、复杂深度学习
- 正式后端维持 `Spring Boot + MyBatis + MySQL`

## 3. 接口约定

## 3.1 基础路径

- 统一前缀：`/api`

## 3.2 统一返回结构

建议正式版统一采用：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

分页数据统一放在 `data` 中：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [],
    "pageNum": 1,
    "pageSize": 10,
    "total": 0
  }
}
```

## 3.3 时间与字段命名

- 时间字段统一返回 `yyyy-MM-dd HH:mm:ss`
- 接口统一使用 `metricCode`，不再混用 `metricType`
- 主键仍使用 `id`
- 页面筛选类分页参数统一用 `pageNum`、`pageSize`

## 3.4 删除策略

MVP 默认不做物理删除，优先做状态启停：

- 用户：`ACTIVE / DISABLED`
- 仓库：`ACTIVE / WARNING / MAINTENANCE / DISABLED`

这样更适合毕业设计演示，也能减少误删数据后的返工。

## 4. 分包建议

```text
backend/src/main/java/com/grain/platform/
├── controller/
├── service/
├── service/impl/
├── mapper/
├── entity/
├── dto/
│   ├── auth/
│   ├── dashboard/
│   ├── user/
│   ├── warehouse/
│   ├── sensor/
│   └── prediction/
├── vo/
│   ├── auth/
│   ├── dashboard/
│   ├── user/
│   ├── warehouse/
│   ├── sensor/
│   └── prediction/
└── common/
    ├── ApiResponse.java
    └── PageResult.java
```

## 5. 正式接口清单

## 5.1 认证模块

| 方法 | 路径 | 说明 | 请求 DTO | 返回 VO |
| --- | --- | --- | --- | --- |
| `POST` | `/api/auth/login` | 用户登录 | `LoginRequest` | `LoginUserVO` |
| `GET` | `/api/auth/me` | 获取当前登录用户 | 无 | `CurrentUserVO` |

### DTO

- `LoginRequest`
  - `username`
  - `password`

### VO

- `LoginUserVO`
  - `userId`
  - `username`
  - `displayName`
  - `roleCodes`
  - `warehouseId`
  - `warehouseName`
  - `token`

- `CurrentUserVO`
  - `userId`
  - `username`
  - `displayName`
  - `roleCodes`
  - `warehouseId`
  - `warehouseName`

## 5.2 仪表盘模块

| 方法 | 路径 | 说明 | 请求 DTO | 返回 VO |
| --- | --- | --- | --- | --- |
| `GET` | `/api/dashboard/overview` | 获取首页概览 | 无 | `DashboardOverviewVO` |

### `DashboardOverviewVO`

- `warehouseCount`
- `todayDataCount`
- `alertCount`
- `archivedPredictionCount`
- `latestAlerts`
- `recentSensorRecords`
- `warehouseHealthList`
- `latestPredictionSummary`

### 子 VO

- `DashboardAlertVO`
  - `title`
  - `level`
  - `description`

- `DashboardRecentSensorVO`
  - `id`
  - `warehouseId`
  - `warehouseName`
  - `metricCode`
  - `metricName`
  - `metricValue`
  - `collectedAt`
  - `qualityFlag`

- `WarehouseHealthVO`
  - `warehouseId`
  - `warehouseName`
  - `healthScore`
  - `riskLevel`
  - `latestTemperature`
  - `latestHumidity`

- `PredictionSummaryVO`
  - `taskId`
  - `taskNo`
  - `warehouseName`
  - `algorithmName`
  - `riskLevel`
  - `requestedAt`

## 5.3 用户管理模块

| 方法 | 路径 | 说明 | 请求 DTO | 返回 VO |
| --- | --- | --- | --- | --- |
| `GET` | `/api/users` | 用户分页列表 | `UserPageQueryDTO` | `PageResult<UserListItemVO>` |
| `GET` | `/api/users/{id}` | 用户详情 | 无 | `UserDetailVO` |
| `POST` | `/api/users` | 新增用户 | `UserCreateDTO` | `IdVO` |
| `PUT` | `/api/users/{id}` | 编辑用户 | `UserUpdateDTO` | `IdVO` |
| `PATCH` | `/api/users/{id}/status` | 启用/禁用用户 | `UserStatusUpdateDTO` | `IdVO` |
| `GET` | `/api/roles/options` | 角色下拉与说明 | 无 | `List<RoleOptionVO>` |

### DTO

- `UserPageQueryDTO`
  - `pageNum`
  - `pageSize`
  - `keyword`
  - `roleCode`
  - `warehouseId`
  - `status`

- `UserCreateDTO`
  - `username`
  - `password`
  - `displayName`
  - `phone`
  - `warehouseId`
  - `roleCodes`
  - `status`

- `UserUpdateDTO`
  - `displayName`
  - `phone`
  - `warehouseId`
  - `roleCodes`
  - `status`

- `UserStatusUpdateDTO`
  - `status`

### VO

- `UserListItemVO`
  - `id`
  - `username`
  - `displayName`
  - `roleCodes`
  - `roleNames`
  - `warehouseId`
  - `warehouseName`
  - `status`
  - `lastLoginAt`

- `UserDetailVO`
  - 在 `UserListItemVO` 基础上增加 `phone`

- `RoleOptionVO`
  - `roleCode`
  - `roleName`
  - `roleDesc`

## 5.4 仓库管理模块

| 方法 | 路径 | 说明 | 请求 DTO | 返回 VO |
| --- | --- | --- | --- | --- |
| `GET` | `/api/warehouses` | 仓库分页列表 | `WarehousePageQueryDTO` | `PageResult<WarehouseListItemVO>` |
| `GET` | `/api/warehouses/{id}` | 仓库详情 | 无 | `WarehouseDetailVO` |
| `GET` | `/api/warehouses/options` | 仓库下拉选项 | 无 | `List<WarehouseOptionVO>` |
| `POST` | `/api/warehouses` | 新增仓库 | `WarehouseCreateDTO` | `IdVO` |
| `PUT` | `/api/warehouses/{id}` | 编辑仓库 | `WarehouseUpdateDTO` | `IdVO` |
| `PATCH` | `/api/warehouses/{id}/status` | 更新仓库状态 | `WarehouseStatusUpdateDTO` | `IdVO` |

### DTO

- `WarehousePageQueryDTO`
  - `pageNum`
  - `pageSize`
  - `keyword`
  - `managerName`
  - `status`

- `WarehouseCreateDTO`
  - `warehouseCode`
  - `warehouseName`
  - `location`
  - `capacityTon`
  - `managerName`
  - `contactPhone`
  - `grainType`
  - `status`
  - `remark`

- `WarehouseUpdateDTO`
  - 同 `WarehouseCreateDTO`，不含 `warehouseCode` 或按是否允许编辑决定

- `WarehouseStatusUpdateDTO`
  - `status`

### VO

- `WarehouseListItemVO`
  - `id`
  - `warehouseCode`
  - `warehouseName`
  - `location`
  - `capacityTon`
  - `managerName`
  - `status`
  - `utilizationRate`
  - `latestTemperature`
  - `latestHumidity`

- `WarehouseDetailVO`
  - 在 `WarehouseListItemVO` 基础上增加 `contactPhone`
  - `grainType`
  - `remark`
  - `updatedAt`

- `WarehouseOptionVO`
  - `id`
  - `warehouseCode`
  - `warehouseName`

## 5.5 指标字典模块

| 方法 | 路径 | 说明 | 请求 DTO | 返回 VO |
| --- | --- | --- | --- | --- |
| `GET` | `/api/metrics/options` | 指标下拉列表 | 无 | `List<MetricOptionVO>` |

### VO

- `MetricOptionVO`
  - `metricCode`
  - `metricName`
  - `unit`
  - `minThreshold`
  - `maxThreshold`

## 5.6 环境数据模块

| 方法 | 路径 | 说明 | 请求 DTO | 返回 VO |
| --- | --- | --- | --- | --- |
| `GET` | `/api/sensor-data` | 环境数据分页查询 | `SensorDataPageQueryDTO` | `PageResult<SensorDataListItemVO>` |
| `GET` | `/api/sensor-data/trend` | 图表趋势查询 | `SensorTrendQueryDTO` | `SensorTrendVO` |
| `POST` | `/api/sensor-data` | 手工录入环境数据 | `SensorDataCreateDTO` | `IdVO` |
| `POST` | `/api/sensor-data/import` | 批量导入，预留 v1.1 | `SensorDataImportDTO` | `ImportResultVO` |

### DTO

- `SensorDataPageQueryDTO`
  - `pageNum`
  - `pageSize`
  - `warehouseId`
  - `metricCode`
  - `startTime`
  - `endTime`
  - `keyword`

- `SensorTrendQueryDTO`
  - `warehouseId`
  - `metricCodes`
  - `startTime`
  - `endTime`

- `SensorDataCreateDTO`
  - `warehouseId`
  - `metricCode`
  - `metricValue`
  - `collectedAt`
  - `sourceType`
  - `remark`

- `SensorDataImportDTO`
  - 预留给 Excel/CSV 导入，不在本轮实现

### VO

- `SensorDataListItemVO`
  - `id`
  - `warehouseId`
  - `warehouseName`
  - `metricCode`
  - `metricName`
  - `metricValue`
  - `unit`
  - `collectedAt`
  - `sourceType`
  - `qualityFlag`

- `SensorTrendVO`
  - `series`
  - `statistics`

- `SensorTrendSeriesVO`
  - `metricCode`
  - `metricName`
  - `unit`
  - `points`

- `SensorTrendPointVO`
  - `time`
  - `value`

- `ImportResultVO`
  - `successCount`
  - `failedCount`
  - `batchNo`

## 5.7 预测模块

| 方法 | 路径 | 说明 | 请求 DTO | 返回 VO |
| --- | --- | --- | --- | --- |
| `GET` | `/api/metrics/options` | 指标下拉列表 | 无 | `List<MetricOptionVO>` |
| `POST` | `/api/predictions` | 执行指标预测并归档 | `PredictionExecuteDTO` | `PredictionTaskDetailVO` |
| `GET` | `/api/predictions/tasks` | 预测任务分页列表 | `PredictionTaskPageQueryDTO` | `PageResult<PredictionTaskListItemVO>` |
| `GET` | `/api/predictions/tasks/{taskId}` | 预测任务详情 | 无 | `PredictionTaskDetailVO` |

### DTO

- `PredictionExecuteDTO`
  - `warehouseId`
  - `metricCode`
  - `futureSteps`
  - `algorithmCode`

- `PredictionTaskPageQueryDTO`
  - `pageNum`
  - `pageSize`
  - `warehouseId`
  - `metricCode`
  - `riskLevel`
  - `status`
  - `startTime`
  - `endTime`

### VO

- `PredictionTaskListItemVO`
  - `taskId`
  - `taskNo`
  - `warehouseId`
  - `warehouseName`
  - `metricCode`
  - `algorithmName`
  - `futureSteps`
  - `sampleSize`
  - `status`
  - `riskLevel`
  - `requestedAt`

- `PredictionTaskDetailVO`
  - 在 `PredictionTaskListItemVO` 基础上增加 `summary`
  - `completedAt`
  - `resultList`

- `PredictionResultPointVO`
  - `stepIndex`
  - `predictedTime`
  - `actualValue`
  - `predictedValue`

## 6. Mapper 设计

## 6.1 Mapper 列表

| Mapper | 主要职责 |
| --- | --- |
| `UserMapper` | 登录查询、用户分页、用户详情、状态更新 |
| `RoleMapper` | 角色字典查询 |
| `UserRoleMapper` | 用户角色关系维护 |
| `WarehouseMapper` | 仓库分页、详情、选项列表、状态更新 |
| `SensorMetricMapper` | 指标字典查询 |
| `SensorDataMapper` | 数据录入、分页、趋势图查询 |
| `PredictionTaskMapper` | 预测任务主表写入与查询 |
| `PredictionResultMapper` | 预测结果明细批量写入与查询 |
| `DashboardMapper` | 首页统计、告警、最近记录、健康度聚合 |

## 6.2 建议核心方法

### `UserMapper`

- `selectLoginUserByUsername(String username)`
- `selectUserPage(UserPageQueryDTO queryDTO)`
- `countUserPage(UserPageQueryDTO queryDTO)`
- `selectUserDetailById(Long id)`
- `insertUser(UserEntity entity)`
- `updateUser(UserEntity entity)`
- `updateUserStatus(Long id, String status)`

### `RoleMapper`

- `selectAllRoles()`
- `selectRolesByUserId(Long userId)`

### `UserRoleMapper`

- `deleteByUserId(Long userId)`
- `batchInsert(Long userId, List<Long> roleIds)`

### `WarehouseMapper`

- `selectWarehousePage(WarehousePageQueryDTO queryDTO)`
- `countWarehousePage(WarehousePageQueryDTO queryDTO)`
- `selectWarehouseDetailById(Long id)`
- `selectWarehouseOptions()`
- `insertWarehouse(WarehouseEntity entity)`
- `updateWarehouse(WarehouseEntity entity)`
- `updateWarehouseStatus(Long id, String status)`

### `SensorMetricMapper`

- `selectActiveMetrics()`
- `selectMetricByCode(String metricCode)`

### `SensorDataMapper`

- `insertSensorData(SensorDataEntity entity)`
- `batchInsertSensorData(List<SensorDataEntity> list)`
- `selectSensorDataPage(SensorDataPageQueryDTO queryDTO)`
- `countSensorDataPage(SensorDataPageQueryDTO queryDTO)`
- `selectTrendPoints(SensorTrendQueryDTO queryDTO)`
- `selectRecentSensorRecords(int limit)`

### `PredictionTaskMapper`

- `insertPredictionTask(PredictionTaskEntity entity)`
- `selectPredictionTaskPage(PredictionTaskPageQueryDTO queryDTO)`
- `countPredictionTaskPage(PredictionTaskPageQueryDTO queryDTO)`
- `selectPredictionTaskDetail(Long taskId)`
- `updatePredictionTaskStatus(Long taskId, String status, String riskLevel, String summary)`

### `PredictionResultMapper`

- `batchInsertPredictionResults(List<PredictionResultEntity> list)`
- `selectResultsByTaskId(Long taskId)`

### `DashboardMapper`

- `countWarehouses()`
- `countTodaySensorData()`
- `countCurrentAlerts()`
- `countArchivedPredictions()`
- `selectLatestAlerts(int limit)`
- `selectRecentSensorRecords(int limit)`
- `selectWarehouseHealthList(int limit)`
- `selectLatestPredictionSummary()`

## 6.3 SQL 实现建议

- 简单单表 CRUD 可以用注解或 XML
- 分页查询、趋势图、仪表盘统计、预测历史等复杂 SQL，建议统一放 MyBatis XML

原因：

- XML 更适合动态条件
- 聚合 SQL 可读性更好
- 后续论文截图和解释也更方便

## 7. 与当前骨架的衔接建议

当前 `backend/` 已有演示级控制器和部分 DTO，下一步建议按下面顺序升级：

1. 引入 `ApiResponse`、`PageResult`
2. 把当前“响应也放在 dto 包”的结构逐步拆为 `dto + vo`
3. 先补齐用户、仓库、环境数据、预测任务四条主线的实体与 mapper
4. 再把 `DemoDataService` 逐步替换成真实数据库服务

## 8. 当前结论

至此，后端正式开发已经具备三层真相源：

- `开发指导版 PRD`
- `数据库设计定稿`
- 本文档中的接口 / DTO / VO / Mapper 设计

后续可以直接进入：

- 后端实体、Mapper、Service、Controller 正式实现
- Vue 前端骨架改造与接口联调

## 9. 2026-04-06 实际落地进展补充

本轮已经完成第一批真实持久层替换，已落地并验证通过的主链路如下：

- 登录：`POST /api/auth/login`
- 仓库列表：`GET /api/warehouses`
- 环境数据写入：`POST /api/sensor-data`
- 多指标预测归档：`POST /api/predictions`

已完成的实现收口：

- 后端数据库连接已切到本地 `grain_env_predict`
- MyBatis XML 已接入 `mapper/` 目录
- 已新增统一返回结构 `ApiResponse`
- 已将后端主链路从 `DemoDataService` 切到真实 `Mapper + Service`
- 已开始把前后端字段命名向 `metricCode` 收口

本轮验证说明：

- 由于本机会话中已有旧 Java 进程占用 `8080`，联调验证临时使用 `8081`
- 在 `8081` 下已实测通过登录、仓库、环境数据、预测归档四条链路

下一步建议：

- 优先推进 `frontend/` 与真实 API 的联调
- 再补用户管理、角色选项、指标选项、预测历史等正式接口
- 最后收口仪表盘增强和演示流程
