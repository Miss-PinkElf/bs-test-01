# 预测开始时间可选与最新预测覆盖旧预测

## Mini Align

- **问题现象**：预测页当前只能按历史样本最后时间自动往后预测，用户不能选择预测开始时间；数据库会保留多次同口径预测任务，旧预测仍出现在预测记录中。
- **问题原因**：前端预测表单只提交 `forecastDays`，后端 `ForecastService` 固定从训练样本最后一条时间 `plusDays(1)` 开始生成结果；`PredictionService.predict` 每次都新增 `prediction_task` 与 `prediction_result`，没有按仓库、指标、预测对象清理旧任务。
- **解决方案**：前端新增“预测开始时间”参数并提交给后端；后端按指定时间连续生成预测点；同一 `warehouse_id + metric_code + target_type` 的旧预测任务和结果在新预测入库前删除，数据库只保留最后一次预测。

## 范围

- 修改预测请求 DTO、预测服务、预测算法入参和预测任务 Mapper。
- 修改正式前端 `PredictionView.vue` 与 API 封装。
- 新增后端服务层单元测试覆盖“指定预测开始时间”和“同口径旧任务删除”。
- 不改 `prediction_task` / `prediction_result` 表结构。
- 不改修正预测入口。

## 任务

- [x] 后端 `PredictionRequest` 增加 `forecastStartTime`。
- [x] 后端预测算法支持指定开始时间。
- [x] 后端新预测入库前删除同口径旧任务及结果。
- [x] 前端预测表单增加预测开始时间选择。
- [x] 补充定向单元测试。
- [x] 运行后端定向测试、后端编译和前端构建。

## 验证结果

- `backend/` 执行 `mvn -q test -Dtest=PredictionServiceTest` 通过。
- `backend/` 执行 `mvn -q -DskipTests compile` 通过。
- `frontend/` 执行 `npm run build` 通过。

## 复测修正

- **问题现象**：页面选择预测开始时间后，后端提示“预测开始时间应晚于训练样本最后时间”。
- **问题原因**：未选择高级训练区间时，后端仍把该仓库全部真实汇总数据作为训练样本；如果数据库中已经存在预测开始时间之后的真实值，训练样本最后时间就会晚于用户选择的预测开始时间。
- **解决方案**：当请求携带 `forecastStartTime` 时，训练样本默认自动截到 `forecastStartTime` 之前；预测完成后的返回结果仍走展示链路，把预测区间内已有真实值回填到图表用于对照。
- **验证结果**：`backend/` 执行 `mvn -q test -Dtest=PredictionServiceTest` 与 `mvn -q -DskipTests compile` 通过。

## 图表时间点修正

- **问题现象**：选择历史日期作为预测开始后，图表同一天上实际值与预测值看起来断开，tooltip 在 `08:40:00` 的真实点上显示预测值为空。
- **问题原因**：页面日期时间选择器可能提交 `00:00:00`，而粮温真实汇总采样时间为 `08:40:00`；后端将 `01-13 00:00` 的预测点与 `01-13 08:40` 的真实点当作两个不同时间点返回。
- **解决方案**：当用户选择的预测开始时间为 `00:00:00` 时，后端自动对齐到训练样本最后一条的采样时刻，例如粮温自动对齐为当天 `08:40:00`。
- **验证结果**：`backend/` 执行 `mvn -q test -Dtest=PredictionServiceTest` 与 `mvn -q -DskipTests compile` 通过。
