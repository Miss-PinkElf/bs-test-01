# Phase 7: zzz-prompt-debug-origin-prompt-md - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in `07-CONTEXT.md` — this log preserves the alternatives considered.

**Date:** 2026-04-10
**Phase:** 07-zzz-prompt-debug-origin-prompt-md
**Areas discussed:** 范围边界、汇总结果筛选结构、趋势图目标模式、图表与表格联动

---

## 范围边界

| Option | Description | Selected |
|--------|-------------|----------|
| 只动粮温主线 | 只优化粮温主线下的汇总趋势图和汇总结果，普通环境模式不动 | ✓ |
| 粮温为主，顺手统一普通环境样式 | 普通环境模式只做样式统一，不改主要行为 | |
| 两种模式一起重做 | 把 DataView 两条主线都纳入同一轮大改 | |

**User's choice:** 只动粮温主线
**Notes:** 用户确认按推荐选项执行，保持 Phase 7 聚焦在粮温汇总查询体验，不扩到整页重构。

---

## 汇总结果筛选结构

| Option | Description | Selected |
|--------|-------------|----------|
| 顶部统一查询 + 表格专属筛选 | 顶部统一管仓库/时间范围；表格再加关键词、预警等级、温度范围 | ✓ |
| 单一大工具栏 | 所有查询条件全部塞进一个工具栏 | |
| 表头筛选风格 | 以表头 filter 为主，尽量贴近后台管理表 | |

**User's choice:** 顶部统一查询 + 表格专属筛选
**Notes:** 这与当前页面“查询卡片 + 业务卡片”的组织方式更一致，也最容易复用现有代码。

---

## 趋势图目标模式

| Option | Description | Selected |
|--------|-------------|----------|
| 单选一条主线 | 在整仓均温/各层均温/最高温中一次只看一条主线 | |
| 单选主目标 + 固定最高温对照 | 主目标单选，但始终保留最高温作为参考线 | ✓ |
| 多选多线 | 用户可任意勾选多层、多指标一起画在图上 | |

**User's choice:** 单选主目标 + 固定最高温对照
**Notes:** 用户接受推荐方案，既能看某层变化，又保留“最高温”这条风险参考线，不把图表复杂度拉高。

---

## 图表与表格联动

| Option | Description | Selected |
|--------|-------------|----------|
| 共享仓库 + 时间范围 | 图和表共享一套仓库/时间条件，确保数据窗口一致 | ✓ |
| 完全独立 | 图表和表格分别维护各自查询条件 | |
| 共享仓库、时间分离 | 两者仓库一致，但时间窗口可各自不同 | |

**User's choice:** 共享仓库 + 时间范围
**Notes:** 用户确认按推荐方案执行，表格额外的关键词/预警等级/温度范围只作为表格的细筛条件。

---

## the agent's Discretion

- 汇总分页接口是扩展原接口还是新增接口
- 查询区的具体排版和按钮组织
- 时间范围默认值与查询触发细节

## Deferred Ideas

- 普通环境模式联动升级
- 多线多层自由组合图表
- 更复杂的多温度字段筛选
- `DataView.vue` 结构性拆分
