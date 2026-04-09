# 用户管理 CRUD 设计

## 背景

- 当前后台管理已完成初始化策略收口与仓库 CRUD。
- `UserController` / `UserService` 当前只提供用户列表与角色选项。
- 数据库已具备 `sys_user + sys_user_role + sys_role` 多角色结构，适合直接补完整后台用户管理闭环。

## 本轮目标

在“数据库优先 MVP”主线下补齐用户管理完整 CRUD，覆盖：

- 用户新增
- 用户编辑
- 用户删除
- 密码重置
- 多角色维护
- 所属仓库维护

## 范围约束

- 本轮继续保持管理员后台定位，不扩展角色管理页面。
- 本轮不改密码存储机制，继续沿用当前演示环境的明文口径。
- 本轮不做逻辑删除；删除仍采用硬删 + 引用拦截提示。
- `UsersView.vue` 右侧角色说明区先保留当前展示，不顺带做展示统一。
- `/screen` 不纳入本轮。

## 接口设计

### 1. 查询用户列表

- `GET /api/users`
- 保持现有返回结构不变，前端列表继续复用。

### 2. 查询角色选项

- `GET /api/roles/options`
- 保持现有返回结构不变。

### 3. 新增用户

- `POST /api/users`
- 请求字段：
  - `username`
  - `password`
  - `displayName`
  - `phone`
  - `warehouseId`（可空）
  - `status`
  - `roleCodes`（数组）

### 4. 编辑用户

- `PUT /api/users/{id}`
- 请求字段：
  - `displayName`
  - `phone`
  - `warehouseId`（可空）
  - `status`
  - `roleCodes`（数组）
- 不在此接口中混入密码更新。

### 5. 重置密码

- `PUT /api/users/{id}/password`
- 请求字段：
  - `newPassword`

### 6. 删除用户

- `DELETE /api/users/{id}`

## 核心业务规则

### 角色维护

- 前端按 `roleCode` 多选提交。
- 后端先校验角色编码是否存在，再将 `sys_user_role` 采用“先删后插”方式重建。

### 所属仓库

- `warehouseId` 允许为空，表示平台级用户。
- 若传入非空仓库，则必须校验仓库存在。

### 删除保护

- 若用户已被以下业务数据引用，则删除时返回业务提示：
  - `sensor_data.created_by`
  - `grain_temp_record.created_by`
  - `prediction_task.requested_by`

### 管理员保护

- 至少保留一个 `ADMIN` 角色用户。
- 因此：
  - 最后一个管理员不能删除
  - 最后一个管理员不能被编辑成不含 `ADMIN`

## 后端实现策略

- 沿用当前项目的 `Controller + Service + Mapper.xml` 风格。
- 新增用户 DTO：
  - `UserCreateRequest`
  - `UserUpdateRequest`
  - `UserPasswordResetRequest`
- `UserMapper` 增加用户增删改查与引用检查 SQL。
- `RoleMapper` 增加按 `roleCode` 批量查询角色 ID 的能力。
- `UserService` 负责：
  - 参数校验
  - 仓库存在性校验
  - 角色存在性校验
  - 管理员保护规则
  - 关联关系落库

## 前端实现策略

- `frontend/src/api/grain.js` 新增：
  - `createUser`
  - `updateUser`
  - `resetUserPassword`
  - `deleteUser`
- `UsersView.vue` 新增：
  - “新增用户”按钮
  - 表格操作列（编辑 / 重置密码 / 删除）
  - 新增 / 编辑弹窗
  - 重置密码弹窗
- 表单字段：
  - 用户名
  - 密码（仅新增）
  - 姓名
  - 手机号
  - 所属仓库
  - 状态
  - 多角色

## 验证策略

### 静态验证

- `backend/`：`mvn -q -DskipTests compile`
- `frontend/`：`npm run build`

### 运行态 smoke

- create user
- update user
- reset password
- delete user
- 最终清理临时验证数据

## 预期产出

- 用户后台维护闭环完成
- 与仓库 CRUD 一样具备最小可演示能力
- 为后续 PowerShell 验收脚本补齐后台管理链路基础
