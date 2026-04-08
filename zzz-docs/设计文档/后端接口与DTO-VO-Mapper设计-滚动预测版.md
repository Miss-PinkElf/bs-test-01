# 粮仓环境数据预测管理平台后端接口与 DTO/VO/Mapper 设计（当前工作稿）

## 1. 文档定位

本文档用于记录当前后端接口、DTO/VO 与 Mapper 的工作方向。

说明：

- 旧版 `后端接口与DTO-VO-Mapper设计.md` 已归档到 `zzz-docs/Archive/`
- 若与当前 PRD、当前数据库设计或用户最新口径冲突，以最新 PRD、最新数据库设计和 `.explore` 中最新 handoff 为准
- 文档中涉及“修正预测”的部分，当前应视为扩展预留，而不是本期必做项

## 2. 路线说明

当前接口设计已从“多指标独立短期预测”调整为：

- 粮温 XLS 导入
- 原始测点入库
- 汇总分析查询
- 滚动预测任务创建
- 真实值与预测值双线展示
- 预测结果归档
- 修正能力预留
- 高温预警展示

## 3. 基础约定

- 统一前缀：`/api`
- 时间格式：`yyyy-MM-dd HH:mm:ss`
- 温度预测主输入优先来自 `grain_temp_summary`
- 湿度和二氧化碳继续走 `sensor_data`

## 4. 接口清单

## 4.1 粮温导入模块

| 方法 | 路径 | 说明 | 请求 DTO | 返回 VO |
| --- | --- | --- | --- | --- |
| `POST` | `/api/grain-temp/import` | 导入固定模板粮温 XLS | `MultipartFile` | `GrainTempImportResultVO` |
| `GET` | `/api/grain-temp/import/template` | 下载导入模板说明或样例 | 无 | 文件流 |

### `GrainTempImportResultVO`

- `batchNo`：导入批次号
- `warehouseId`：仓库 ID
- `warehouseName`：仓库名称
- `collectedAt`：检测时间
- `pointCount`：导入测点数
- `summaryGenerated`：是否已生成汇总
- `warningLevel`：真实预警等级
- `warningMessage`：预警说明

## 4.2 粮温数据查询模块

| 方法 | 路径 | 说明 | 请求 DTO | 返回 VO |
| --- | --- | --- | --- | --- |
| `GET` | `/api/grain-temp/records` | 查询粮温原始测点数据 | `GrainTempRecordQueryDTO` | `List<GrainTempRecordVO>` |
| `GET` | `/api/grain-temp/summaries` | 查询粮温汇总分析结果 | `GrainTempSummaryQueryDTO` | `List<GrainTempSummaryVO>` |

### `GrainTempRecordQueryDTO`

- `warehouseId`
- `startTime`
- `endTime`
- `zoneCode`
- `layerNo`

### `GrainTempSummaryQueryDTO`

- `warehouseId`
- `startTime`
- `endTime`

### `GrainTempRecordVO`

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

### `GrainTempSummaryVO`

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

## 4.3 滚动预测模块

| 方法 | 路径 | 说明 | 请求 DTO | 返回 VO |
| --- | --- | --- | --- | --- |
| `POST` | `/api/predictions` | 创建一轮滚动预测任务 | `RollingPredictionCreateDTO` | `PredictionTaskDetailVO` |
| `GET` | `/api/predictions/tasks` | 查询预测任务列表 | `PredictionTaskQueryDTO` | `List<PredictionTaskListItemVO>` |
| `GET` | `/api/predictions/tasks/{taskId}` | 查询预测任务详情 | 无 | `PredictionTaskDetailVO` |
| `POST` | `/api/predictions/{taskId}/correct` | 基于新真实数据执行验证与修正 | `PredictionCorrectionDTO` | `PredictionTaskDetailVO` |

### `RollingPredictionCreateDTO`

- `warehouseId`
- `metricCode`
- `targetType`
- `trainStartTime`
- `trainEndTime`
- `forecastStartTime`
- `forecastEndTime`
- `algorithmCode`

### `PredictionTaskQueryDTO`

- `warehouseId`
- `metricCode`
- `targetType`
- `triggerType`
- `taskRound`

### `PredictionCorrectionDTO`

- `actualDataEndTime`
- `regenerateFuture`：是否生成修正后未来预测
- `algorithmCode`

### `PredictionTaskListItemVO`

- `taskId`
- `taskNo`
- `parentTaskId`
- `taskRound`
- `warehouseId`
- `warehouseName`
- `metricCode`
- `metricName`
- `targetType`
- `algorithmName`
- `triggerType`
- `adjustStatus`
- `forecastStartTime`
- `forecastEndTime`
- `riskLevel`
- `requestedAt`
- `summary`

### `PredictionTaskDetailVO`

- `taskId`
- `taskNo`
- `parentTaskId`
- `taskRound`
- `warehouseId`
- `warehouseName`
- `metricCode`
- `metricName`
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

### `PredictionResultItemVO`

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

## 5. Mapper 建议

## 5.1 新增 Mapper

- `GrainTempPointMapper`
- `GrainTempRecordMapper`
- `GrainTempSummaryMapper`

## 5.2 继续保留但要改 SQL 的 Mapper

- `PredictionTaskMapper`
- `PredictionResultMapper`
- `SensorDataMapper`

## 5.3 关键 SQL 方向

### `GrainTempPointMapper`

- 根据仓库和测点信息查找测点
- 批量插入测点定义

### `GrainTempRecordMapper`

- 批量插入粮温原始记录
- 按仓库、时间、层号查询原始记录

### `GrainTempSummaryMapper`

- 插入系统自动汇总结果
- 按仓库和时间范围查询汇总趋势
- 查询预测输入需要的历史汇总序列

### `PredictionTaskMapper`

- 查询任务列表
- 查询父子任务链
- 插入新一轮修正任务

### `PredictionResultMapper`

- 批量插入每日预测结果
- 按日期回填真实值
- 更新误差和预警
- 查询任务详情结果

## 6. DTO / VO 分包建议

```text
backend/src/main/java/com/grain/platform/dto/
├── grain/
│   ├── GrainTempRecordQueryDTO.java
│   ├── GrainTempSummaryQueryDTO.java
│   ├── RollingPredictionCreateDTO.java
│   └── PredictionCorrectionDTO.java
└── prediction/

backend/src/main/java/com/grain/platform/vo/
├── grain/
│   ├── GrainTempImportResultVO.java
│   ├── GrainTempRecordVO.java
│   └── GrainTempSummaryVO.java
└── prediction/
```

## 7. 当前结论

这份接口设计已经把后续改造的后端边界定出来了：

- 导入链路单独拆成粮温专用接口
- 查询链路拆成原始记录与汇总分析
- 预测链路升级为“任务创建 + 历史查询 + 验证修正”
- DTO/VO 和 Mapper 也围绕新数据模型展开

下一步即可据此去改 `schema.sql` 和 Java 实体/Mapper/Service。
