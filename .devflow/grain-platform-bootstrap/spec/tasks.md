# Tasks

## 实现任务

- [x] 任务 1：建立 mission 记录与 spec 文档
  - 验收：`.devflow/grain-platform-bootstrap/` 下存在 workflow、state、proposal、design、tasks 等文件。

- [x] 任务 2：创建后端 Spring Boot 骨架
  - 验收：存在 `backend/pom.xml`、主启动类、基础 controller、service、配置文件和 SQL 脚本。

- [x] 任务 3：创建前端 Vue 骨架
  - 验收：存在 `frontend/package.json`、`src/` 基础页面、路由和 API 封装。

- [x] 任务 4：补充数据库、接口、页面和角色说明
  - 验收：`README.md` 或文档中能直接查看系统结构、模块、表设计、接口清单和角色建议。

- [x] 任务 5：创建一键脚本
  - 验收：存在 PowerShell 脚本，至少支持环境检查与启动说明；依赖齐全时可启动前后端。

- [x] 任务 6：升级正式版 Vue 前端骨架
  - 验收：`frontend/` 切换到 `Vue 3 + Vue Router + Pinia + Element Plus + Axios + ECharts` 正式结构，并补齐登录、仪表盘、用户、仓库、环境数据、预测页面入口。

- [x] 任务 7：将核心后端接口替换为真实 MyBatis 持久层
  - 验收：登录、仓库、环境数据、预测主链路不再依赖 `DemoDataService`，而是按数据库定稿与接口设计接入真实表结构。

## 审查与验证任务

- [ ] 对照 proposal 与 design 做实现审查
- [x] 运行最小验证命令并记录证据

## 备注

- 当前用户已明确要求继续推进，因此在写出 artifact 后直接进入骨架初始化。
- 若后续用户希望加真实权限认证、Excel 导入或真实数据库 CRUD，可在此 spec 基础上继续拆任务。
