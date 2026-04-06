# Checkpoints

> [已归档] 2026-04-05 - Mission Init 与首轮 Align 已归档到 `checkpoints-archive.md`

## 2026-04-05-002
- 当前阶段：Apply / Verify
- 本轮完成内容：
  - 生成 `.explore/grain-platform-bootstrap/spec/` 下的 proposal、design、tasks。
  - 创建 `backend/` Spring Boot 骨架、基础控制器、服务和 SQL 脚本。
  - 创建 `frontend/` Vue 页面、路由、API 封装和样式。
  - 创建 `scripts/` 下的环境检查与一键启动脚本。
  - 补充 `README.md` 和 VS Code 扩展建议。
- 本轮决策与原因：
  - 后端先用演示级内存数据服务，降低当前无数据库联调门槛。
  - 一键脚本采用“检查 + 启动”模式，以适应当前 Maven 缺失环境。
- 本轮沉淀经验：
  - 对毕业设计骨架，先让页面和接口可对上，再逐步替换为真实持久化，推进更稳。
- 待解决问题：
  - 缺少 Maven 或 Maven Wrapper，后端无法在命令行直接一键运行。
- 下一步：
  - 补 Maven Wrapper，或在 IDEA 中运行后端。
  - 进入真实数据库 CRUD、权限控制与导入功能迭代。
- 可以从活跃上下文移除的内容：
  - 已生成文件的逐个创建过程。

## 2026-04-06-003
- 当前阶段：Apply
- 本轮完成内容：
  - 基于 `开发指导版 PRD` 与最新 handoff 重新恢复上下文。
  - 通过一次 mini align 确认预测归档采用“任务主表 + 结果明细表”。
  - 将 `backend/src/main/resources/db/schema.sql` 从初稿升级为数据库定稿。
  - 新增 `zzz-docs/设计文档/数据库设计定稿.md` 作为表说明与建模决策文档。
  - 同步更新 `README.md` 与 `.explore/grain-platform-bootstrap/spec/design.md`。
- 本轮决策与原因：
  - 环境数据继续按“单指标单行”存储，便于查询、图表与指标扩展。
  - 预测归档采用 `prediction_task + prediction_result`，避免单表难以表达一次预测任务和多条预测点结果的关系。
- 本轮沉淀经验：
  - 对业务流程类毕业设计，先定数据库主模型，再做接口设计，可以显著降低 DTO、Mapper 和页面结构返工。
- 待解决问题：
  - 后端接口清单、DTO/VO、Mapper 仍需按新表结构同步定稿。
  - `frontend/` 仍是早期 Vue 骨架，尚未切换到正式技术栈与页面结构。
- 下一步：
  - 输出后端接口清单、DTO/VO、Mapper 设计。
  - 检查 `frontend/` 现状并准备正式版 Vue 骨架改造入口。
- 可以从活跃上下文移除的内容：
  - 关于 `prediction_record` 与双表方案的比较过程。

## 2026-04-06-004
- 当前阶段：Apply
- 本轮完成内容：
  - 在 `zzz-docs/设计文档/` 下新增后端接口、DTO/VO、Mapper 设计文档。
  - 将 `frontend/` 升级到 `Vue Router + Pinia + Element Plus + Axios + ECharts` 正式技术路线。
  - 新增 `ConsoleLayout`、`auth store`、`UsersView`、`BigScreenView` 和正式版页面导航结构。
  - 保留对当前 demo 后端的兼容适配，并补齐登录、仪表盘、仓库、环境数据、预测页面入口。
  - 在 `frontend/` 执行 `npm install` 和 `npm run build`，构建通过。
- 本轮决策与原因：
  - 正式前端先升级技术路线和页面骨架，再回头替换真实后端持久层，能更快形成可答辩的完整前端外壳。
  - API 层增加字段归一化适配，避免后端尚未重构完成时前端被阻塞。
- 本轮沉淀经验：
  - 在毕业设计这类长任务中，把“正式技术栈升级”和“真实后端落库”拆成两步推进，比强行同轮一起做更稳。
- 待解决问题：
  - 后端仍依赖 `DemoDataService`，真实 MyBatis 持久层尚未接上。
  - 用户管理、角色、指标选项、预测历史等正式接口还没落真实实现。
- 下一步：
  - 优先替换登录、仓库、环境数据、预测归档四条主链路的后端真实持久层。
  - 再开展前后端联调和页面真实数据替换。
- 可以从活跃上下文移除的内容：
  - 前端依赖安装与初次构建时的过程性输出。
