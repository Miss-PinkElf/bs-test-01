# ROADMAP: 粮仓平台（GSD）

**Milestone:** 毕设数据库优先 MVP — 体验优化波次  
**Updated:** 2026-04-10

## Overview

| # | Phase | Goal | Requirements | Success Criteria |
| --- | --- | --- | --- | --- |
| 1 | 粮温 Excel 模板与导入一致化 | 模板人类可读、生成与解析同源、说明同步 | DATA-01 — DATA-04 | 已完成（模板 v2） |
| 2 | 温度预测页体验优化 | 任务信息集中可读；预测前可配置训练区间 | PRED-01 — PRED-03 | **已交付**（UAT 见 REQ） |
| 3 | 温度预测页交互二次优化 | 图表先达、双表语义可读 | UX2-01 — UX2-03（见 REQUIREMENTS） | **已交付**（UAT 见 REQ） |

## Phase 1: 粮温 Excel 模板与导入一致化（已完成）

**Delivered:** 固定 XLSX 合并单元格与中文分区；`cellMatchesKey` 兼容双语标签；`DataView` 文案同步。

**可选后续：** DATA-05 验收脚本扩展。

---

## Phase 2: 温度预测页体验优化：任务信息集中展示与预测参数扩展（训练区间等）（已交付）

**阶段目录：** `.planning/phases/02-prediction-view-ux/`

**Delivered（2026-04-10）：** 摘要 `el-descriptions`、折叠训练区间、`ElMessage`、后端 `PredictionRequest`时间格式；详见 `zzz-docs/归档/2026-04-10-温度预测页Phase2-Phase3交付归档.md`。

**Goal:** 解决预测页「信息散、参数少」：摘要区信息架构收紧；表单透出后端已支持的训练时间窗口，默认行为不变。

**Requirements:** PRED-01 — PRED-03

**Depends on:** Phase 1（无硬依赖）

**Success criteria:**

1. 同一视口内任务元数据分组清晰。
2. 用户可选择训练数据起止时间并发起预测；不传区间时与全量历史行为一致。
3. 历史数据不足或区间无效时，错误信息可理解。

**Plans:** 已实现；可选补 `PLAN.md` 仅作文档归档。

---

## Phase 3: 温度预测页交互二次优化：图表可达性与双表语义说明（已交付）

**阶段目录：** `.planning/phases/03-prediction-ux-pass2/`（`ALIGNMENT.md`、`03-CONTEXT.md`）

**Delivered（2026-04-10）：** 图序、图表锚点滚动、两表 `panel-subtitle`、`md` 起 15:9 栅格；详见 `zzz-docs/归档/2026-04-10-温度预测页Phase2-Phase3交付归档.md`。

**Goal:** 执行预测后主视觉（曲线）无需滚到页底即可看到；两张表的职责与「点击历史行切换任务」一眼可读。

**Requirements:** UX2-01 — UX2-03

**Depends on:** Phase 2

**Success criteria:**

1. 常规视口下，点击「执行预测」后图表进入可视区域（布局：参数+摘要 → 图 → 双表）。
2. 两表卡片均有一句副标题，能区分「当前任务分步明细」与「历史任务列表」。
3. **`md` 及以上宽度**下参数与任务摘要左右并排（非仅超宽 `xl` 才并排），与 UX2-03 一致。

**Plans:** 与 ALIGNMENT 同步落地；可选 `/gsd-plan-phase 3` 补 PLAN.md。

---

*Roadmap updated: 2026-04-10（Phase 2/3 标注已交付；轻量归档见 zzz-docs/归档）*
