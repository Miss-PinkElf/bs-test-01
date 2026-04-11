---
phase: 09-mock
plan: 01
subsystem: backend
tags: [users, warehouses, predictions, pagination, spring-boot, mybatis]
requires:
  - phase: 08-zzz-prompt-debug-origin
    provides: Dashboard / screen 真实接口基线
provides:
  - `/api/users/page` 与 `/api/users/stats`
  - `/api/warehouses/page` 与 `/api/warehouses/stats`
  - `/api/predictions/tasks/page`
  - 用户、仓库、预测任务统一 `keyword + pageNum + pageSize` 后端分页契约
affects: [users-view, warehouse-view, prediction-view, dashboard]
tech-stack:
  added:
    - backend/src/main/java/com/grain/platform/dto/user/UserListStatsResponse.java
    - backend/src/main/java/com/grain/platform/dto/warehouse/WarehouseStatsResponse.java
  patterns: [page-result, count-plus-limit-offset, page-level-keyword-search]
key-files:
  created:
    - backend/src/main/java/com/grain/platform/dto/user/UserListStatsResponse.java
    - backend/src/main/java/com/grain/platform/dto/warehouse/WarehouseStatsResponse.java
  modified:
    - backend/src/main/java/com/grain/platform/controller/UserController.java
    - backend/src/main/java/com/grain/platform/controller/WarehouseController.java
    - backend/src/main/java/com/grain/platform/controller/PredictionController.java
    - backend/src/main/java/com/grain/platform/service/UserService.java
    - backend/src/main/java/com/grain/platform/service/WarehouseService.java
    - backend/src/main/java/com/grain/platform/service/PredictionService.java
    - backend/src/main/java/com/grain/platform/mapper/UserMapper.java
    - backend/src/main/java/com/grain/platform/mapper/WarehouseMapper.java
    - backend/src/main/java/com/grain/platform/mapper/PredictionTaskMapper.java
    - backend/src/main/resources/mapper/UserMapper.xml
    - backend/src/main/resources/mapper/WarehouseMapper.xml
    - backend/src/main/resources/mapper/PredictionTaskMapper.xml
key-decisions:
  - "保留 `/api/users`、`/api/warehouses`、`/api/predictions/tasks` 旧全量接口兼容既有调用，同时新增分页接口给管理端页面切换使用"
  - "PredictionView 分页接口继续返回完整 `PredictionTaskResponse`，仅分页 task 主表，不拆详情接口"
patterns-established:
  - "管理端列表页统一采用 `PageResult<T>` + keyword 模式，与 DataView 已有分页范式对齐"
  - "summary cards 统计改由后端聚合接口提供，不再从当前页行数推导"
requirements-completed: [MOCK-09-01, MOCK-09-02]
duration: 25 min
completed: 2026-04-11
---

# Phase 09 Plan 01: 后端分页与统计接口 Summary

**补齐 Users / Warehouse / Prediction 的服务端分页与统计接口，为管理端四个页面切换到真实分页链路提供后端契约。**

## Performance

- **Duration:** 25 min
- **Completed:** 2026-04-11
- **Tasks:** 3
- **Files modified:** 14

## Accomplishments
- 用户页新增 `/api/users/page` 与 `/api/users/stats`，保留旧 `/api/users` 兼容调用。
- 仓库页新增 `/api/warehouses/page` 与 `/api/warehouses/stats`，保留 `/api/warehouses` 与 `/api/warehouses/options`。
- 预测记录新增 `/api/predictions/tasks/page`，继续返回完整 `PredictionTaskResponse` 与 `resultList`。
- 三组分页接口全部复用 `PageResult<T>`、`count + limit/offset + 关键字查询` 模式。

## Verification

- `cd backend && mvn -q -DskipTests compile`：passed
- `rg "/users/page|/users/stats|/warehouses/page|/warehouses/stats|/tasks/page" backend/src/main/java/com/grain/platform/controller`
- `rg "countUserPage|selectUserPage|countPage|selectPage" backend/src/main/resources/mapper/UserMapper.xml backend/src/main/resources/mapper/WarehouseMapper.xml backend/src/main/resources/mapper/PredictionTaskMapper.xml`

## Notes

- 本次执行未做 git 原子提交；仓库存在既有脏工作树与并行文档变更，因此仅完成文件落盘与编译验证。
- 用户、仓库 summary cards 的统计口径已切换为真实后端聚合，不再依赖当前页条数。
