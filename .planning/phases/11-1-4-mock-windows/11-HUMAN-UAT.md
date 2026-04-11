---
status: partial
phase: 11-1-4-mock-windows
source:
  - 11-VERIFICATION.md
started: 2026-04-11T20:21:52+08:00
updated: 2026-04-11T20:21:52+08:00
---

## Current Test

等待人工在前端确认 Jan-Apr 河南 baseline 的展示效果与预测归档口径。

## Tests

### 1. DataView env 模式检查 humidity / co2
expected: 选择仓库 2 或 6，切到 `humidity` / `co2` 后，趋势图与分页列表都只呈现 Jan-Apr 数据，不再出现旧 9 月样本。
result: pending

### 2. DataView grain 模式检查粮温汇总
expected: 粮温汇总图与汇总列表围绕 Jan-Apr baseline，风险仓在 4 月末接近阈值，稳定仓保持平缓，对比仓有轻微波动但不是旧 9 月高温故事。
result: pending

### 3. PredictionView 检查归档任务窗口
expected: 归档任务只剩 `TASK-PHASE11-*`；训练窗口为 `2025-01-01` 到 `2025-04-30`，预测窗口落在 `2025-05`；风险仓 / 稳定仓 / 对比仓摘要可直接用于答辩讲述。
result: pending

## Summary

total: 3
passed: 0
issues: 0
pending: 3
skipped: 0
blocked: 0

## Gaps

None yet. If人工检查发现旧 9 月样本、错误窗口或页面口径不一致，再回填到这里并进入 gap closure。
