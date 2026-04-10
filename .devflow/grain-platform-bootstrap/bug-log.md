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

---

## BUG-2026-04-10-002：为压制横向无限变宽而全局关闭主内容 `overflow-x`，导致正常超宽内容无横向滚动且预测摘要被裁切

### 问题现象

- 在 **BUG-2026-04-10-001** 收口后，管理端主内容区改为 `.console-main-native` 原生滚动，并设置 **`overflow-x: hidden`**。
- 结果是：虽然“页面横向持续变宽”被止住，但**所有后台页面在真实超宽时也失去了正常横向滚动能力**。
- 在 `/prediction` 页，右侧「任务摘要」使用 `el-descriptions` 展示较长的时间区间与摘要文本时，会在部分宽度下出现**内容横向溢出后被直接裁切**，用户只能看到右边露出一截的灰条，无法正常拖动或阅读。

### 问题原因

1. **BUG-2026-04-10-001 的止血方案过于全局化**：为避免 `el-scrollbar + ECharts + ResizeObserver` 的横向尺寸反馈，直接在主内容容器层面禁用了所有横向滚动，而不是仅隔离高风险节点。
2. 预测页底部宽表已通过 `.prediction-history-table-wrap { overflow-x: auto; }` 做了**局部横向滚动**，但右侧摘要卡片仍然沿用 `el-descriptions` 表格式布局，且没有独立 wrapper。
3. `el-descriptions` / 卡片 body / 栅格列在窄列中若未同步补足 **`min-width: 0`**、内容换行与局部滚动保护，就会把真实溢出表现为“被上层 `overflow-x: hidden` 吃掉”，从而掩盖“这是局部内容超宽，不是整页继续失控”的区别。

### 解决方案

1. **布局层改为“允许正常横向滚动”**：保留主内容原生滚动，不把 `el-scrollbar` 放回主内容区；但将 `.console-main-native` 从 **`overflow-x: hidden`** 调整为 **`overflow-x: auto`**，让真实超宽场景恢复正常横向滚动。
2. **组件层负责避免误撑宽**：
   - 预测记录表继续保留局部 wrapper 横向滚动；
   - 任务摘要卡片新增 `task-summary-card` 与 `task-summary-table-wrap`，卡片 body / wrapper / descriptions 统一补 **`min-width: 0`**；
   - `el-descriptions__table` 使用 **`table-layout: fixed`**，内容单元格开启 **`overflow-wrap: anywhere`** / `word-break: break-word`，避免长时间串把列无限撑宽。
3. **维持上个 bug 的核心规避点**：主内容层依旧不回退到 `el-scrollbar`，避免重新把高风险的滚动条更新链与图表 resize 绑在一起。

### 关联

- **代码：** `frontend/src/styles.css`、`frontend/src/views/PredictionView.vue`
- **上游问题：** `BUG-2026-04-10-001`
- **验证：** `frontend/` 执行 `npm run build` 通过
