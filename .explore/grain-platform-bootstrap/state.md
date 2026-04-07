# 当前状态

## 当前阶段
- Resume-ready after truth-source rewrite and schema refactor

## 已确认的事实
- 用户要求使用 `context-budget-explore` 记录过程。
- 当前仓库已是 Git 仓库，当前分支为 `main`。
- 当前环境已确认可用：`java`、`node`、`npm`、`mvn`、`mysql`。
- 开题报告已转为 Markdown，核心课题是“基于 Spring Boot 的粮仓环境数据预测管理平台设计与实现”。
- 已完成 `frontend-next/` 高保真静态原型增强，但正式前端路线仍是 `frontend/`。
- 已完成数据库定稿、后端接口设计、正式 Vue 前端骨架、真实 MyBatis + MySQL 主链路替换、8081 联调口径收口、环境数据导入与启动脚本修复。
- 已完成新一轮需求对齐：老师最新口径不再以“24 小时短期预测”或“多指标独立预测展示”为主，而是以粮温数据为核心的滚动预测闭环。
- 已确认当前正式业务闭环为：固定模板 XLS 导入 -> 粮温原始测点入库 -> 系统生成汇总 -> 前 8 个月真实数据作为训练参考 -> 预测后 2 个月每天数据 -> 新真实数据回填验证 -> 修正后续预测 -> 高温预警。
- 已新增五份新真相源文档，并新增 `schema.sql` 改造实施计划。
- 已重写 `backend/src/main/resources/db/schema.sql`，新增粮温测点、原始记录、汇总分析、滚动预测任务版本链和预测结果误差/预警结构。
- 已在 `schema.sql` 中写入稳定仓、风险仓、修正仓三类 mock 数据。
- 使用 `mysql -uroot -p123456` 实际导入新 `schema.sql` 时返回 `Access denied for user 'root'@'localhost'`，因此数据库真实导入验证尚未完成。

## 工作假设
- 以毕业设计 MVP 为目标，先做可演示的软件平台，不接入真实硬件。
- 预测功能继续采用简单回归或移动平均，重点在数据库与闭环留痕，而不是算法复杂度。
- 项目结构采用前后端分离：`backend/` + `frontend/`。
- 启动与联调默认使用 `8081`，避免与本地旧进程占用的 `8080` 冲突。
- 后续更像是“以现有项目为基础的半重写”，而不是继续小步补丁式迭代。

## 待解决的问题
- 当前 MySQL 凭据与文档中的 `root/123456` 不一致，导致新 `schema.sql` 不能直接导入验证。
- Java 实体、Mapper、Service、Controller 仍围绕旧模型实现，尚未与新 `schema.sql` 对齐。
- 前端 `PredictionView.vue`、`DataView.vue`、`frontend/src/api/grain.js` 仍沿用旧预测口径。
- `workflow.md` 仍是早期骨架表述，后续可继续收口，但不影响恢复。

## 下一步
- 先确认可用的 MySQL 账号密码，实际执行新的 `schema.sql`。
- 再按新表结构改后端实体、Mapper、Service、Controller。
- 最后改前端预测页、导入页和 API 适配层。

## 最新 handoff
- `.explore/grain-platform-bootstrap/handoffs/2026-04-08-011-schema-refactor-resume-ready.md`

## 最小活跃上下文摘要
- 当前已经完成真相源重写和 `schema.sql` 重构，但代码仍停留在旧预测模型；下一次应先完成数据库实际导入验证，再进入后端和前端的半重写改造。
