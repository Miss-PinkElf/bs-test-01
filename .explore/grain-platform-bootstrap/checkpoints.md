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

## 2026-04-08-005
- 当前阶段：Realign / Truth-source rewrite
- 本轮完成内容：
  - 重新阅读老师新增需求，确认项目核心已从“多指标独立短期预测展示”转向“粮温滚动预测闭环”。
  - 新增滚动预测闭环版 PRD、数据库设计版、字段与接口变更方案、滚动预测接口设计版和 mock 数据设计。
  - 同步更新主 PRD、数据库定稿、接口设计文档、`NEXT-SESSION-PROMPT.md` 与 `.explore/` 当前状态。
- 本轮决策与原因：
  - 预测主线正式转为“8 个月训练参考 + 2 个月按天预测 + 新真实数据回填验证 + 修正续预测”。
  - 温度采用专门的粮温数据结构，湿度与二氧化碳保留普通单值结构，降低改造耦合。
- 本轮沉淀经验：
  - 当老师口径明显改变时，应先更新真相源与恢复提示，而不是沿旧路线继续写代码，否则后续会出现大面积返工。
- 待解决问题：
  - `schema.sql`、后端接口与前端预测页尚未正式按新模型改造。
  - 需要判断是增量重构还是半重写。
- 下一步：
  - 先输出实施计划，再改 `schema.sql`、接口和页面。
  - 生成新的 resume-ready handoff，明确旧 handoff 已被 supersede。
- 可以从活跃上下文移除的内容：
  - 关于“要不要纯按月预测”的多轮讨论过程。

## 2026-04-08-006
- 当前阶段：Resume-ready after schema refactor
- 本轮完成内容：
  - 新增 `docs/superpowers/plans/2026-04-08-rolling-forecast-schema-refactor.md`。
  - 重写 `backend/src/main/resources/db/schema.sql` 到粮温滚动预测闭环模型。
  - 在 `schema.sql` 中加入粮温测点、原始记录、汇总分析、滚动预测任务、修正预测结果和高温预警 mock 数据。
  - 新增最新 resume-ready handoff，并同步 `state.md`、`handoffs/index.md`、`NEXT-SESSION-PROMPT.md`。
- 本轮决策与原因：
  - 先把数据库模型和故事型 mock 数据写实，再进入 Java 层和前端的半重写，可以显著降低后续接口返工。
  - 当前不继续做大屏或旧预测页补丁，因为这些都建立在已过时的数据模型之上。
- 本轮沉淀经验：
  - 当数据库是老师强调的核心时，mock 数据不能只追求数量，必须能完整讲出“首次预测 -> 真实值回填 -> 修正预测 -> 高温预警”的故事。
- 待解决问题：
  - `mysql -uroot -p123456` 返回 `Access denied for user 'root'@'localhost'`，新 schema 尚未完成真实导库验证。
  - 后端 Java 层和前端页面仍停留在旧模型。
- 下一步：
  - 先确认可用 MySQL 凭据并实际导库。
  - 再改后端实体、Mapper、Service、Controller。
  - 最后改前端预测页、导入页和 API 适配层。
- 可以从活跃上下文移除的内容：
  - schema 字段命名微调时的中间草稿。

## 2026-04-08-007
- 当前阶段：Realign / Align（主线二次收口）
- 本轮完成内容：
  - 基于用户新增口径重新对齐主线，确认本期从“滚动预测闭环强绑定修正链”收口为“数据库优先 MVP”。
  - 确认本期不拆独立归档表，归档沿用 `prediction_task + prediction_result`。
  - 确认湿度和二氧化碳保留简单单值辅线，并支持文件导入与手工录入。
  - 确认预测页交互改为可选预测天数，图表重点展示“实际值 + 预测值”双线。
  - 更新 `.explore/grain-platform-bootstrap/` 的 workflow、state、decision-log 与 handoff 索引。
- 本轮决策与原因：
  - 修正链功能降级为扩展能力预留（字段保留，入口不做），以避免本期范围超载并突出数据库价值。
  - 不新增独立归档表，减少重复建模和数据一致性风险。
- 本轮沉淀经验：
  - 当主线连续变更时，应先统一更新 mission 状态、决策与恢复入口，再继续开发；否则会重复返工。
- 待解决问题：
  - MySQL 凭据仍未确认，`schema.sql` 尚未真实导库验证。
  - 后端与前端实现仍停留在旧预测口径。
- 下一步：
  - 先完成导库验证。
  - 再按“本期不做修正链入口”的标准改后端与前端。
  - 回写并收口真相源文档，避免后续会话冲突。
- 可以从活跃上下文移除的内容：
  - 关于“修正链是否本期必做”的多轮讨论过程。

## 2026-04-08-008
- 当前阶段：Checkpoint / Handoff（暂停前收尾）
- 本轮完成内容：
  - 复核当前已完成的文档更新，确认 `.explore`、最新 handoff 与根目录恢复提示已落盘。
  - 使用 `context-budget-explore` 内置 `session-handoff` 准备新的暂停恢复入口。
  - 确认当前最重要的未完成事项仍是 MySQL 凭据确认与 schema 真实导入验证。
- 本轮决策与原因：
  - 单独新增一份暂停 handoff，而不是只依赖上一份对齐 handoff，方便用户休息后直接恢复。
- 本轮沉淀经验：
  - 在需求连续变化的阶段，休息前应额外生成一份“pause-ready” handoff，避免恢复时混淆“设计对齐”和“实施起点”。
- 待解决问题：
  - 最新 handoff、state 与 `NEXT-SESSION-PROMPT.md` 仍需保持同一编号。
  - 旧真相源文档中的修正链表述尚未完全收口。
- 下一步：
  - 新增 `013` handoff 并更新索引。
  - 更新 `NEXT-SESSION-PROMPT.md` 指向最新 handoff。
  - 提交本轮文档变更。
- 可以从活跃上下文移除的内容：
  - 本轮对已更新文件的重复核对输出。
