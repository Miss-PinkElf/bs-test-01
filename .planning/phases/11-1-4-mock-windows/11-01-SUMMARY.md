---
phase: 11-1-4-mock-windows
plan: "01"
status: completed
requirements-completed:
  - D-01
  - D-02
  - D-03
  - D-04
  - D-05
  - D-06
  - D-07
  - D-08
  - D-09
key-files:
  created:
    - scripts/verify-demo-baseline.ps1
  modified:
    - backend/src/main/resources/db/schema.sql
    - scripts/reset-demo-db.ps1
duration: 0h 28m
completed: 2026-04-11
---

# Phase 11 Plan 01 Summary

把 demo 库的真相源改成可重建的河南 1-4 月 baseline，并补上 reset 后可直接执行的基线校验脚本。

## Completed Tasks

1. 重写 `schema.sql` 的演示 seed，移除旧 8-9 月主线，改为仓库 `1 / 2 / 6` 的 Jan-Apr 日级 humidity / co2 / grain temp baseline，并把预测任务与结果同步重置到 5 月预测窗口。
2. 修正 `reset-demo-db.ps1` 的目标数据库语义，使其与 `schema.sql` 的 `USE grain_env_predict` 一致；新增 `verify-demo-baseline.ps1`，直接断言 baseline 行数、时间窗、预警分布和旧 9 月残留已清除。

## Verification

- `rg -n "INSERT INTO sensor_data|INSERT INTO grain_temp_record|INSERT INTO grain_temp_summary|INSERT INTO prediction_task|INSERT INTO prediction_result|2025-01-|2025-04-|2025-05-|TASK-PHASE11|PHASE11-" backend/src/main/resources/db/schema.sql`
- `& '.\scripts\reset-demo-db.ps1'`
- `& '.\scripts\verify-demo-baseline.ps1'`

## Key Outcomes

- `sensor_data` 现在是 3 个重点仓库、2 个环境指标、120 天连续 Jan-Apr 基线，共 720 行。
- `grain_temp_record` / `grain_temp_summary` 通过现有粮温链路生成 5760 条原始点位与 360 条汇总数据，并保留“稳定仓 / 风险仓 / 对比仓”叙事。
- `prediction_task` / `prediction_result` 改成基于 Jan-Apr 真实温度、面向 5 月的 3 组预测归档，不再保留旧 `TASK-ROLLING-*` 与 9 月时间线。

## Deviations from Plan

None - plan executed within the intended write scope.

## Next Phase Readiness

Wave 1 baseline 已可通过脚本稳定重建，Wave 2 可以直接把 `verify-demo-baseline.ps1` 接入 acceptance smoke，并补齐 Phase 11 验证与 UAT 文档。
