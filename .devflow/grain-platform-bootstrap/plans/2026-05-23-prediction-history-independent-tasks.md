# 预测历史任务恢复为独立保存

## Mini Align

- **问题现象**：当前再次执行同仓库、同指标、同预测对象预测时，后一次预测会删除前一次预测任务，导致历史预测无法并存回看。
- **问题原因**：`PredictionService.predict` 在 2026-05-21 的需求中新增了“按范围删除旧 `prediction_task` / `prediction_result`”逻辑，把“预测开始时间可选”与“最新预测覆盖旧预测”绑在了一起。
- **解决方案**：撤销旧预测删除逻辑，恢复“每次预测新增一条独立任务”；保留 `forecastStartTime`、训练样本自动截断、午夜时间对齐三项时间相关修复。

## 范围

- 修改 `PredictionService`，不再按 `warehouse_id + metric_code + target_type` 删除旧预测。
- 删除仅为覆盖逻辑新增的 `PredictionTaskMapper` 查询。
- 调整 `PredictionServiceTest`，验证预测开始时间仍生效，且不会删除已有任务。
- 更新 devflow 记录，明确本次是对 2026-05-21 需求口径的纠偏。

## 任务

- [x] 移除后端“新预测覆盖旧预测”逻辑。
- [x] 删除不再需要的 Mapper 查询。
- [x] 调整定向测试覆盖“保留历史任务”。
- [x] 运行定向测试与后端编译验证。
- [x] 更新 `state.md`、`bug-log.md`、`checkpoints.md`、`active-plan-links.md`。

## 验证结果

- `backend/` 执行 `mvn -q test -Dtest=PredictionServiceTest` 通过。
- `backend/` 执行 `mvn -q -DskipTests compile` 通过。
