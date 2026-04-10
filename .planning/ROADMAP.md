# ROADMAP: 粮仓平台（GSD）

**Milestone:** 毕设数据库优先 MVP — 体验优化波次  
**Updated:** 2026-04-10

## Overview

| # | Phase | Goal | Requirements | Success Criteria |
| --- | --- | --- | --- | --- |
| 1 | 粮温 Excel 模板与导入一致化 | 模板人类可读、生成与解析同源、说明同步 | DATA-01 — DATA-04 | 已完成（模板 v2） |
| 2 | 温度预测页体验优化 | 任务信息集中可读；预测前可配置训练区间 | PRED-01 — PRED-03 | **已交付**（UAT 见 REQ） |
| 3 | 温度预测页交互二次优化 | 图表先达、双表语义可读 | UX2-01 — UX2-03（见 REQUIREMENTS） | **已交付**（UAT 见 REQ）；底部表形态见 Phase 4 演进 |
| 4 | 温度预测页单表与预测区间 | 单表、预测区间列、操作列；后端可追溯 | POLISH-04-01 — POLISH-04-02 | **已落地**（待 UAT） |
| 5 | 预测记录表支持删除 | 多选 + 批量删 + 单删；二次确认；DEL-05-* | 已规划（待执行） |

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

## Phase 4: 温度预测页单表打磨（prediction-page-polish）

**阶段目录：** `.planning/phases/04-prediction-page-polish/`（`04-CONTEXT.md`、`04-UI-SPEC.md`、`04-01-PLAN.md`）

**Goal:** 消除底部「预测结果列表」与「历史归档」并列带来的重复感：**仅保留一张任务级「预测记录」表**，含 **预测区间** 列与 **操作列**；列表与区间字段来自后端 **`GET /api/predictions/tasks`**（`PredictionService` 归档，非前端 mock）。

**Requirements:** POLISH-04-01 — POLISH-04-02（`REQUIREMENTS.md`）；与 Phase 3 的 UX2-* 演进说明同条引用。

**Depends on:** Phase 3

**Success criteria:**

1. 页面底部仅有 **一张** 全宽任务表，标题与副标题能说明「每次预测一行」。
2. 表中含 **预测区间**列（forecast 起止），且搜索可命中该列展示内容。
3. 通过 **操作列** 完成查看摘要与切换任务；**无** 整行点击切换。
4. 文档（CONTEXT / VERIFICATION）可追溯到后端接口与落库实现。

**Plans:**

- [x] `04-01-PLAN.md` — 单表、预测区间、操作列、GSD 同步（Wave 1）

### Phase 5: 温度预测页预测记录表操作列增加删除，用于清理无用或脏数据记录

**阶段目录：** `.planning/phases/05-prediction-record-delete/`（`05-CONTEXT.md`、`05-UI-SPEC.md`、`05-01-PLAN.md`）

**Goal:** 「预测记录」表支持 **表前多选**、**批量删除** 与 **操作列单条删除**；**凡删除二次确认**（批量须展示数量）；后端 **单删 + 批量删** API，物理删除任务与关联结果。

**Requirements:** DEL-05-01 — DEL-05-02（`REQUIREMENTS.md`）

**Depends on:** Phase 4

**Success criteria:**

1. 表最左列为多选；工具栏有「批量删除」且与 CONTEXT 中无选禁用策略一致。
2. 单删、批量删均有 `ElMessageBox.confirm`；批量确认含选中数量。
3. `DELETE /api/predictions/tasks/{id}` 与 `POST /api/predictions/tasks/batch-delete` 可用且与持久化一致。
4. 删后列表与勾选状态、当前任务主视觉行为符合 `05-CONTEXT.md`。

**Plans:**

- [ ] `05-01-PLAN.md` — 后端删除、前端 API、PredictionView 多选与批量删、文档同步（Wave 1）

---

*Roadmap updated: 2026-04-10（Phase 5：预测记录删除；Phase 4 单表打磨；Phase 2/3 归档见 zzz-docs/归档）*