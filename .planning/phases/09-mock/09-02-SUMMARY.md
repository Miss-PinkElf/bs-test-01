---
phase: 09-mock
plan: 02
subsystem: frontend
tags: [dashboard, users-view, warehouse-view, prediction-view, vue, pagination, mock-cleanup]
requires:
  - phase: 09-mock
    provides: 09-01 backend pagination and stats endpoints
provides:
  - Dashboard `overview + alerts/warehouse-health/grain-summaries` 独立分页链路
  - Users / Warehouse / Prediction / Dashboard 四页服务端分页化
  - 删除 `frontend/src/mock/platform.js`
  - Phase 9 文档追溯与验收入口
affects: [dashboard, users-view, warehouse-view, prediction-view, planning-docs]
tech-stack:
  added: []
  patterns: [module-level-loading-error, server-driven-pagination, no-runtime-mock-fallback]
key-files:
  created: []
  modified:
    - backend/src/main/java/com/grain/platform/controller/DashboardController.java
    - backend/src/main/java/com/grain/platform/service/DashboardService.java
    - backend/src/main/java/com/grain/platform/mapper/DashboardMapper.java
    - backend/src/main/resources/mapper/DashboardMapper.xml
    - frontend/src/api/grain.js
    - frontend/src/views/DashboardView.vue
    - frontend/src/views/UsersView.vue
    - frontend/src/views/WarehouseView.vue
    - frontend/src/views/PredictionView.vue
    - .planning/ROADMAP.md
    - .planning/REQUIREMENTS.md
    - .planning/STATE.md
key-decisions:
  - "Dashboard overview 只保留顶部聚合指标，下方三块列表全部走独立分页接口和独立 loading/error"
  - "WarehouseView 保留列表驱动详情，但翻页时通过快照保留当前选中仓库，不因当前页缺失而立刻清空"
  - "PredictionView 切换到服务端分页后，当前任务摘要和图表仍保持页面级状态，只在删除当前任务时清空"
patterns-established:
  - "管理端模块级错误态隔离：单模块失败不拖垮整页 overview"
  - "mock 文件删除后直接显示真实接口错误，不再用本地假数据兜底"
requirements-completed: [MOCK-09-03, MOCK-09-04]
duration: 40 min
completed: 2026-04-11
---

# Phase 09 Plan 02: 前端分页迁移与 Dashboard 收口 Summary

**完成 Dashboard 拆分分页、四个管理端页面回源分页，并清理运行时 mock 依赖。**

## Performance

- **Duration:** 40 min
- **Completed:** 2026-04-11
- **Tasks:** 4
- **Files modified:** 12

## Accomplishments
- Dashboard 拆为 `fetchOverview()` + `fetchDashboardAlertsPage()` + `fetchDashboardWarehouseHealthPage()` + `fetchDashboardGrainSummariesPage()`。
- `UsersView.vue`、`WarehouseView.vue`、`PredictionView.vue`、`DashboardView.vue` 全部移除前端分页主链，切页和 keyword 改为回源请求。
- `PredictionView.vue` 保持完整任务详情分页、摘要高亮、当前任务图表、多选删除与批量删除语义不回退。
- 删除 `frontend/src/mock/platform.js`，目标页面不再保留 mock / 演示型兜底文案。

## Verification

- `cd backend && mvn -q -DskipTests compile`：passed
- `cd frontend && npm run build`：passed
- `Test-Path 'frontend/src/mock/platform.js'`：False
- `rg "useClientPagination|useIncrementalList" frontend/src/views/UsersView.vue frontend/src/views/WarehouseView.vue frontend/src/views/PredictionView.vue frontend/src/views/DashboardView.vue`

## Notes

- Dashboard 三个分页模块各自维护 `keyword / pageNum / pageSize / total / loading / error`，已满足模块级隔离要求。
- 本次仅完成自动化验证；仍需要你手工打开 Users / Warehouse / Prediction / Dashboard 做最终 UAT，再将 Phase 9 正式标为完成。
