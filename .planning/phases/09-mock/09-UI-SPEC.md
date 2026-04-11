---
phase: 09
slug: mock
status: draft
shadcn_initialized: false
preset: none
created: 2026-04-11
---

# Phase 09 — UI Design Contract

> 面向 Phase 9「管理端列表去 mock 并收口服务端分页查询」的视觉与交互合同。供 `gsd-planner`、`gsd-executor`、`gsd-ui-checker`、`gsd-ui-auditor` 共同消费。

---

## Design System

| Property | Value |
|----------|-------|
| Tool | none |
| Preset | 不适用 |
| Component library | Vue 3 + Element Plus |
| Icon library | `@element-plus/icons-vue` |
| Font | `"Source Han Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif` |

说明：
- 本项目当前是 **Vue 3 + Vite + Element Plus**，不是 React/Next/shadcn 体系，因此 **shadcn gate 不适用**。
- 本阶段不引入新视觉语言，不新建第二套列表组件；继续沿用 `page-stack`、`metrics-grid`、`panel-card`、`el-table`、`el-pagination`。
- Phase 9 是“数据源与交互口径收口”而不是“列表页重设计”：页面骨架、按钮分布、摘要卡、右侧详情/图表关系保持现状，只收紧分页、搜索、空态、错误态与 mock 清理表现。

来源：
- 上游约束：`09-CONTEXT.md` D-01 ~ D-14、`09-RESEARCH.md`
- 现有实现：`frontend/package.json`、`frontend/src/styles.css`、`frontend/src/views/UsersView.vue`、`frontend/src/views/WarehouseView.vue`、`frontend/src/views/PredictionView.vue`、`frontend/src/views/DashboardView.vue`

---

## Spacing Scale

声明值仅使用 4 的倍数：

| Token | Value | Usage |
|-------|-------|-------|
| xs | 4px | 标签与行内状态点的微间距 |
| sm | 8px | 表格操作按钮间距、标签组间距、紧凑提示行 |
| md | 16px | 默认卡片内边距、列表模块间距、分页与表格间距 |
| lg | 24px | 页面一级区块留白、摘要卡与主列表之间的断层 |
| xl | 32px | 页面头部与主内容的主节奏间距 |
| 2xl | 48px | 仅用于较大断层，如预测页图表与下方记录表的明显分区 |
| 3xl | 64px | 不在本阶段新增使用；仅保留为全页级安全留白上限 |

Exceptions:
- 可点击控件最小热区按 **40px** 处理；如出现纯图标或极窄翻页控件，最小按 **44px** 处理。
- `PredictionView` 的主图容器继续允许 **360px** 高度，因为它是语义尺寸，不是普通留白 token。
- `DashboardView` 列表卡与表格模块默认仍用 `16px` 间距，不允许因分页组件加入而压缩到 `12px` 以下。

Phase 9 收口约束：
- 列表顶部搜索/按钮工具条统一使用 `12px` 历史值的收口版，即实现时按 **8px 或 16px** 吸附；新代码优先落到 **8px** 的组件级间距与 **16px** 的区块级间距。
- 表格与分页器之间固定 `16px`。
- 卡片头、搜索栏、表格、分页四段结构保持垂直顺序，不在本阶段做左右混排压缩。

来源：
- 现有全局样式：`frontend/src/styles.css`
- 参考基线：Phase 8 `08-UI-SPEC.md`

---

## Typography

本阶段只声明 4 个字号、2 个字重：

| Role | Size | Weight | Line Height |
|------|------|--------|-------------|
| Body | 16px | 400 | 1.5 |
| Label | 14px | 400 | 1.5 |
| Heading | 20px | 600 | 1.2 |
| Display | 28px | 600 | 1.2 |

补充约束：
- 页面标题、模块总标题、右侧详情标题统一落在 `Heading` 或 `Display`，不新增第五档。
- 顶部 summary card 数值统一使用 `Display`，不再为分页后的小范围统计额外放大到 34px 以上。
- 搜索框 placeholder、分页说明、标签说明、副标题、空态正文统一使用 `Label` 或 `Body`，避免列表页出现大段 13px/17px 混杂。
- 表格正文、描述区正文、错误提示正文统一使用 `Body`。
- 删除确认、批量删除确认、接口失败提示走系统弹层样式，但文本语气遵守本合同的 copywriting 约束。

来源：
- 字体与现有字号来自 `frontend/src/styles.css`
- 行高与层级按当前后台列表页可读性收口为 4 档

---

## Color

| Role | Value | Usage |
|------|-------|-------|
| Dominant (60%) | `#f3efe6` | 后台页面背景、整体主画布 |
| Secondary (30%) | `rgba(255,255,255,0.92)` | 卡片、表格面板、详情面板、弹窗主体 |
| Accent (10%) | `#0b7a75` | 查询主按钮、当前选中态、高亮行、当前分页/主操作 |
| Destructive | `#f56c6c` | 删除按钮、删除确认语义、危险反馈 |

Accent reserved for:
- 列表页主查询动作
- 当前选中的记录行或当前任务强调
- 与主链路直接相关的主按钮，例如 `新增用户`、`新增仓库`、`执行预测`
- 分页器当前页、可聚焦的主输入反馈
- 预测页 `查看摘要` 触发后的摘要强调边框

明确禁止：
- Accent 不得用于所有按钮、所有 tag、所有边框。
- `warning` / `attention` 风险等级继续沿用 Element Plus 风险色，不与主 accent 混用。
- “mock 演示风格”的强调色不再出现在这四个页面，包括“用于答辩演示”“演示后端”等弱真实化文案对应的装饰色。

来源：
- 现有 token：`frontend/src/styles.css` 中 `--app-bg`、`--panel-bg`、`--brand-main`
- 删除动作来自现有 `el-button type="danger"` 模式

---

## Copywriting Contract

| Element | Copy |
|---------|------|
| Primary CTA | 查询列表 |
| Empty state heading | 暂无匹配结果 |
| Empty state body | 当前关键词或分页条件下没有可展示数据。请清空关键词、返回第一页，或先新增/导入数据后再查看。 |
| Error state | 列表加载失败，请重试；若仍失败，请检查接口状态或缩小关键词后重新查询。 |
| Destructive confirmation | 删除用户：`确定删除用户“{displayName}”吗？`；删除仓库：`确定删除仓库“{warehouseName}”吗？`；删除预测记录：`确定删除任务“{taskNo}”吗？删除后不可恢复。`；批量删除预测记录：`确定删除选中的 {count} 条预测记录吗？删除后不可恢复。` |

补充文案合同：
- 搜索 placeholder 只覆盖当前列表实际展示字段，不写超出当前表格的结构化条件。
- Phase 9 覆盖页面内，删除所有“演示”“mock”“用于答辩演示”“当前演示后端仅返回”这一类会削弱真实数据语义的文案。
- `DashboardView` 若接口失败，文案必须明确“真实接口加载失败”，不得回落成看似正常的假数据提示。
- `WarehouseView` 右侧详情空态使用：`暂无仓库数据`
- `PredictionView` 记录表空态使用：`暂无预测记录`
- `UsersView` 角色说明区可继续保留“角色编码/权限说明”型说明文案，但不新增分页相关解释文案。

来源：
- 页面现有按钮与提示：`UsersView.vue`、`WarehouseView.vue`、`PredictionView.vue`、`DashboardView.vue`
- Phase 9 决策：`09-CONTEXT.md` D-04、D-05、D-13、D-14

---

## Interaction Contract

### 1. Shared List Pattern

- 本阶段覆盖的主列表统一采用：`summary cards / 辅助说明` -> `主列表卡片` -> `keyword 搜索` -> `按页请求表格或列表` -> `标准分页器`。
- 所有目标页的分页主链必须是 **后端 keyword + pageNum + pageSize**；禁止继续对全量数组执行 `filterRows()`、`slice()`、`useClientPagination()` 作为真实数据主链。
- keyword 只匹配 **当前列表已展示字段**；不增加高级筛选面板，不扩展第二个主搜索框。
- keyword 变化后必须回到第一页。
- 切页、改 pageSize、提交 keyword 都必须触发新的后端请求；不得只在当前页数组上做本地重算。

### 2. Pagination Contract

- `UsersView`、`WarehouseView`、`PredictionView` 默认 `pageSize = 10`，提供 `10 / 20 / 50`。
- `DashboardView` 的三个分页模块默认 `pageSize = 5`，提供 `5 / 10 / 20`。
- 分页器统一使用 `total, sizes, prev, pager, next`。
- 页码变化时只刷新当前模块，不触发整页阻塞刷新。
- 若返回页码越界，前端应落回后端返回的实际 `pageNum`，而不是停留在无效页。

### 3. Loading, Empty, Error

- 加载态以 **模块级** 为主，继续保留当前卡片、标题、搜索框、分页器骨架；禁止因为单块列表刷新而把整页白屏。
- 空态只替换列表主体，不移除搜索框与标题，让用户能立刻调整关键词或返回第一页。
- 错误态与空态必须区分：
  - 空态表示“接口成功但无数据”
  - 错误态表示“接口失败，需要重试或排查”
- `DashboardView` 的 overview 卡、近期预警、仓库健康度、最新粮温汇总三块各自独立 loading/error，不得因为任一分页模块失败导致 overview 失效。

### 4. Summary Card Counting Rules

- 分页后，`UsersView` 与 `WarehouseView` 顶部 summary card 不得用“当前页条数”充当系统总数。
- 这些卡片必须以后端 total 或明确的聚合字段为准。
- `DashboardView` 顶部 5 张指标卡继续以后端 overview 聚合为准，不受下方分页模块当前页条数影响。
- `PredictionView` 本阶段不新增总量 summary card，避免把分页列表总数误解成预测结果总量。

### 5. Mock Removal Contract

- 这四个页面不得再显示任何“mock 占位正常工作”的假象。
- 接口失败时显示明确失败状态，不自动回填本地假数据。
- `frontend/src/mock/platform.js` 删除后，不补新的运行时 mock 兜底目录。
- 若有仍然存在的静态说明卡片，其文案必须表述真实业务上下文，不得再出现“演示型后端”“临时假数据”口径。

---

## Page-Specific Contracts

### UsersView

- 页面结构保持 **顶部 3 张 summary card + 左侧用户列表 + 右侧角色说明**。
- 只有 **用户主表** 切到服务端分页；右侧 `角色说明` 仍可保持小规模静态/轻量列表，不强制为分页模块。
- 搜索框 placeholder 固定为当前可见字段集合，推荐：
  - `搜索用户名、姓名、手机号、角色、所属范围、状态`
- `新增用户`、`编辑`、`重置密码`、`删除` 交互保持不变；Phase 9 不改弹窗结构。
- 删除成功后应刷新当前页数据，并保持 keyword 与 pageSize 不丢失。

### WarehouseView

- 页面结构保持 **顶部 summary cards + 左侧提示卡 + 右侧当前选中仓库详情 + 主列表**。
- 主列表改为服务端分页后，`selectedWarehouseId` 仍是页面级状态，不因切页自动清空。
- 行点击继续驱动右侧详情联动。
- 若当前选中仓库在刷新后仍存在，则继续展示该详情。
- 若当前选中仓库已被删除或已不在当前结果集中，则回退到当前页第一条；若当前页为空，则展示空态。

### PredictionView

- 页面结构继续保持 **参数与摘要 -> 图表 -> 预测记录表**，不因分页改造改变信息架构。
- 预测记录列表继续返回 **完整任务详情结构**，不拆详情接口；`查看摘要`、`切换任务`、`删除`、`批量删除` 交互保持 Phase 4/5 既有语义。
- 搜索框继续只覆盖当前表中已展示字段：任务号、预测对象、仓库、风险、预测天数、预测区间、执行时间。
- 当前选中的任务摘要与图表是页面级主视觉：
  - 切页或改 keyword 时，如果当前页里没有该任务，摘要和图表 **不自动清空**
  - 只有在“删除当前任务”或“执行新预测切换到新任务”时，才清空或替换主视觉
- 批量删除后必须清空表格勾选状态；若删除集合包含当前任务，则摘要卡与图表同步清空。

### DashboardView

- 页面继续保持 **顶部指标卡 + 近期预警 + 仓库运行健康度 + 最新粮温汇总 + 次级说明卡** 的后台首页结构。
- 顶部指标卡继续使用 overview 聚合接口。
- 下方三块数据模块改为 **三个独立分页模块**：
  - 近期预警
  - 仓库运行健康度
  - 最新粮温汇总
- `近期预警` 不再使用无限滚动或“下滑继续加载更多预警”；改为标准分页 footer。
- 三块模块各自维护自己的 keyword、pageNum、pageSize、loading、error。
- 若次级说明卡与真实分页模块争夺首屏注意力，优先保证真实数据模块，不为说明卡压缩分页区空间。

---

## Component Inventory

| Area | Component / Pattern | Contract |
|------|---------------------|----------|
| 顶部 summary cards | `el-card.metric-card` | 继续保留，不改为图表或新样式；统计口径改为后端真实 total / overview |
| 搜索入口 | `el-input` + `Search` icon | 每个主列表一个 keyword；placeholder 仅描述当前可见字段 |
| 主列表 | `el-table` | Users / Warehouse / Prediction / Dashboard 表格保持 Element Plus 风格，不换虚拟表或自研表 |
| 近期预警列表 | `list-card` 或等价卡片列表 | 可保留卡片样式，但必须加标准分页，不再无限滚动 |
| 分页器 | `el-pagination` | 统一布局、统一放置在模块底部 |
| 右侧详情 | `detail-grid` / `el-descriptions` | Warehouse 与 Prediction 的详情/摘要保持现有信息密度 |
| 操作列 | `el-button link` | 编辑、重置密码、删除、查看摘要、切换任务维持现有轻量链接按钮模式 |
| 模块 loading | `v-loading` / `el-skeleton` | 仅做模块级，不做整页阻塞 |
| 空态 | `el-empty` 或表格空数据态 | 保留查询工具区，不整卡消失 |

禁止项：
- 不引入无限滚动替代标准分页。
- 不在本阶段引入多标签筛选器、高级搜索 Drawer、批量操作工具栏扩展。
- 不把 Dashboard 再改回“大包 overview 响应 + 前端切片”的旧模式。

---

## Data-to-UI Mapping

| UI 模块 | 数据来源合同 | 说明 |
|--------|--------------|------|
| Users 主表 | 后端 `PageResult<User>` + `keyword/pageNum/pageSize` | 真实分页；角色说明区不强制分页 |
| Warehouse 主表 | 后端 `PageResult<Warehouse>` + `keyword/pageNum/pageSize` | 真实分页；右侧详情跟随选中 id |
| Prediction 记录表 | 后端 `PageResult<PredictionTaskResponse>` + `keyword/pageNum/pageSize` | 返回完整任务详情，不拆轻量 DTO |
| Dashboard 顶部指标 | overview 聚合接口 | 只负责汇总，不承载分页列表 |
| Dashboard 近期预警 | 独立 `PageResult<Alert>` 接口 | keyword 仅搜当前卡片可见字段 |
| Dashboard 仓库健康度 | 独立 `PageResult<WarehouseHealth>` 接口 | 与 overview 解耦 |
| Dashboard 粮温汇总 | 独立 `PageResult<Summary>` 接口 | 与 overview 解耦 |

后端返回合同：
- 所有分页接口统一返回 `PageResult<T>`
- 前端统一通过 `grain.js` 层做 `normalizePageResult`
- 页面内只维护分页与查询状态，不直接处理 mock fallback

来源：
- `09-RESEARCH.md`
- `frontend/src/api/grain.js`
- Phase 7 已落地的 `DataView.vue` 服务端分页模式

---

## Registry Safety

| Registry | Blocks Used | Safety Gate |
|----------|-------------|-------------|
| shadcn official | none | 不适用 |
| third-party | none | 不适用 |

说明：本阶段技术栈为 Vue 3 + Element Plus，不涉及 shadcn registry 或第三方 UI block 引入。

---

## Checker Sign-Off

- [ ] Dimension 1 Copywriting: PASS
- [ ] Dimension 2 Visuals: PASS
- [ ] Dimension 3 Color: PASS
- [ ] Dimension 4 Typography: PASS
- [ ] Dimension 5 Spacing: PASS
- [ ] Dimension 6 Registry Safety: PASS

**Approval:** pending
