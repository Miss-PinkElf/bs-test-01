# Init Strategy And Admin CRUD Sequencing

## 背景

- 当前 `backend/src/main/resources/application.yml` 仍配置 `spring.sql.init.mode=always`。
- 当前 `backend/src/main/resources/db/schema.sql` 不只是建表，还包含：
  - `DROP TABLE IF EXISTS`
  - 全量演示数据 `INSERT`
- 这意味着后端每次启动都会重建演示库并覆盖本地联调过程中新增的导入、CRUD 和 smoke 数据。

## 本轮结论

- 不建议继续把 `spring.sql.init.mode=always` 作为默认开发态。
- 当前更合理的方向是把“初始化建库/重置演示数据”和“日常联调启动”拆开。
- 在初始化策略未收口前，不适合优先补 PowerShell 验收脚本，因为脚本结果会被每次重启清空，难以复用。

## 推荐顺序

1. 先收口初始化策略
   - 目标：默认启动不再清库。
   - 推荐方向：把默认配置改为保留数据的模式，并提供显式的演示库重置入口。
   - 结果：后续 CRUD、导入、验收脚本和老师演示数据都能稳定留存。

2. 再补仓库管理 CRUD
   - 当前后端与前端只做到列表/新增。
   - 仓库是用户、数据、预测的上游基础实体，先补它最稳。

3. 然后补用户管理 CRUD
   - 当前只做到用户列表和角色选项。
   - 这一步依赖仓库选项与角色关联处理，复杂度高于仓库 CRUD。

4. 最后补 PowerShell 验收脚本
   - 等初始化策略和后台管理接口稳定后再固化脚本，返工最少。

## 现状证据

- 仓库管理现状：
  - 后端 `WarehouseController` 只有列表、选项、新增。
  - 前端 `WarehouseView.vue` 只有新增弹框和列表分页。
- 用户管理现状：
  - 后端 `UserController` 只有用户列表、角色选项。
  - 前端 `UsersView.vue` 仍明确写着“新增、编辑和状态切换留待下一轮”。

## 下一轮进入 Apply 的最小任务定义

1. 调整本地初始化策略，区分“默认启动”和“显式重置演示库”。
2. 为仓库管理补齐后端更新/删除接口与前端编辑/删除交互。
3. 用新的初始化策略重新做一次最小 smoke，确认数据不会因重启丢失。
