---
phase: 13-zzz-prompt-debug-origin
plan: 03
subsystem: api
tags: [spring, mybatis, authz, warehouse-scope, audit]
requires:
  - phase: 13-zzz-prompt-debug-origin
    provides: AccessControlService、CurrentUserContext 与显式 403 语义
provides:
  - 平台管理接口 admin-only 门禁
  - 粮温/环境/预测接口按角色与仓库范围收口
  - 写入链路操作者 id 改为真实当前用户
  - 仪表盘后台接口 manager 单仓视图
affects: [user-management, warehouse-management, grain-temp-api, sensor-data-api, prediction-api, dashboard-api]
tech-stack:
  added: []
  patterns: [header-based-current-user, controller-level-scope-resolution, real-operator-audit]
key-files:
  created: []
  modified:
    - backend/src/main/java/com/grain/platform/controller/UserController.java
    - backend/src/main/java/com/grain/platform/controller/WarehouseController.java
    - backend/src/main/java/com/grain/platform/controller/GrainTempController.java
    - backend/src/main/java/com/grain/platform/controller/SensorDataController.java
    - backend/src/main/java/com/grain/platform/controller/PredictionController.java
    - backend/src/main/java/com/grain/platform/controller/DashboardController.java
    - backend/src/main/java/com/grain/platform/service/WarehouseService.java
    - backend/src/main/java/com/grain/platform/service/GrainTempService.java
    - backend/src/main/java/com/grain/platform/service/GrainTempImportService.java
    - backend/src/main/java/com/grain/platform/service/SensorDataService.java
    - backend/src/main/java/com/grain/platform/service/PredictionService.java
    - backend/src/main/java/com/grain/platform/service/DashboardService.java
key-decisions:
  - "继续沿用轻量 demo header 方案，在控制器层统一解析 current user 并收口 warehouse scope，而不是在每个 service 内重复拼权限分支"
  - "manager 的分页预测列表与仪表盘 scoped 数据优先用现有 mapper/结果集做单仓过滤与内存分页，避免为了当前 phase 扩大到大量 SQL 重写"
  - "删除类 path-id 接口先取真实记录/任务仓库归属，再做写权限判断，避免只看前端传参导致越仓删除"
patterns-established:
  - "后台控制器接收 X-Demo-Username 后，先 requireCurrentUser，再按接口类型选择 resolveWarehouseScope 或 assertWarehouseWriteAccess"
  - "审计字段 createdBy/requestedBy 一律由控制器传入真实 current user id，不允许再写死 1L"
requirements-completed: [AUTHZ-13-02, AUTHZ-13-03]
duration: 24 min
completed: 2026-04-12
---

# Phase 13 Plan 03 Summary

**把平台管理、粮温、环境、预测和仪表盘后台接口全部收口到统一的角色/仓库权限语义，并把写入操作者从硬编码 `1L` 切到真实当前用户。**

## Performance

- **Duration:** 24 min
- **Completed:** 2026-04-12
- **Tasks:** 3
- **Files modified:** 12

## Accomplishments

- `UserController` 与 `WarehouseController` 统一接入 `X-Demo-Username`，用户管理与仓库维护相关接口改为 admin-only；仓库列表/选项对 manager 自动收口到自己的仓库。
- `GrainTempController`、`SensorDataController`、`PredictionController` 全部接入 current user，上读接口统一经过 `resolveWarehouseScope`，写接口统一经过 `assertWarehouseWriteAccess`。
- 粮温、环境和预测写入链路的 `createdBy / requestedBy` 已切到真实 `currentUser.userId`，导入链路也同步改掉了原来的 `1L` 硬编码。
- `PredictionController` 的详情、删除和批量删除都先按任务真实 `warehouseId` 做权限判断，避免 manager 越仓查看或删除。
- `DashboardController` 与 `DashboardService` 已让 manager 只看到自己仓库的后台概览、预警、健康度和粮温汇总；`/screen` 继续保持公开入口，不纳入后台权限门禁。

## Verification

- `cd backend && mvn -q -DskipTests compile`：passed
- `rg "1L" backend/src/main/java/com/grain/platform/service/GrainTempService.java backend/src/main/java/com/grain/platform/service/SensorDataService.java backend/src/main/java/com/grain/platform/service/GrainTempImportService.java backend/src/main/java/com/grain/platform/service/PredictionService.java`：passed（未命中操作者硬编码）
- `rg "X-Demo-Username|requireCurrentUser|resolveWarehouseScope|assertWarehouseWriteAccess" backend/src/main/java/com/grain/platform/controller/GrainTempController.java backend/src/main/java/com/grain/platform/controller/SensorDataController.java backend/src/main/java/com/grain/platform/controller/PredictionController.java backend/src/main/java/com/grain/platform/controller/DashboardController.java`：passed
- `rg "X-Demo-Username" backend/src/main/java/com/grain/platform/controller/UserController.java backend/src/main/java/com/grain/platform/controller/WarehouseController.java`：passed

## Task Commits

1. **Task 1: 平台管理接口改为仅管理员可用，仓库列表接口按角色收口** - `34650d5` (`feat`)
2. **Task 2: 粮温、环境数据与预测接口按角色和仓库范围收口** - `9ac5450` (`feat`)
3. **Task 3: 仪表盘后端读取范围与 403 语义对齐** - `e77599d` (`feat`)

## Notes

- manager 的单仓分页预测列表与后台仪表盘分页数据当前采用“基于现有 mapper 的单仓过滤/内存分页”方案，先满足 phase 13 的权限目标，没有扩展到额外 SQL 重写。
- `/api/dashboard/screen` 保持公开展示定位，只对后台 `/api/dashboard/*` 资源接入 current user 和 manager 单仓范围。
