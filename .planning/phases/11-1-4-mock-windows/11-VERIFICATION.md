---
status: passed
phase: 11-1-4-mock-windows
verified: 2026-04-11
updated: 2026-04-11T20:30:00+08:00
source:
  - 11-01-PLAN.md
  - 11-02-PLAN.md
requirements:
  - D-01
  - D-02
  - D-03
  - D-04
  - D-05
  - D-06
  - D-07
  - D-08
  - D-09
---

# Phase 11 Verification

## Goal Verdict

Phase 11 已完成自动化与人工收口：demo 库重建链路、Jan-Apr 河南 baseline、真实 API / smoke 主链，以及页面侧验收口径均已确认可接受。

## Automated Checks

1. `& '.\scripts\reset-demo-db.ps1'`
Result: passed. `schema.sql` 可稳定重建 `grain_env_predict`，且脚本目标库语义与 `USE grain_env_predict` 一致。

2. `& '.\scripts\verify-demo-baseline.ps1'`
Result: passed. 关键结果：
- `sensor_data` 720 行、120 天、无 9 月残留。
- `grain_temp_record` 5760 行、`grain_temp_summary` 360 行、风险仓有 2 条真实预警。
- `prediction_task` 3 条、`prediction_result` 12 条，训练窗口固定 Jan-Apr，预测窗口固定 May。

3. `& '.\scripts\run-acceptance-smoke.ps1'`
Result: passed. 覆盖了：
- backend `mvn -q -DskipTests compile`
- frontend `npm run build`
- reset + baseline verifier gate
- `/api/sensor-data`、`/api/sensor-data/trend`、`/api/grain-temp/summaries`、`/api/predictions/tasks` 的 Phase 11 baseline 断言
- `/api/dashboard/overview` 与分页子接口
- Users CRUD
- 固定模板粮温导入、legacy CSV 导入、legacy row-style Excel 导入

## Key Evidence

- `/api/sensor-data?warehouseId=2&metricCode=humidity&pageNum=1&pageSize=150`
  Evidence: `total = 120`，返回中不存在 `2025-09*` 记录，April 记录可见。
- `/api/sensor-data/trend?warehouseId=6&metricCode=co2`
  Evidence: `points = 120`，趋势点全部位于 Jan-Apr。
- `/api/grain-temp/summaries?warehouseId=2`
  Evidence: 120 条 Jan-Apr 汇总，且存在真实预警样本。
- `/api/predictions/tasks`
  Evidence: 仅保留 3 条 `TASK-PHASE11-*` 任务，无 `TASK-ROLLING-*` 旧任务；风险仓详情中的 FUTURE 点全部位于 2025-05。

## Human Verification

1. DataView 的 env 模式切换到 `humidity` / `co2` 后，仓库 2 与仓库 6 能直接看到 Jan-Apr 趋势与分页列表。
   result: passed
   note: 用户已同意直接归档本 phase，视为页面口径符合当前预期。
2. DataView 的 grain 模式与汇总分页展示 Jan-Apr 粮温汇总，不再出现旧 9 月主线。
   result: passed
   note: 已按 Phase 11 基线收口，允许归档。
3. PredictionView 中 Phase 11 归档任务的训练窗口为 Jan-Apr，预测窗口为 May，且风险仓 / 稳定仓 / 对比仓叙事符合答辩口径。
   result: passed
   note: 用户已接受当前答辩叙事与归档窗口。

## Risks / Notes

- `npm run build` 仍会输出 Vite chunk-size warning，但不影响本 phase 的功能验收。
- `mysql` CLI 会输出 password warning；当前仅属工具提示，不影响 reset / verifier 结论。

## Final Verdict

用户已接受当前结果，并要求补归档说明后直接完成 Phase 11 收口。本阶段现可视为完成。
