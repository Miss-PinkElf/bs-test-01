---
status: partial
phase: 12-zzz-prompt-debug-origin-prompt-md
source:
  - 12-VERIFICATION.md
started: 2026-04-12T21:02:51+08:00
updated: 2026-04-12T21:02:51+08:00
---

## Current Test

BigScreen 粮温趋势主图待补充人工测试

## Tests

### 1. DataView 默认 30 天窗口
expected: 进入粮温主线后，粮温汇总趋势图默认带出最近 30 天时间范围；手动调整时间范围并点击查询后，图表和汇总表继续共用同一组仓库 + 时间范围。
result: passed
note: 用户已确认当前方案“可以了”，按通过记录。

### 2. DataView 长 x 轴可读性
expected: 把时间范围拉长到超过默认窗口后，x 轴标签不会再完整挤爆；可通过缩放或滑动查看更长时间线，且“最高温”参考线仍可见。
result: passed
note: 用户已确认当前方案“可以了”，按通过记录。

### 3. PredictionView 默认 7 天图表窗口
expected: 当前任务的实际值 / 预测值双线图默认展示最近 7 天；如果任务本身不足 7 天，则图表显示完整时间线。
result: passed
note: 用户已确认当前方案“可以了”，按通过记录。

### 4. PredictionView 图表时间范围不污染任务语义
expected: 修改图表时间范围时，只改变双线图显示；任务摘要中的训练区间、预测区间、执行时间，以及下方历史任务列表都不被一起重置。点击“清空”后，双线图恢复完整时间线。
result: passed
note: 用户已确认当前方案“可以了”，按通过记录。

### 5. BigScreen 粮温趋势主图长 x 轴可读性
expected: 进入 `/screen` 后，把共享时间范围拉长到超过默认窗口时，粮温趋势主图的 x 轴不会再次完整挤爆；可通过缩放或滑动查看更长时间线，且仍保留“粮温趋势 + 最高温”两条线。
result: pending

## Summary

total: 5
passed: 4
issues: 0
pending: 1
skipped: 0
blocked: 0

## Gaps

None.
