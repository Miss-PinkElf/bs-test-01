# Phase 6 UI 设计约定（overflow-x）

**Status:** 与 `06-CONTEXT.md` 对齐  
**页面:** `frontend/src/layout/ConsoleLayout.vue`、`frontend/src/views/PredictionView.vue`

## 主内容区

- 主内容继续由 `.console-main-native` 承载滚动。
- 默认行为应为：
  - 纵向内容长时，主内容区正常纵向滚动
  - 横向内容真实超宽时，主内容区允许横向滚动
- 本阶段**不**恢复 `el-scrollbar` 到主内容层。

## 预测记录表

- 保持局部横向滚动容器：`.prediction-history-table-wrap`
- 表本体可维持大于容器的最小宽度；滚动责任在 wrapper，而非整页兜底

## 任务摘要

- 任务摘要卡片须可在 `md` / `lg` 右侧窄列中稳定成立
- 约束：
  - card body 可收缩
  - `el-descriptions` 表格固定布局
  - 内容单元格允许换行 / 断词
  - 必要时由 `task-summary-table-wrap` 提供局部横向滚动
- 不允许再出现“摘要右侧被裁切但用户无明确滚动入口”的状态

## 风险约束

- 不以“恢复整页 `el-scrollbar`”作为解法
- 不破坏既有图表稳定性修复

## 参考

- `.planning/phases/06-overflow-x/06-CONTEXT.md`
- `.devflow/grain-platform-bootstrap/bug-log.md`
