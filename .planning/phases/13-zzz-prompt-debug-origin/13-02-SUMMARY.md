---
phase: 13-zzz-prompt-debug-origin
plan: 02
subsystem: frontend
tags: [vue, pinia, vue-router, role-menu, warehouse-scope]
requires:
  - phase: 13-zzz-prompt-debug-origin
    provides: demo 用户身份透传与 AccessControlService 权限基础设施
provides:
  - 后台菜单、路由守卫与登录默认落点按角色统一收口
  - 仓库管理员前端锁仓
  - 查看者只读页面体验
affects: [console-layout, auth-store, data-view, prediction-view]
tech-stack:
  added: []
  patterns: [single-role-capability-source, route-meta-allowed-roles, manager-warehouse-lock]
key-files:
  created: []
  modified:
    - frontend/src/stores/auth.js
    - frontend/src/router/index.js
    - frontend/src/layout/ConsoleLayout.vue
    - frontend/src/views/LoginView.vue
    - frontend/src/views/DataView.vue
    - frontend/src/views/PredictionView.vue
key-decisions:
  - "把角色能力判断集中到 auth store，路由守卫、菜单生成和登录跳转都消费同一套 allowedRoles/defaultRoute 规则"
  - "仓库管理员在 DataView 和 PredictionView 前端主动锁仓，不把越仓操作完全留给后端兜底"
  - "查看者继续保留环境数据页和预测页的浏览能力，但隐藏所有新增、导入、删除、执行预测等写入口"
patterns-established:
  - "后台路由必须显式声明 allowedRoles，再由菜单与守卫共同消费"
  - "共享业务页的写入口要按角色单独裁剪，不能只靠页面整体可见/不可见"
requirements-completed: [AUTHZ-13-01, AUTHZ-13-02]
duration: 18 min
completed: 2026-04-12
---

# Phase 13 Plan 02 Summary

**把后台菜单、路由守卫、登录默认落点以及环境数据/预测页面的可操作入口统一收口到 `ADMIN / WAREHOUSE_MANAGER / VIEWER` 三类角色语义。**

## Performance

- **Duration:** 18 min
- **Completed:** 2026-04-12
- **Tasks:** 2
- **Files modified:** 6

## Accomplishments

- `frontend/src/stores/auth.js` 提供 `isAdmin / isWarehouseManager / isViewer / managedWarehouseId / defaultRoute / canAccessRoute`，让角色能力模型集中在单一真相源。
- `frontend/src/router/index.js` 为后台子路由补 `allowedRoles` 与 `navOrder`，并在全局守卫中对登录页回跳、根路由默认落点和无权限访问统一兜底到角色默认页。
- `frontend/src/layout/ConsoleLayout.vue` 改为从路由元数据生成侧栏菜单，确保菜单展示与路由权限规则完全一致。
- `frontend/src/views/LoginView.vue` 登录成功后优先复用合法 redirect，否则按角色跳转到 `authStore.defaultRoute`。
- `frontend/src/views/DataView.vue` 为仓库管理员锁定仓库选择和表单仓库字段，并对查看者隐藏新增、导入、编辑、删除等写入口。
- `frontend/src/views/PredictionView.vue` 为仓库管理员锁定预测仓库，对查看者关闭执行预测与删除能力，同时保留任务摘要与历史查看。

## Verification

- `cd frontend && npm run build`：passed
- `rg "isAdmin|isWarehouseManager|isViewer|managedWarehouseId|defaultRoute|canAccessRoute" frontend/src/stores/auth.js`：passed
- `rg "allowedRoles|defaultRoute|canAccessRoute|showInNav" frontend/src/router/index.js frontend/src/layout/ConsoleLayout.vue frontend/src/views/LoginView.vue`：passed
- `rg "managedWarehouseId|isViewer|isWarehouseManager|canWriteData|visibleWarehouses|canExecutePrediction|canDeletePrediction" frontend/src/views/DataView.vue frontend/src/views/PredictionView.vue`：passed

## Task Commits

1. **Task 1: 统一角色能力模型、菜单生成与登录默认跳转** - `4a20bef` (`feat`)
2. **Task 2: 环境数据页与预测页按角色切换可操作能力** - `e7ded7c` (`feat`)

## Notes

- `PredictionView` 不再为查看者自动触发新的预测任务，而是优先展示已有历史任务，避免只读账号在页面初始化时产生写操作。
- 当前前端已经实现角色级体验收口；后续后端 plan `13-03` 负责把同样的角色/仓库语义落到接口门禁与审计字段上。
