---
status: pending
phase: 08-zzz-prompt-debug-origin
verified: null
source:
  - .planning/phases/08-zzz-prompt-debug-origin/08-01-PLAN.md
  - .planning/phases/08-zzz-prompt-debug-origin/08-02-PLAN.md
  - .planning/phases/08-zzz-prompt-debug-origin/08-CONTEXT.md
  - .planning/phases/08-zzz-prompt-debug-origin/08-UI-SPEC.md
---

# Phase 08 目标验证

## Phase 目标

将现有公开路由 `/screen` 升级为适合答辩展示的 **信息型大屏**：优先解决白屏 / 卡住风险，在 **上总览 / 中图表 / 下明细** 骨架下优先使用真实数据展示 **粮温趋势、预警趋势、预测趋势、仓库对比**，并保留 **最新预警、最近预测任务、重点仓库、说明** 等明细。

## must_haves.truths（对照 PLAN）

| 条目 | 证据（实现后填写） |
|------|-------------------|
| `/api/dashboard/screen` 存在，且支持 `warehouseId`、`startTime`、`endTime` | `DashboardController` / `DashboardService` / `DashboardMapper.xml` |
| 前端存在 `fetchScreenDashboard`，并规范化 `grainTrend`、`alertTrend`、`predictionTrend`、`warehouseComparison`、`latestPredictionTasks` | `frontend/src/api/grain.js` |
| `BigScreenView.vue` 不再依赖 `mockScreenMetrics` / `mockScreenAlerts`，首屏先渲染骨架和 CTA | `frontend/src/views/BigScreenView.vue` |
| 中部图表区存在 `粮温趋势 / 预警趋势 / 预测趋势 / 仓库对比` 四块真实信息位 | `frontend/src/views/BigScreenView.vue` |
| 底部明细区存在 `最新预警 / 最近预测任务 / 重点仓库 / 说明`，且说明块不再占主视觉 | `frontend/src/views/BigScreenView.vue` / `frontend/src/styles.css` |
| 图表生命周期安全：容器稳定后初始化、卸载时 dispose、模块失败不导致整页白屏 | `frontend/src/views/BigScreenView.vue` |

## 自动化

- （执行后）`cd backend && mvn -q -DskipTests compile`
- （执行后）`cd frontend && npm run build`

## 需求追溯

- `SCREEN-08-01`
- `SCREEN-08-02`
- `SCREEN-08-03`

## 手工（建议）

- 打开 `/screen`，确认先看到大屏壳层与操作按钮，而不是白屏
- 切换仓库与时间范围，确认四块图表和底部最近预测任务响应共享查询条件
- 断开某个模块数据或制造空数据，确认只出现局部空状态 / 错误提示，页面其余部分仍可见
- 在桌面宽屏和小屏下各验证一次布局，不回退为旧的“展示说明 + 预警表”结构

## Gaps

执行前留空；完成后更新 `status`、`verified` 与具体验证证据。
