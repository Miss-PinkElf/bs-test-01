# Phase 12: zzz-prompt-debug-origin-prompt-md - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-04-12
**Phase:** 12-zzz-prompt-debug-origin-prompt-md
**Areas discussed:** 修复覆盖范围, 长 x 轴解法, 默认时间范围, PredictionView 时间范围语义

---

## 修复覆盖范围

| Option | Description | Selected |
|--------|-------------|----------|
| 1A | 只修最直接受影响的正式页面：`DataView` 的普通环境趋势图 + `BigScreenView` 的四块大屏图 | |
| 用户补充 | 必须覆盖 `DataView` 的粮温汇总趋势图与 `PredictionView` 的实际值 / 预测值双线图 | ✓ |

**User's choice:** 以用户明确指定的两张图为本阶段核心范围：`DataView` 粮温汇总趋势图 + `PredictionView` 双线图。  
**Notes:** 用户补充后，范围从最初推荐值调整为“明确点名的两张图”，并明确两张图都要保留。

---

## 长 x 轴解法

| Option | Description | Selected |
|--------|-------------|----------|
| 2A | 以缩短数据窗口为主：补时间/时间范围，再配少量标签抽样/旋转 | |
| 2B | 以图表交互为主：保留更多数据，重点上 `dataZoom`、标签省略、滑动查看 | |
| 2C（偏A） | 两者都要，但以时间范围优先、图表交互兜底 | ✓ |

**User's choice:** 按推荐采用 `2C（偏A）`。  
**Notes:** 首屏可读性优先，不把问题完全推给图表交互。

---

## 默认时间范围

| Option | Description | Selected |
|--------|-------------|----------|
| 3A | 统一较短窗口，优先首屏可读 | |
| 3B | 统一更长历史，优先信息完整 | |
| 3C | 按页面职责区分默认窗口 | ✓ |

**User's choice:** 按推荐采用 `3C`。  
**Notes:** 用户接受“不同页面职责不同，默认窗口不必一刀切”的判断。

---

## PredictionView 时间范围语义

| Option | Description | Selected |
|--------|-------------|----------|
| 1 | 只影响当前图表显示，不改摘要和其它区域 | ✓ |
| 2 | 影响整个当前任务视图 | |
| 3 | 更具体规则待补充 | |

**User's choice:** 采用 `1`。  
**Notes:** 用户接受推荐方案：双线图时间范围只是图表查看窗口，不重定义当前任务本身。

---

## the agent's Discretion

- `DataView` 与 `PredictionView` 默认时间范围的具体数值留给后续 planning 决定。
- `axisLabel`、`rotate`、`formatter`、`dataZoom` 的具体组合留给后续 planning 决定。

## Deferred Ideas

- 后续统一治理所有正式前端 ECharts 图的 x 轴策略
- 后续单独处理 `BigScreenView.vue` 全部图表的一致性收口
