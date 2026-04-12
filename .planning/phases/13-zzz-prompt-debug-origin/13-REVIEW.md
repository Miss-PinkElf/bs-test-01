---
status: clean
phase: 13-zzz-prompt-debug-origin
updated: 2026-04-12
scope:
  - frontend role/menu changes
  - backend authorization and warehouse-scope changes
---

# Phase 13 Advisory Review

## Result

未发现新的阻塞性代码审查问题。

## What Was Checked

- 前端角色模型是否由单一能力源驱动，并避免菜单/路由/登录跳转规则漂移。
- DataView / PredictionView 是否把 viewer 写入口隐藏、manager 仓库选择锁定到所属仓库。
- 后端控制器是否统一接入 `X-Demo-Username`、`requireCurrentUser`、`resolveWarehouseScope` 与 `assertWarehouseWriteAccess`。
- `createdBy / requestedBy` 是否已从 `1L` 切到真实当前用户。
- 仪表盘后台接口是否对 manager 收口为单仓视图，同时 `/screen` 继续保持公开入口。

## Residual Risk

- 当前阶段仍需要按 `13-HUMAN-UAT.md` 对 `admin / manager_a01 / viewer_demo` 进行人工验收，特别是默认落点、菜单可见性和真实越权 `403` 反馈。
