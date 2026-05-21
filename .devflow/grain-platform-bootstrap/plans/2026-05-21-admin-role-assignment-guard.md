# 系统管理员角色分配收口

## 背景

老师指出当前“系统管理员”账号可以在用户管理里继续创建新的系统管理员。若 A 创建 B，B 又具备同等管理权限，就可能删除 A，这不符合“系统管理员是总管理员”的演示口径。

## 问题现象

- 用户管理的新增用户弹窗中，角色下拉包含“管理员（ADMIN）”。
- 通过页面可以新增第二个管理员账号。
- 如果存在多个启用管理员，后端当前只保护“最后一个启用管理员”，因此后创建的管理员仍可能删除或降权原管理员。

## 问题原因

- 前端角色下拉直接使用后端返回的全部角色选项，没有区分“可分配角色”和“系统内置管理角色”。
- 后端创建 / 编辑用户时只校验角色编码是否存在，没有限制 `ADMIN` 是否可被新增分配。
- 删除用户时只做最后一个启用管理员保护，不能阻止已有第二管理员删除其他管理员。

## 方案

1. 前端用户弹窗中，新增 / 编辑普通用户时只展示 `WAREHOUSE_MANAGER` 和 `VIEWER`。
2. 编辑已有管理员时保留 `ADMIN` 选项但禁用，避免误删管理员角色。
3. 后端禁止创建用户时提交 `ADMIN`。
4. 后端禁止把非管理员编辑成 `ADMIN`，也禁止移除已有管理员的 `ADMIN` 角色。
5. 后端禁止删除带 `ADMIN` 角色的用户，避免历史误建管理员继续删除总管理员。

## 验证计划

- `backend/` 执行 `mvn -q test -Dtest=UserServiceTest`
- `backend/` 执行 `mvn -q -DskipTests compile`
- `frontend/` 执行 `npm run build`

## 执行结果

- 已修改 `frontend/src/views/UsersView.vue`：用户弹窗的可分配角色收口为 `WAREHOUSE_MANAGER` / `VIEWER`；已有管理员编辑时保留并禁用 `ADMIN`。
- 已修改 `backend/src/main/java/com/grain/platform/service/UserService.java`：禁止新增管理员、禁止提权为管理员、禁止移除已有管理员角色、禁止删除管理员账号。
- 已新增 `backend/src/test/java/com/grain/platform/service/UserServiceTest.java`：覆盖接口绕过风险。

## 验证结果

- `backend/` 执行 `mvn -q test -Dtest=UserServiceTest` 通过。
- `backend/` 执行 `mvn -q -DskipTests compile` 通过。
- `frontend/` 执行 `npm run build` 通过。
