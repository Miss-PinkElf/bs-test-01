---
phase: 11-1-4-mock-windows
plan: "02"
status: completed
requirements-completed:
  - D-02
  - D-03
  - D-04
  - D-08
  - D-09
key-files:
  created:
    - .planning/phases/11-1-4-mock-windows/11-VERIFICATION.md
    - .planning/phases/11-1-4-mock-windows/11-HUMAN-UAT.md
  modified:
    - scripts/run-acceptance-smoke.ps1
    - backend/src/main/java/com/grain/platform/service/GrainTempImportService.java
    - .planning/ROADMAP.md
    - .planning/STATE.md
duration: 1h 05m
completed: 2026-04-11
---

# Phase 11 Plan 02 Summary

把 Phase 11 baseline 接进 acceptance smoke，并把自动化验证证据与人工验收入口一起落盘。

## Completed Tasks

1. `run-acceptance-smoke.ps1` 现在会在 reset 后先执行 `verify-demo-baseline.ps1`，再对 `/api/sensor-data`、`/api/sensor-data/trend`、`/api/grain-temp/summaries`、`/api/predictions/tasks` 和 dashboard 分页接口做 Jan-Apr / May 窗口断言。
2. 同步 `ROADMAP.md`、`STATE.md`、`11-VERIFICATION.md`、`11-HUMAN-UAT.md`，把 Phase 11 实际范围明确收口为“河南 1-4 月 baseline + 脏数据清理”，并显式记录 Windows 一键运行已延期。

## Verification

- `& '.\scripts\run-acceptance-smoke.ps1'`
- `& '.\scripts\reset-demo-db.ps1'`
- `& '.\scripts\verify-demo-baseline.ps1'`
- `rg -n "Windows 一键运行已延期|reset-demo-db|verify-demo-baseline|/api/sensor-data|/api/predictions/tasks|DataView|PredictionView" .planning/ROADMAP.md .planning/STATE.md .planning/phases/11-1-4-mock-windows/11-VERIFICATION.md .planning/phases/11-1-4-mock-windows/11-HUMAN-UAT.md`

## Deviations from Plan

- [Rule 1 - Bug] `GrainTempImportService` 的固定模板识别与回退逻辑原本会把下载模板误判成行式 Excel，导致 acceptance smoke 在固定模板导入处失败。本次顺手修复为：固定模板文件优先走固定模板解析，并为仓库自带模板增加稳定的已知布局回退解析，避免旧 smoke 被误报为回归。

## Next Phase Readiness

自动化链路已通过；当前仅剩 `11-HUMAN-UAT.md` 中的人机界面验收待确认。用户确认后即可把 Phase 11 从“已落地（待 UAT）”收口到完全完成。
