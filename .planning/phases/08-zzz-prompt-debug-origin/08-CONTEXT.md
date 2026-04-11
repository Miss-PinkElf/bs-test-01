# Phase 8: zzz-prompt-debug-origin - Context

**Gathered:** 2026-04-11
**Status:** Ready for planning

<domain>
## Phase Boundary

本阶段只优化现有 `/screen` 展示大屏，对应 `frontend/src/views/BigScreenView.vue`。目标是在**不新增新路由、不另起一套大屏工程**的前提下，把当前“指标卡 + 说明表 + 预警表”的轻量页面，升级为适合答辩展示的**信息型大屏**：优先修复“卡住 / 白屏”问题，再将大屏重组为“上总览、中图表、下明细”的结构，并尽可能用真实数据替换当前演示型内容。

本阶段不扩展为新的业务能力，不新增机器人/硬件接入/复杂算法，也不把后台普通管理页整体重做。

</domain>

<decisions>
## Implementation Decisions

### 页面骨架与风格
- **D-01:** 大屏继续沿用现有公开路由 **`/screen`** 与 `frontend/src/views/BigScreenView.vue`，不新增独立大屏路由或第二套前端页面。
- **D-02:** 页面骨架改为 **上总览 / 中图表 / 下明细**：顶部放总览指标，中部放主图表区，底部放明细信息区。
- **D-03:** 视觉方向采用 **“后台面板增强版”**，即保留当前管理台设计语言与答辩可读性，不走夸张的“科技蓝驾驶舱”风格。

### 数据真实化范围
- **D-04:** 大屏内容遵循 **“能真实就真实”**：能接真实接口的数据优先接真实接口，只有接口或字段确实缺失时才允许局部兜底。
- **D-05:** 当前已接入 `fetchOverview()` 的指标卡能力继续保留，并作为 Phase 8 真实化的基础数据入口。
- **D-06:** 现有纯说明性内容不再占据主视觉区域，只允许保留为**轻量说明块**，用于答辩补充说明。

### 图表策略
- **D-07:** 大屏图表以 **折线图 + 柱状图** 为主，**饼图少量辅助**；不做“到处都是环图”的展示型堆砌。
- **D-08:** 图表选择以“能讲清业务链路”为先，而不是只追求视觉数量；底部仍保留表格型明细，不强行把所有内容都图表化。

### 主图表区内容
- **D-09:** 主图表区至少覆盖以下 4 类内容：**粮温趋势、预警趋势、预测趋势、仓库对比**。
- **D-10:** **粮温趋势** 作为优先级最高的主图，承担答辩讲述中的核心时间序列视图。
- **D-11:** **仓库对比** 更适合用柱状图/条形图表达，用于展示不同仓库之间的横向差异。
- **D-12:** **预警趋势** 与 **预测趋势** 用来补足“发现问题 → 分析预测”的演示链路，不要求都做成最重的图，但必须有可展示的信息位。

### 底部明细区内容
- **D-13:** 底部明细区包含 **最新预警、最近预测任务、重点仓库** 三块核心信息。
- **D-14:** **简短说明** 可以保留，但必须降级为次要区块，不能继续占用与核心数据同等的展示权重。

### 稳定性与性能底线
- **D-15:** 本阶段优先级首先是 **稳定打开、不白屏、不明显卡死**；在此基础上再补足信息量与视觉完整度。
- **D-16:** 如果图表数量、刷新逻辑或视觉效果会明显增加白屏/卡顿风险，允许在实现上做首屏收敛、模块降级或延迟加载，优先保证稳定性。

### Claude's Discretion
- 图表在中部区域的具体排布（2 宫格 / 3 宫格 / 主次布局）可由后续规划结合真实接口情况决定。
- 哪些模块最终使用表格、榜单还是简化卡片，只要不违背“上总览 / 中图表 / 下明细”的骨架即可。
- 若个别图表在现有接口条件下难以完全真实化，可采用“主模块真实、次模块弱化”的收敛策略，但不得回到整体 mock 大屏思路。

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### 原始诉求
- `zzz-prompt-debug/origin/展示大屏优化/prompt.md` — 用户原始诉求：当前展示大屏不行、会卡住白屏、希望尽可能展示信息并更多使用图表
- `zzz-prompt-debug/origin/展示大屏优化/image.png` — 当前问题截图，供后续规划和实现对照

### 路线图与项目约束
- `.planning/ROADMAP.md` — Phase 8 条目与依赖 Phase 7 的位置
- `.planning/PROJECT.md` — 毕设 / 数据库优先 MVP 的范围约束与答辩导向
- `.planning/REQUIREMENTS.md` — 当前项目已落地能力边界，避免把大屏优化扩成新能力
- `.planning/STATE.md` — 最近阶段演进与当前上下文

### 现有前端实现
- `frontend/src/views/BigScreenView.vue` — 当前 `/screen` 大屏实现，Phase 8 的直接改造对象
- `frontend/src/router/index.js` — `/screen` 路由定义与公开访问方式
- `frontend/src/layout/ConsoleLayout.vue` — 后台面板视觉语言与“大屏入口”位置，供风格对齐参考
- `frontend/src/api/grain.js` — `fetchOverview()` 与相关数据规范化入口

### 历史背景
- `.planning/phases/07-zzz-prompt-debug-origin-prompt-md/07-CONTEXT.md` — 最近一轮环境数据页能力增强，提示可复用的粮温相关展示能力
- `NEXT-SESSION-PROMPT-DEVFLOW.md` — 记录 `/screen` 大屏尚未纳入前一轮统一改造范围

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `frontend/src/views/BigScreenView.vue` 已有基础大屏页骨架、顶部操作区、指标卡区和底部信息区，可直接在此页迭代。
- `frontend/src/api/grain.js` 中的 `fetchOverview()` 已返回 `warehouseCount`、`grainSummaryCount`、`realAlertCount`、`predictionAlertCount`、`archivedPredictionCount`、`latestAlerts`、`latestGrainSummaries`、`warehouseHealthList` 等数据，可作为大屏真实化的第一批数据源。
- `frontend/src/layout/ConsoleLayout.vue` 提供当前后台的整体视觉语言，适合作为“后台面板增强版”风格参考，而不是新起一套视觉体系。

### Established Patterns
- 前端统一通过 `frontend/src/api/grain.js` + `request()` 封装访问后端接口，Phase 8 应沿用该模式扩展大屏所需数据。
- 现有前端主栈为 Vue 3 + Element Plus + ECharts，适合继续在当前技术栈内补图表，不需要切换到其它图表或大屏框架。
- `/screen` 是公开路由，意味着大屏实现需要考虑首屏稳定性，不能依赖后台登录态后才初始化的复杂状态。

### Integration Points
- 大屏的直接改造入口是 `frontend/src/views/BigScreenView.vue`。
- 若现有 `fetchOverview()` 不足以支撑四类图表，需要在 `frontend/src/api/grain.js` 与后端概览 / 统计相关接口处补充数据能力。
- 最近 Phase 7 已经增强了环境数据页中的粮温汇总查询与趋势展示，后续可优先评估是否复用其数据口径或查询结果来支撑大屏中的粮温趋势模块。

</code_context>

<specifics>
## Specific Ideas
- 用户明确希望：**尽可能展示信息，能用图就用图**，但不是纯视觉炫技，而是要适合答辩展示与讲解。
- 用户已明确选择：页面结构为 **上总览 / 中图表 / 下明细**。
- 用户已明确选择：图表组合为 **折线图 + 柱状图 + 少量饼图**。
- 用户已明确选择：主图内容包括 **粮温趋势、预警趋势、预测趋势、仓库对比**。
- 用户已明确选择：底部明细包括 **最新预警、最近预测任务、重点仓库、简短说明**。
- 用户已明确选择：整体风格采用 **后台面板增强版**，而不是独立科技蓝大屏风格。
- 用户已明确选择：Phase 8 的底线是 **稳定优先**，先解决 `/screen` 卡住、白屏，再谈更满的信息展示。

</specifics>

<deferred>
## Deferred Ideas
- 是否后续再做独立“科技风”答辩驾驶舱视觉大改版
- 是否将大屏拆成多个可轮播子屏或自动切换场景
- 是否补充更多与硬件、机器人或复杂算法相关的展示模块
- 是否把后台其它页面统一重构为与大屏一致的展示语言

</deferred>

---

*Phase: 08-zzz-prompt-debug-origin*
*Context gathered: 2026-04-11*
