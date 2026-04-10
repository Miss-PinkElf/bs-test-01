# 管理端布局：侧栏与主区独立滚动 + el-scrollbar

## 背景（Mini Align）

- **目标**：后台页左侧导航固定视口内区域，右侧主内容单独滚动；与 Element Plus [Container 布局容器](https://element-plus.org/zh-CN/component/container) 常见用法一致。
- **范围**：仅 `frontend/src/layout/ConsoleLayout.vue` 与 `frontend/src/styles.css`（含小屏断点）。
- **不做**：不改路由、不改业务页内容、不调整大屏 `/screen` 独立布局（除非后续单独立项）。

## 问题记录（现象 / 原因 / 解决）

| 项 | 内容 |
| --- | --- |
| **现象** | 用户向下滚动时，左侧侧栏与右侧主体一起移动，呈现「整页一体滚动」。 |
| **原因** | 虽已使用 `el-container` / `el-aside` / `el-main`，但外层布局仅用 `min-height: 100vh`、未限制视口高度与内部滚动分工；主区内容增高时整体容器被撑高，由**文档根滚动条**统一滚动。并非未使用组件库。 |
| **解决** | ① `html`/`body`/`#app` 高度链 + `.console-shell` 视口限高（如 `max-height: 100vh`）与 `overflow: hidden`；② 右侧 `.console-body` / `.console-main` 使用 `flex: 1`、`min-height: 0`、`overflow: hidden`；③ 侧栏中部与主内容按官方示例套 `el-scrollbar`，主区内边距落在 `.el-scrollbar__view`；④ `≤960px` 时恢复块级布局与自然滚动，避免移动端被裁切。 |

## 轻量任务（已完成）

- [x] 视口锁定与右侧主区独立滚动
- [x] 侧栏 `el-scrollbar` + 主区 `el-scrollbar`
- [x] `npm run build` 验证

## 参考文件

- `frontend/src/layout/ConsoleLayout.vue`
- `frontend/src/styles.css`（`.console-shell`、`.console-aside`、`.aside-scroll`、`.console-body`、`.console-main`、`.console-main-scroll`、小屏媒体查询）
