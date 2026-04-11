---
phase: 08-zzz-prompt-debug-origin
plan: 01
subsystem: api
tags: [dashboard, spring-boot, mybatis, vue-api, screen]
requires:
  - phase: 07-zzz-prompt-debug-origin-prompt-md
    provides: 粮温汇总时间范围与预测任务真实数据基础
provides:
  - `/api/dashboard/screen` 公开大屏聚合接口
  - 大屏趋势、仓库对比、最近预测任务后端数据契约
  - `fetchScreenDashboard()` 前端 API 封装
affects: [08-02-screen-ui, dashboard, screen]
tech-stack:
  added: []
  patterns: [dashboard-screen-endpoint, screen-specific-response-dtos]
key-files:
  created:
    - backend/src/main/java/com/grain/platform/dto/dashboard/ScreenDashboardResponse.java
    - backend/src/main/java/com/grain/platform/dto/dashboard/ScreenTrendPointResponse.java
    - backend/src/main/java/com/grain/platform/dto/dashboard/ScreenWarehouseCompareResponse.java
    - backend/src/main/java/com/grain/platform/dto/dashboard/ScreenPredictionTaskItemResponse.java
  modified:
    - backend/src/main/java/com/grain/platform/controller/DashboardController.java
    - backend/src/main/java/com/grain/platform/service/DashboardService.java
    - backend/src/main/java/com/grain/platform/mapper/DashboardMapper.java
    - backend/src/main/resources/mapper/DashboardMapper.xml
    - frontend/src/api/grain.js
key-decisions:
  - "为 /screen 新增独立的 /api/dashboard/screen 聚合接口，避免污染现有 fetchOverview 口径"
  - "大屏 overview 继续沿用 DashboardOverviewResponse，趋势与任务区改为 screen 专属 DTO"
patterns-established:
  - "DashboardService 同时维护 overview 与 screen 两条聚合路径，避免仪表盘回归"
  - "前端大屏 API 统一在 grain.js 内 normalize，不把字段兜底散到页面层"
requirements-completed: [SCREEN-08-01, SCREEN-08-03]
duration: 10 min
completed: 2026-04-11
---

# Phase 08 Plan 01: 大屏真实聚合接口 Summary

**新增 `/api/dashboard/screen` 聚合接口与 `fetchScreenDashboard()` 封装，为 `/screen` 提供真实趋势、对比和最近预测任务数据底座**

## Performance

- **Duration:** 10 min
- **Started:** 2026-04-11T04:09:00Z
- **Completed:** 2026-04-11T04:19:11Z
- **Tasks:** 3
- **Files modified:** 9

## Accomplishments
- 为大屏增加独立的 screen 聚合响应模型与公开查询接口
- 在 DashboardMapper / XML 中补齐大屏趋势、仓库对比、最近预测任务与过滤统计查询
- 在 `frontend/src/api/grain.js` 中新增 `fetchScreenDashboard()` 与统一 normalize 逻辑

## Task Commits

Each task was committed atomically:

1. **Task 1-2: 后端 screen 聚合接口与查询实现** - `a3e5786` (feat)
2. **Task 3: 前端大屏 API 封装** - `b4868e4` (feat)

**Plan metadata:** pending

## Files Created/Modified
- `backend/src/main/java/com/grain/platform/dto/dashboard/ScreenDashboardResponse.java` - 大屏聚合响应根对象
- `backend/src/main/java/com/grain/platform/dto/dashboard/ScreenTrendPointResponse.java` - 趋势图统一点位结构
- `backend/src/main/java/com/grain/platform/dto/dashboard/ScreenWarehouseCompareResponse.java` - 仓库对比图响应结构
- `backend/src/main/java/com/grain/platform/dto/dashboard/ScreenPredictionTaskItemResponse.java` - 最近预测任务响应结构
- `backend/src/main/java/com/grain/platform/controller/DashboardController.java` - 新增 `/api/dashboard/screen` 入口
- `backend/src/main/java/com/grain/platform/service/DashboardService.java` - 新增 screen 聚合组装逻辑
- `backend/src/main/java/com/grain/platform/mapper/DashboardMapper.java` - 声明大屏统计与趋势查询方法
- `backend/src/main/resources/mapper/DashboardMapper.xml` - 实现大屏统计、趋势、仓库对比与最近预测任务 SQL
- `frontend/src/api/grain.js` - 新增 `fetchScreenDashboard()` 与 screen normalize 函数

## Decisions Made
- 为避免 DashboardView 现有 `fetchOverview()` 回归，screen 数据走独立 `/api/dashboard/screen` 路由而不是修改原接口返回结构。
- screen 聚合仍复用 `DashboardOverviewResponse` 作为 overview 容器，减少前端顶部指标与底部明细的二次适配成本。
- 趋势与横向对比数据统一落成 screen DTO，保证 Wave 2 的 `BigScreenView.vue` 只消费稳定契约。

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] `apply_patch` 在当前 Windows 沙箱中不可用**
- **Found during:** Task 1（后端 DTO 与接口落地）
- **Issue:** 标准补丁工具持续返回 sandbox setup refresh failed，无法正常写入文件
- **Fix:** 改为使用 PowerShell 定向写文件与精确替换，保持修改范围只落在计划文件指定路径
- **Files modified:** backend/src/main/java/com/grain/platform/controller/DashboardController.java, backend/src/main/java/com/grain/platform/service/DashboardService.java, backend/src/main/java/com/grain/platform/mapper/DashboardMapper.java, backend/src/main/resources/mapper/DashboardMapper.xml, frontend/src/api/grain.js
- **Verification:** `mvn -q -DskipTests compile` 与 `npm run build` 均通过
- **Committed in:** `a3e5786`, `b4868e4`

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** 仅影响写文件方式，不影响目标范围与交付结果。

## Issues Encountered
- 第一次后端提交命中 `.git/index.lock`，等待残留 git 进程退出后重试成功。

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Wave 1 的真实数据底座已经具备，`BigScreenView.vue` 可以直接消费 `fetchScreenDashboard()` 进入三段式重构。
- 仍需要 Wave 2 落地真实图表、底部明细区与首屏稳定壳层，Phase 8 才能算完成。

---
*Phase: 08-zzz-prompt-debug-origin*
*Completed: 2026-04-11*
