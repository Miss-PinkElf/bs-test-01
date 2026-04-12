---
phase: 13-zzz-prompt-debug-origin
plan: 01
subsystem: auth
tags: [axios, vue, spring, authz, 403, demo-login]
requires: []
provides:
  - 前端后台请求统一透传 X-Demo-Username
  - 导入上传链路补齐当前 demo 用户身份
  - 后端 CurrentUserContext 与 AccessControlService 权限基础设施
  - 越权访问的 403 异常语义
affects: [frontend-authz, backend-authz, phase-13-wave-2]
tech-stack:
  added: []
  patterns: [demo-username-header, lightweight-access-control-service, explicit-403-semantics]
key-files:
  created:
    - backend/src/main/java/com/grain/platform/common/ForbiddenException.java
    - backend/src/main/java/com/grain/platform/security/CurrentUserContext.java
    - backend/src/main/java/com/grain/platform/security/AccessControlService.java
  modified:
    - frontend/src/api/http.js
    - frontend/src/api/grain.js
    - backend/src/main/java/com/grain/platform/common/GlobalExceptionHandler.java
key-decisions:
  - "继续沿用 demo 登录体系，只把用户名透传到后端查库解析，不引入 Spring Security 或第二套 token 机制"
  - "越权访问改为显式 403，缺少/无效身份仍维持参数类异常通道，避免把未登录与越权语义混在一起"
  - "导入上传虽然绕过 request()，仍复用同一 session 读取逻辑补齐 X-Demo-Username"
patterns-established:
  - "所有后台请求默认透传 demo 用户名，但登录接口显式排除"
  - "后端角色/仓库判断统一收敛到 AccessControlService，而不是在控制器和 service 中各自拼装"
requirements-completed: [AUTHZ-13-01, AUTHZ-13-03]
duration: 12 min
completed: 2026-04-12
---

# Phase 13 Plan 01 Summary

**为 demo 登录链路补齐跨前后端一致的当前用户身份透传、后端用户上下文与 403 权限异常语义，给后续角色菜单和接口门禁提供统一基础。**

## Performance

- **Duration:** 12 min
- **Completed:** 2026-04-12
- **Tasks:** 2
- **Files modified:** 6

## Accomplishments

- `frontend/src/api/http.js` 为所有非登录后台请求统一追加 `X-Demo-Username`，并在 `403` 时优先显示后端返回的权限消息。
- `frontend/src/api/grain.js` 为粮温/环境数据导入上传补齐相同的 demo 用户头，避免上传链路绕过身份透传。
- 后端新增 `CurrentUserContext`、`AccessControlService` 与 `ForbiddenException`，把当前用户解析、角色判断、仓库范围解析和写权限断言沉到同一层。
- `GlobalExceptionHandler` 新增 `ForbiddenException` 映射，越权访问现在能稳定返回 `ApiResponse.error(403, message)`。

## Verification

- `cd frontend && npm run build`：passed
- `cd backend && mvn -q -DskipTests compile`：passed
- `rg "X-Demo-Username|getSession|/api/auth/login" frontend/src/api/http.js frontend/src/api/grain.js`：passed
- `rg "ForbiddenException|403|record CurrentUserContext|requireCurrentUser|requireAdmin|resolveWarehouseScope|assertWarehouseWriteAccess|ADMIN|WAREHOUSE_MANAGER|VIEWER" backend/src/main/java/com/grain/platform/common backend/src/main/java/com/grain/platform/security`：passed

## Task Commits

1. **Task 1: 前端后台请求统一透传当前用户名** - `a51ba9e` (`feat`)
2. **Task 2: 后端建立轻量当前用户上下文与 403 异常语义** - `71b9aca` (`feat`)

## Notes

- 本 plan 未修改 `ROADMAP.md`、`REQUIREMENTS.md`、`STATE.md`，阶段级文档收口留到后续计划统一处理。
- 未发现偏离计划的额外修补项，Wave 2 可以直接复用 `AccessControlService` 开始前后端角色收口。
