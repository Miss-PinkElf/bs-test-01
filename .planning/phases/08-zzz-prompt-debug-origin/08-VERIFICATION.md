---
status: human_needed
phase: 08-zzz-prompt-debug-origin
verified: 2026-04-11
source:
  - .planning/phases/08-zzz-prompt-debug-origin/08-01-PLAN.md
  - .planning/phases/08-zzz-prompt-debug-origin/08-02-PLAN.md
  - .planning/phases/08-zzz-prompt-debug-origin/08-01-SUMMARY.md
  - .planning/phases/08-zzz-prompt-debug-origin/08-02-SUMMARY.md
  - .planning/phases/08-zzz-prompt-debug-origin/08-CONTEXT.md
  - .planning/phases/08-zzz-prompt-debug-origin/08-UI-SPEC.md
---

# Phase 08 目标验证

## Phase 目标

将现有公开路由 `/screen` 升级为适合答辩展示的 **信息型大屏**：优先解决白屏 / 卡住风险，在 **上总览 / 中图表 / 下明细** 骨架下优先使用真实数据展示 **粮温趋势、预警趋势、预测趋势、仓库对比**，并保留 **最新预警、最近预测任务、重点仓库、说明** 等明细。

## must_haves.truths（对照 PLAN）

| 条目 | 证据 |
|------|------|
| `/api/dashboard/screen` 存在，且支持 `warehouseId`、`startTime`、`endTime` | `backend/src/main/java/com/grain/platform/controller/DashboardController.java`、`backend/src/main/java/com/grain/platform/service/DashboardService.java`、`backend/src/main/resources/mapper/DashboardMapper.xml` |
| 前端存在 `fetchScreenDashboard`，并规范化 `grainTrend`、`alertTrend`、`predictionTrend`、`warehouseComparison`、`latestPredictionTasks` | `frontend/src/api/grain.js` |
| `BigScreenView.vue` 不再依赖 `mockScreenMetrics` / `mockScreenAlerts`，首屏先渲染骨架和 CTA | `frontend/src/views/BigScreenView.vue` |
| 中部图表区存在 `粮温趋势 / 预警趋势 / 预测趋势 / 仓库对比` 四块真实信息位 | `frontend/src/views/BigScreenView.vue` |
| 底部明细区存在 `最新预警 / 最近预测任务 / 重点仓库 / 说明`，且说明块不再占主视觉 | `frontend/src/views/BigScreenView.vue`、`frontend/src/styles.css` |
| 图表生命周期安全：容器稳定后初始化、卸载时 dispose、模块失败不导致整页白屏 | `frontend/src/views/BigScreenView.vue` |

## 自动化

- `cd backend && mvn -q -DskipTests compile`：通过（2026-04-11）
- `cd frontend && npm run build`：通过（2026-04-11）
- 结构性命中检查：`BigScreenView.vue` 已命中 `fetchScreenDashboard`、四个图表标题、四个明细标题、`echarts.init`、`dispose()`、`requestAnimationFrame`

## 需求追溯

- `SCREEN-08-01`
- `SCREEN-08-02`
- `SCREEN-08-03`

## Human Verification

1. 打开 `/screen`
   expected: 首屏先看到标题、按钮、共享筛选与模块骨架，而不是白屏。
2. 切换仓库与时间范围后点击“刷新大屏”
   expected: 顶部指标、四块图表和“最近预测任务”按共享条件刷新。
3. 检查中部图表区
   expected: 存在“粮温趋势、预警趋势、预测趋势、仓库对比”四块稳定信息位，粮温趋势为主图。
4. 检查底部明细区
   expected: 存在“最新预警、最近预测任务、重点仓库、说明”，说明区只占次要位置。
5. 在较窄窗口下查看 `/screen`
   expected: 图表与明细纵向堆叠，不出现整体溢出或白屏。

## Gaps

- 自动化与代码审查层面已完成；最终验收仍需要人工打开 `/screen` 进行 UI/UAT 确认。
