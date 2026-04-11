# 任务工作流

## 任务目标
- 基于 `zzz-docs/任务书.md` 与 `zzz-docs/开题报告.md`，推进一个数据库优先的粮仓环境数据预测管理平台。
- 以“数据保存、管理、归档”作为主目标，完成粮温导入、汇总分析、按天预测和高温预警的可演示主链路。
- 在现有项目基础上完成必要的半重写，不再沿旧多指标短期预测或修正链强绑定路线推进。

## 范围边界
- 范围内：
  - 后端 Spring Boot + MyBatis
  - 前端 Vue + ECharts
  - 数据库重构
  - 粮温 XLS 导入
  - 粮温原始测点与汇总分析
  - 滚动预测任务（前端可选预测天数）
  - 预测结果双线展示（实际值 / 预测值）
  - 高温预警
  - 湿度、二氧化碳简单单值数据（文件导入 + 手工录入）
  - 预测结果归档（`prediction_task + prediction_result` 两层表）
  - 启动脚本与恢复文档
- 范围外：
  - 真实物联网设备接入
  - 机器人、数字孪生、区块链实现
  - 复杂机器学习训练流程
  - 生产级部署
  - 本期实施“预测修正再预测”业务入口（仅保留扩展字段）
  - 本期新增独立归档表

## 成功标准
- `.devflow/` 中存在完整 mission 记录、最新 handoff 和可恢复提示词。
- 仓库内真相源与“数据库优先 + 修正链降级为扩展”的新口径一致。
- `backend/src/main/resources/db/schema.sql` 与当前主线一致，并明确保留扩展字段。
- 后续开发可以直接从“导库验证 -> 后端改造 -> 前端改造（双线图）”继续。

## 阶段规划
1. Classify / 选择路径（当前已切到 devflow 主线）
2. Align（确认当前业务边界与下一轮目标）
3. Plan / Propose（维护计划索引、必要时补 proposal / design / tasks）
4. Apply（继续推进初始化策略、仓库 CRUD、用户 CRUD、验收脚本等实现）
5. Review / Verify（获取新的静态或运行态验证证据）
6. Checkpoint / Handoff（阶段切换、暂停、跨对话恢复时更新记录）
7. Resume / Continue（从最新 state、plan、handoff 恢复推进）

## 当前阶段
- Pause-ready after GSD + devflow + 论文文档同步

## 本轮补充进展
- 已完成数据库优先 MVP 第一轮落地：
  - 新 `schema.sql` 已真实导库验证通过
  - 后端预测主链已切到 `grain_temp_summary + prediction_task + prediction_result`
  - 新增粮温导入与汇总查询接口
  - 前端预测页已切到“可选预测天数 + 实际值/预测值双线图”
  - 前端数据页已区分“粮温主线 / 普通环境数据”
- 已完成构建与运行级验证：
  - 前端 `npm run build` 通过
  - 后端本地启动成功
  - 粮温汇总接口与预测接口烟雾测试通过
  - 预测接口已成功生成新归档任务
- 已补充 `.gitignore`，忽略项目内临时 Maven 仓库与本地 settings 文件。

- 已完成第二轮首页与固定模板升级：
  - 首页仪表盘已切到真实预警 + 预测预警口径
  - 固定粮温模板已升级为 `XLSX` 下载与矩阵解析
  - 已新增回归验证清单并定位运行态阻塞为本机 MySQL 认证失败
- 已补数据主线 CRUD 执行计划：
  - 执行参考计划：`.devflow/grain-platform-bootstrap/plans/2026-04-09-data-crud-and-temperature-recompute.md`
  - 本计划用于约束 `grain_temp_record` CRUD、`sensor_data` CRUD 与温度变更后的汇总/真实预警联动重算
- 已完成过程记录迁移：
  - `.explore/grain-platform-bootstrap/` 已完整复制到 `.devflow/grain-platform-bootstrap/`
  - 新增 `.devflow/grain-platform-bootstrap/plans/active-plan-links.md` 作为计划索引
  - 新增 `NEXT-SESSION-PROMPT.devflow.md` 作为新的续接提示词副本
  - 原 `.explore/` 工作区与原 `NEXT-SESSION-PROMPT.md` 保持不变
- 已完成 2026-04-11 文档同步收口：
  - 已按 `.planning` **Phase 1-9 全量** 回看本轮 GSD 增量，不再只按最新阶段零散补文档
  - 已对照 `backend/src/main/resources/db/schema.sql` 与各 Controller，更新论文主文档与接口/数据库文档
  - 已新增 devflow 轻量计划 `2026-04-11-gsd-devflow-prd-database-doc-sync.md`
## 退出条件
- 用户可直接查看 `.devflow/grain-platform-bootstrap/` 下的最新真相源、状态记录、计划索引和 handoff 恢复上下文。
- 用户下次可直接从 `NEXT-SESSION-PROMPT.devflow.md` 或最新 devflow handoff 恢复，而无需重复核对迁移细节。

