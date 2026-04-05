# Tasks

## 实现任务

- [ ] 任务 1：建立 mission 记录与 spec 文档
  - 验收：`.explore/grain-platform-bootstrap/` 下存在 workflow、state、proposal、design、tasks 等文件。

- [ ] 任务 2：创建后端 Spring Boot 骨架
  - 验收：存在 `backend/pom.xml`、主启动类、基础 controller、service、配置文件和 SQL 脚本。

- [ ] 任务 3：创建前端 Vue 骨架
  - 验收：存在 `frontend/package.json`、`src/` 基础页面、路由和 API 封装。

- [ ] 任务 4：补充数据库、接口、页面和角色说明
  - 验收：`README.md` 或文档中能直接查看系统结构、模块、表设计、接口清单和角色建议。

- [ ] 任务 5：创建一键脚本
  - 验收：存在 PowerShell 脚本，至少支持环境检查与启动说明；依赖齐全时可启动前后端。

## 审查与验证任务

- [ ] 对照 proposal 与 design 做实现审查
- [ ] 运行最小验证命令并记录证据

## 备注

- 当前用户已明确要求继续推进，因此在写出 artifact 后直接进入骨架初始化。
- 若后续用户希望加真实权限认证、Excel 导入或真实数据库 CRUD，可在此 spec 基础上继续拆任务。
