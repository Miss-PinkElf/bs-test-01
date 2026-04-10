# Phase 4: prediction-page-polish - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.  
> Decisions are captured in `04-CONTEXT.md`.

**Date:** 2026-04-10  
**Phase:** 4 — prediction-page-polish  
**Areas discussed:** 双表信息架构（主从 + 历史表操作列）、历史表点击行为

---

## 双表是否重复 & 历史表操作列

| Option | Description | Selected |
|--------|-------------|----------|
| 维持 Phase 3 两表语义，仅优化文案 | 不合并；继续整行点击 |  |
| 合并为单表 | 任务与分步合一 |  |
| 主从 + 历史表操作列 | 右表任务级主控，左表当前任务分步明细；历史表「查看摘要」「切换/加载结果」 | 是 |

**User's choice:** 采纳主从方案；两表数据粒度本不重复，通过操作列强化主控。  
**Notes:** 原始疑问「是否重复」已通过粒度区分澄清。

---

## 历史表：整行点击 vs 仅按钮

| Option | Description | Selected |
|--------|-------------|----------|
| 保留行点击 + 操作列 | 快捷与显式并存 |  |
| 仅操作列按钮 | 取消 `@row-click`，只通过按钮切换任务 | 是 |

**User's choice:** 不用保留点击切换，只保留按钮。  
**Notes:** 与 CONTEXT D-03/D-04 一致；当前行高亮仍可数据驱动。

---

## 查看摘要（实现形态）

| Option | Description | Selected |
|--------|-------------|----------|
| 滚到摘要区 + 轻量强调 | 默认实现；规划阶段可改（助手建议，用户未单独反对；见 CONTEXT D-05） | 是 |
| Drawer / Popover | 独立浮层展示摘要 |  |

**User's choice:** 未单独回复；CONTEXT 锁定为滚动 + 轻量强调。

---

## Claude's Discretion

- 按钮文案、图标、列宽不足时的菜单折叠。  
- `prompt.md` 中样式与时间范围列的具体落表。

## Deferred Ideas

- 视觉对照 `image.png` 的细粒度样式。  
- 历史/明细表「时间范围」列与后端字段。  
- 窄屏 Tab（Phase 3 deferred）。

---

## 2026-04-10 修订（单表、预测区间、数据源）

| 话题 | 用户表述 | 落地 |
|------|-----------|------|
| 表数量 | 只保留一个表；「预测结果列表」与「历史归档」是否同一类 | 底部仅 **「预测记录」** 全宽任务表；去掉左侧分步明细表 |
| 列 | 多加「预测区间」 | 列展示 `forecastStartTime` ~ `forecastEndTime` |
| 数据 | 是否 mock、后端有没有 | **非 mock**：`GET /api/predictions/tasks`，`PredictionService` 读库；见 CONTEXT **D-07**与 canonical_refs |

**说明：** 正式决策与条目编号以 `04-CONTEXT.md`、`04-01-PLAN.md`、`REQUIREMENTS.md`（POLISH-04-*）为准；本段仅供审计追溯。
