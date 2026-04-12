---
status: human_needed
phase: 13-zzz-prompt-debug-origin
updated: 2026-04-12
requirements:
  - AUTHZ-13-01
  - AUTHZ-13-02
  - AUTHZ-13-03
  - AUTHZ-13-04
---

# Phase 13 Verification

## Goal

验证“角色菜单与权限区分（前端菜单 + 后端权限）”是否已在代码层落地，并且能通过后续人工 UAT 覆盖 admin / manager / viewer 三类账号的菜单、路由、读写权限与越权场景。

## Automated Checks

- [x] `cd frontend && npm run build`
- [x] `cd backend && mvn -q -DskipTests compile`
- [x] `rg "isAdmin|isWarehouseManager|isViewer|managedWarehouseId|defaultRoute|canAccessRoute" frontend/src/stores/auth.js`
- [x] `rg "allowedRoles|defaultRoute|canAccessRoute|showInNav" frontend/src/router/index.js frontend/src/layout/ConsoleLayout.vue frontend/src/views/LoginView.vue`
- [x] `rg "managedWarehouseId|isViewer|isWarehouseManager|canWriteData|visibleWarehouses|canExecutePrediction|canDeletePrediction" frontend/src/views/DataView.vue frontend/src/views/PredictionView.vue`
- [x] `rg "X-Demo-Username|requireCurrentUser|resolveWarehouseScope|assertWarehouseWriteAccess" backend/src/main/java/com/grain/platform/controller`
- [x] `rg "1L" backend/src/main/java/com/grain/platform/service/GrainTempService.java backend/src/main/java/com/grain/platform/service/SensorDataService.java backend/src/main/java/com/grain/platform/service/GrainTempImportService.java backend/src/main/java/com/grain/platform/service/PredictionService.java`

## Code Evidence

- 菜单 / 路由 / 默认落点：`frontend/src/stores/auth.js`、`frontend/src/router/index.js`、`frontend/src/layout/ConsoleLayout.vue`、`frontend/src/views/LoginView.vue`
- 查看者只读与 manager 锁仓：`frontend/src/views/DataView.vue`、`frontend/src/views/PredictionView.vue`
- 权限基础设施与 403 通道：`backend/src/main/java/com/grain/platform/security/AccessControlService.java`、`backend/src/main/java/com/grain/platform/common/ForbiddenException.java`、`backend/src/main/java/com/grain/platform/common/GlobalExceptionHandler.java`
- 平台管理 admin-only：`backend/src/main/java/com/grain/platform/controller/UserController.java`、`backend/src/main/java/com/grain/platform/controller/WarehouseController.java`
- 业务接口 role / warehouse scope：`backend/src/main/java/com/grain/platform/controller/GrainTempController.java`、`backend/src/main/java/com/grain/platform/controller/SensorDataController.java`、`backend/src/main/java/com/grain/platform/controller/PredictionController.java`、`backend/src/main/java/com/grain/platform/controller/DashboardController.java`

## Requirement Verdict

| Requirement | Verdict | Notes |
| --- | --- | --- |
| AUTHZ-13-01 | passed | 菜单、路由守卫、默认落点均由单一角色能力模型驱动 |
| AUTHZ-13-02 | passed | manager 锁仓、viewer 只读在前后端均已接入 |
| AUTHZ-13-03 | passed | 后端统一接入 current user、403 语义与真实操作者审计字段 |
| AUTHZ-13-04 | passed | SUMMARY / ROADMAP / STATE / VERIFICATION / HUMAN-UAT 已补齐 |

## Human Verification Needed

1. `admin / 123456` 登录后应默认进入 `/dashboard`，可见全部后台菜单，并能完成用户管理、仓库管理、环境写操作、预测执行与删除。
2. `manager_a01 / 123456` 登录后应默认进入 `/environment`，菜单不出现用户管理/仓库管理，环境页与预测页仓库选择固定为所属仓库，无法越仓查看或写入。
3. `viewer_demo / 123456` 登录后应默认进入 `/dashboard`，仍可浏览仪表盘 / 环境数据 / 温度预测，但看不到新增、编辑、删除、导入、执行预测等入口；直接访问无权限后台 URL 时不能停在错误页。

## Conclusion

自动化验证已通过，Phase 13 已达到代码级完成条件；当前剩余阻塞为按账号人工验收，因此状态标记为 `human_needed`。
