# 经验沉淀

- 开题报告中的“技术趋势”与“文献综述”不能直接等价为实现范围，真正范围要以“本课题要研究或解决的问题”和“系统设计与实现路线”为准。
- 对毕业设计类平台，先做 MVP 边界收敛，再做骨架，效率明显高于一开始追求大而全。
- 在无 Maven 的 Windows 环境中，优先准备 `mvnw.cmd` 兼容路径和 IDEA 内置 Maven 兜底说明。
- 对需要“执行记录 + 明细结果”的页面，数据库优先拆成主表和明细表，通常比把所有信息塞进单表更稳，后续接口和页面都更容易设计。
- 当正式前端技术路线已经确定、但后端仍是早期 demo 接口时，先在前端 API 层做字段归一化适配，比强行同步改两端更稳，能显著降低联调阻塞。
- 当前数据库表结构已经预留了 `metric_code`，将“单温度预测”收口为“多指标独立预测”时，优先复用现有表结构和阈值字段，而不是贸然改表，能显著降低联调成本。
- 对仪表盘这类总览页，优先先补真实统计、最近记录和健康度，再决定是否继续补更复杂聚合，能更快形成答辩可展示闭环。
- Vue 3 组合式工具若直接返回 `ref` / `computed` 给复杂三方组件（如 Element Plus `el-table` / `el-pagination`），容易在运行时被当成非数组或非原始值，触发 `data2 is not iterable`、`undefined.sourceType` 一类白屏错误；对这类通用 composable，优先在返回层使用 `proxyRefs` 做自动解包，并在渲染前对列表数据做一次 `filter(Boolean)` 兜底更稳。
- **主内容区慎用 `el-scrollbar` 与 ECharts 同屏：** Element Plus `el-scrollbar` 会对内容做 `ResizeObserver` 并在 `onUpdated` 里 `update()`；ECharts 也会监听容器尺寸。二者再叠上 flex 默认 `min-width: auto` 时，可能出现**横向宽度反复修正、甚至持续变宽**。侧栏等非图表主链仍可保留 `el-scrollbar`；**包一整页主内容 + 图表**时，优先**原生 `overflow-y: auto` + `overflow-x: hidden`**，并对 `.console-body` / `el-main` /栅格列等保持 **`min-width: 0`** 与 **`flex: 1 1 0`**。图表内在布局稳定后（如 `requestAnimationFrame`）再 **`resize` 一次并关闭动画**更稳。详见 `bug-log.md` **BUG-2026-04-10-001**。
