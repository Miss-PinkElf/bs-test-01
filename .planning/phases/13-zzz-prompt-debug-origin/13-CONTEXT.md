# Phase 13: 角色菜单与权限区分（前端菜单 + 后端权限） - Context

**Gathered:** 2026-04-12
**Status:** Ready for planning

<domain>
## Phase Boundary

本阶段聚焦当前正式 Vue 管理端与 Spring Boot 后端的角色区分收口：让不同角色登录后看到不同菜单、进入不同默认页面，并对后台页面操作与后端接口做一致的权限限制。

范围包括：

1. 登录后前端菜单可见性
2. 路由直接访问时的角色校验与跳转
3. 页面内新增 / 编辑 / 删除 / 导入 / 执行预测等操作的角色限制
4. 后端接口的基础角色门禁与仓库范围校验

本阶段不扩展为新的角色体系设计，不新增权限管理后台，也不把公开展示大屏 `/screen` 改成登录后专用页面。

</domain>

<decisions>
## Implementation Decisions

### 角色范围

- **D-01:** 本阶段只使用当前数据库里已经存在的 3 个角色：`ADMIN`、`WAREHOUSE_MANAGER`、`VIEWER`，不新增角色，不拆更细权限组。
- **D-02:** 角色职责收口如下：
  - `ADMIN`：平台管理员，拥有全局查看与全局操作权限。
  - `WAREHOUSE_MANAGER`：仓库管理员，围绕所属仓库开展查看与操作。
  - `VIEWER`：查看者，只读查看，不进行任何新增、编辑、删除、导入或预测执行。

### 菜单与默认落点

- **D-03:** 前端菜单按角色过滤，不再对所有登录用户展示同一套后台菜单。
- **D-04:** `ADMIN` 可见菜单为：`仪表盘`、`用户管理`、`仓库管理`、`环境数据`、`温度预测`。
- **D-05:** `WAREHOUSE_MANAGER` 可见菜单为：`仪表盘`、`环境数据`、`温度预测`。
- **D-06:** `VIEWER` 可见菜单为：`仪表盘`、`环境数据`、`温度预测`。
- **D-07:** 登录后默认落点按角色区分：
  - `ADMIN` → `/dashboard`
  - `WAREHOUSE_MANAGER` → `/environment`
  - `VIEWER` → `/dashboard`

### 前端访问控制

- **D-08:** 前端除了隐藏无权限菜单外，还必须在路由守卫中拦截“手动输入 URL 直达无权限页面”的情况，不能只做视觉隐藏。
- **D-09:** 当前用户访问无权限后台路由时，前端统一跳到“该角色第一个有权限的页面”，并给出明确提示；本阶段不单独新增复杂 `403` 页面。
- **D-10:** `/screen` 继续保持当前公开展示路由属性，不纳入本阶段的后台角色菜单控制。

### 数据与操作权限

- **D-11:** `ADMIN` 拥有全局数据查看与全局操作权限。
- **D-12:** `WAREHOUSE_MANAGER` 只允许查看自己 `warehouseId` 对应仓库的数据，并且只允许操作自己所属仓库的数据。
- **D-13:** `VIEWER` 允许查看全局数据，但只能只读查看，不允许新增、编辑、删除、导入、重置或执行预测。
- **D-14:** 对 `VIEWER` 而言，`环境数据` 和 `温度预测` 页面继续保留为可访问页面，但页面内所有修改型入口都必须隐藏或禁用，保持“可看不可改”的答辩演示账号语义。

### 后端权限门禁

- **D-15:** 后端必须做真正的接口权限校验，不能只依赖前端隐藏菜单或按钮。
- **D-16:** 用户越权访问接口时，后端返回 `403`，与前端路由拦截形成双重防线。
- **D-17:** 仓库范围校验采用“角色 + `warehouseId`”的轻量方式收口，不在本阶段引入 Spring Security、RBAC 配置表、策略引擎或复杂注解体系。

### the agent's Discretion

- 前端菜单权限映射可以写在路由 `meta`、单独常量表，或 `ConsoleLayout` / `router` 共享配置中，只要最终能保持菜单、默认跳转和路由校验一致即可。
- `VIEWER` 页面内操作入口使用“直接隐藏”还是“禁用并附提示”，可由 planner 基于现有页面结构决定，但最终必须满足只读。
- 后端权限校验采用控制器辅助方法、拦截器或轻量公共权限工具均可，只要不引入超出本阶段边界的重型权限框架即可。

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### 原始诉求

- `zzz-prompt-debug/origin/区分角色（前端+后端）/prompt.md` — 用户原始需求：当前不同角色进入后台看到同一菜单，需要按角色区分菜单与权限

### 路线图与项目约束

- `.planning/ROADMAP.md` — Phase 13 条目、当前 milestone 位置与阶段目标
- `.planning/PROJECT.md` — 毕设 / 数据库优先 MVP 的范围约束，要求避免无关大重构
- `.planning/REQUIREMENTS.md` — 当前项目既有页面与能力边界，避免把权限收口扩成新产品能力
- `.planning/STATE.md` — 当前阶段状态与 roadmap evolution

### 当前前端实现

- `frontend/src/layout/ConsoleLayout.vue` — 当前后台菜单是固定全量 `navItems`，Phase 13 的直接改造入口
- `frontend/src/router/index.js` — 当前只做登录态校验，未做角色路由守卫
- `frontend/src/stores/auth.js` — 当前 session 中已有 `roleCodes` 与 `primaryRole`
- `frontend/src/utils/session.js` — 当前登录态本地持久化位置
- `frontend/src/api/http.js` — 前端统一请求入口，后续若接入角色头或 403 处理需参考这里
- `frontend/src/api/grain.js` — 登录响应与角色字段归一化入口
- `frontend/src/views/LoginView.vue` — 登录后跳转逻辑入口

### 当前后端实现

- `backend/src/main/java/com/grain/platform/controller/AuthController.java` — 登录与当前用户接口入口
- `backend/src/main/java/com/grain/platform/service/AuthService.java` — 登录成功后角色与仓库信息的事实源
- `backend/src/main/java/com/grain/platform/controller/UserController.java` — 当前典型后台管理接口之一，尚无角色门禁
- `backend/src/main/java/com/grain/platform/service/UserService.java` — 角色解析与用户角色维护逻辑
- `backend/src/main/java/com/grain/platform/dto/auth/LoginResponse.java` — 前端 session 当前可直接使用的角色字段
- `backend/src/main/java/com/grain/platform/dto/auth/CurrentUserResponse.java` — 当前用户接口返回结构
- `backend/src/main/java/com/grain/platform/mapper/RoleMapper.java` — 当前角色查询入口
- `backend/src/main/resources/mapper/RoleMapper.xml` — 角色查询 SQL 实现
- `backend/src/main/resources/db/schema.sql` — `sys_role`、`sys_user`、`sys_user_role`、`warehouse_id` 的数据库事实源

### 上游相关阶段

- `.planning/phases/09-mock/09-CONTEXT.md` — 管理端页面边界与已有服务端分页页面范围
- `.planning/phases/08-zzz-prompt-debug-origin/08-CONTEXT.md` — `/screen` 作为公开展示入口的当前边界

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets

- `frontend/src/stores/auth.js` 已把登录用户收口成统一 store，后续可直接在这里增加角色判断辅助函数，如“是否管理员”“是否仓库管理员”“是否只读”。
- `frontend/src/router/index.js` 已有统一 `beforeEach` 入口，适合在现有登录态守卫基础上补角色路由守卫与默认跳转。
- `frontend/src/layout/ConsoleLayout.vue` 现有菜单由单一 `navItems` 常量驱动，改造成“路由配置 + 角色过滤”成本可控。
- `backend/src/main/java/com/grain/platform/service/AuthService.java` 已经能查出 `roleCodes` 与 `warehouseId`，不需要新增登录链路即可支撑前后端权限判断。

### Established Patterns

- 前端当前采用 `Pinia + Vue Router` 管理登录态与页面跳转，适合继续走“session 内 roleCodes + router meta 权限”的轻量模式。
- 后端当前没有 Spring Security，也没有全局权限框架；接口层主要是 `Controller -> Service -> Mapper` 的轻量 Spring Boot 结构。
- 当前角色体系已经通过 `sys_role`、`sys_user_role` 与用户 `warehouse_id` 建模，因此“角色 + 所属仓库”的权限收口符合现有数据库事实。

### Integration Points

- `ConsoleLayout.vue` 需要把固定菜单改造成按当前角色动态过滤后的菜单。
- `router/index.js` 需要为路由补角色元信息、默认落点与越权跳转逻辑。
- `LoginView.vue` 登录成功后的默认跳转需要从统一 `/dashboard` 改为按角色分流。
- `http.js` 与后端接口层需要为 `403` 场景补统一处理。
- 后端各后台管理接口需要补一层统一角色判断与仓库范围校验，至少覆盖用户管理、仓库管理、环境数据、预测相关写接口与查询范围。

</code_context>

<specifics>
## Specific Ideas

- 用户明确要求：当前不同角色进入后台看到的是同一菜单，这次必须按角色区分菜单与权限。
- 用户确认采用推荐默认值：不新增角色，只使用 `ADMIN`、`WAREHOUSE_MANAGER`、`VIEWER` 三种现有角色。
- 用户确认采用推荐默认值：仓库管理员只管理所属仓库；查看者全局只读；管理员全局可管。
- 用户确认采用推荐默认值：前端越权访问跳到该角色首个可用页面并提示，后端接口越权返回 `403`，本阶段不单独做复杂 `403` 页面。

</specifics>

<deferred>
## Deferred Ideas

- 把权限从“写死在代码里”升级为可配置权限点、菜单权限表、按钮权限表
- 新增更细角色，如数据录入员、预测分析员、审计员等
- 引入 Spring Security 或更完整的认证鉴权体系
- 为越权访问新增独立 `403` 页面或更丰富的权限说明页
- 把公开大屏 `/screen` 也并入统一权限体系

</deferred>

---

*Phase: 13-zzz-prompt-debug-origin*
*Context gathered: 2026-04-12*
