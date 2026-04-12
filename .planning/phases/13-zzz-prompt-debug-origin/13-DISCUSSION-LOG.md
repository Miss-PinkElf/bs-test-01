# Phase 13: 角色菜单与权限区分（前端菜单 + 后端权限） - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in `13-CONTEXT.md` — this log preserves the alternatives considered.

**Date:** 2026-04-12
**Phase:** 13-角色菜单与权限区分（前端菜单 + 后端权限）
**Areas discussed:** 角色范围、菜单映射、数据权限、越权处理

---

## 角色范围

| Option | Description | Selected |
|--------|-------------|----------|
| 继续只用现有 3 个角色 | 沿用 `ADMIN`、`WAREHOUSE_MANAGER`、`VIEWER`，不扩角色体系 | ✓ |
| 顺手新增细分角色 | 本阶段同步扩展更多角色与职责 | |
| 重新设计角色模型 | 把当前角色体系整体重做 | |

**User's choice:** 全部按推荐，继续只用现有 3 个角色。
**Notes:** 用户接受“先让现有角色真正生效，不顺手扩角色体系”的建议。

---

## 菜单映射

| Option | Description | Selected |
|--------|-------------|----------|
| 管理员全量，仓库管理员与查看者仅业务页 | `ADMIN` 看全量菜单；`WAREHOUSE_MANAGER` / `VIEWER` 只看 `仪表盘`、`环境数据`、`温度预测` | ✓ |
| 仓库管理员也保留仓库管理 | 允许仓库管理员继续进入仓库管理页 | |
| 查看者仅保留仪表盘 | 进一步缩减查看者菜单，只留汇总页 | |

**User's choice:** 全部按推荐。
**Notes:** 推荐默认落点同步被接受：`ADMIN -> /dashboard`，`WAREHOUSE_MANAGER -> /environment`，`VIEWER -> /dashboard`。

---

## 数据权限

| Option | Description | Selected |
|--------|-------------|----------|
| 管理员全局可管，仓库管理员单仓可管，查看者全局只读 | 最符合当前 `role + warehouseId` 结构，落地成本最低 | ✓ |
| 仓库管理员全局可看仅本仓可改 | 页面上还能看到其它仓库数据，但不能操作 | |
| 查看者单仓只读 | 把查看者也绑定到单个仓库 | |

**User's choice:** 全部按推荐。
**Notes:** 用户接受“查看者作为演示账号全局只读”的建议；仓库管理员按所属仓库限制查看和操作范围。

---

## 越权处理

| Option | Description | Selected |
|--------|-------------|----------|
| 前端隐藏 + 路由拦截跳转 + 后端 403 | 最小可落地方案，不引入复杂权限页 | ✓ |
| 前端隐藏 + 独立 403 页面 + 后端 403 | 体验更完整，但本阶段实现量更大 | |
| 只做前端隐藏 | 成本最低，但无法真正防越权 | |

**User's choice:** 全部按推荐。
**Notes:** 用户接受“本阶段不单独做复杂 403 页面，前端跳转提示即可”的建议。

---

## the agent's Discretion

- 菜单权限配置落在路由 `meta` 还是单独常量表，由后续 planning 决定。
- 页面内修改型操作使用“直接隐藏”还是“禁用并提示”，由 planner 结合现有页面结构决定。
- 后端权限收口具体落在拦截器、控制器辅助方法或 service 层公共守卫，由 planner 决定。

## Deferred Ideas

- 新增更细角色或按钮级权限
- 引入 Spring Security
- 新增独立 403 页面
- 把 `/screen` 纳入统一权限体系
