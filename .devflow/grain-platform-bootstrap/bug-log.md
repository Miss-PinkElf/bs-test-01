# Bug 记录（grain-platform-bootstrap）

> 格式：**现象** / **原因** / **解决方案** / **关联**  
> 与 `learnings.md` 配合：本文件保留可追溯案例，经验条目不重复堆砌细节。

---

## BUG-2026-04-10-001：管理端主内容区横向宽度持续增长（温度预测页最明显）

### 问题现象

- 进入 Vue 管理端后（尤以 **`/prediction` 温度预测** 为甚），页面**横向宽度持续增加**，右侧按钮、卡片等**不断被推向更右侧**，仿佛布局在无限变宽。
- 首轮仅通过 CSS为 flex /滚动视图补充 `min-width: 0`、约束 `.chart-box` 等，**未能完全消除**。

### 问题原因

1. **Element Plus `el-scrollbar`**（2.x）：实现中对内容视图注册了 **`useResizeObserver`**，且在 **`onUpdated` 钩子中调用 `update()`** 刷新滚动条状态。主内容区使用该组件时，任意子树更新都可能触发滚动条侧的更新链。
2. **ECharts 5**：对图表容器有**尺寸监听与自动 `resize`** 行为。
3. 二者与 **flex 布局默认 `min-width: auto`**（子项按内容最小宽度参与计算）叠加时，容易形成 **DOM 宽度 ↔ 滚动条/图表 resize** 的反馈，表现为横向尺寸**反复修正甚至持续外扩**。
4. 侧栏仍可使用 `el-scrollbar`；**问题集中在「主内容 +图表」同屏**场景。

### 解决方案

1. **主内容区不再使用 `el-scrollbar`**：在 `ConsoleLayout.vue` 中改为 **`div.console-main-native`**，使用 **`overflow-y: auto`**、**`overflow-x: hidden`**，并保留 **`min-width: 0`**、**`scrollbar-gutter: stable`**（`styles.css`）。
2. **收紧 flex**：`.console-body`、`.console-main` 使用 **`flex: 1 1 0`**；顶栏 `.console-header` 与子块补充 **`min-width: 0`** / **`flex-shrink: 0`**；`.page-stack`、`.el-col`、`.panel-card`、`.chart-box` 等继续约束最大宽度与溢出。
3. **预测页图表**：`PredictionView.vue` 在 `setOption` 之后于 **`requestAnimationFrame`** 内调用 **`chart.resize({ width: clientWidth, height: clientHeight, animation: { duration: 0 } })`**，减少与首帧布局叠加的抖动。

### 关联

- **代码：** `frontend/src/layout/ConsoleLayout.vue`、`frontend/src/styles.css`、`frontend/src/views/PredictionView.vue`
- **GSD quick：** `.planning/quick/260410-k32-vue-scrollbar-echarts-layout-fix/`
- **经验：** `.devflow/grain-platform-bootstrap/learnings.md`（同日期条目）
