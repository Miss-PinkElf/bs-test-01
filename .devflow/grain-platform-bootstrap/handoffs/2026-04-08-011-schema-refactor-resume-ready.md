# Handoff

## 基础信息

- 创建时间：2026-04-08
- mission：grain-platform-bootstrap
- 当前阶段：Resume-ready after truth-source rewrite and schema refactor
- handoff 编号：011
- 是否 superseded：否

## 当前目标

- 保存“新真相源已建立 + schema.sql 已切到滚动预测闭环模型 + mock 数据已设计并写入”的阶段性成果。
- 让下一次会话可以直接从“验证数据库导入 -> 改后端实体/Mapper/Service -> 改前端预测页与导入页”继续，而不必再重新梳理需求。

## 当前进度

- 已完成与用户的需求对齐，确认项目主线正式切换为“粮温滚动预测闭环”。
- 已完成新版 PRD、数据库设计、字段与接口变更方案、滚动预测接口设计和 mock 数据设计文档。
- 已将 `backend/src/main/resources/db/schema.sql` 从旧的“多指标短期预测”结构重写为：
  - `grain_temp_point`
  - `grain_temp_record`
  - `grain_temp_summary`
  - 升级后的 `prediction_task`
  - 升级后的 `prediction_result`
- 已将稳定仓、升温风险仓、修正明显仓三类场景的 mock 数据写入 `schema.sql`。

## 本轮完成内容

- [x] 新增 `docs/superpowers/plans/2026-04-08-rolling-forecast-schema-refactor.md`
- [x] 重写 `backend/src/main/resources/db/schema.sql`
- [x] 在 `schema.sql` 中加入粮温测点、原始记录、汇总分析、滚动预测任务、修正预测结果和高温预警样例数据
- [x] 继续完善 `.devflow/grain-platform-bootstrap/state.md`
- [x] 更新 `NEXT-SESSION-PROMPT.md` 为新路线恢复入口

## 关键决策与原因

| 决策 | 备选方案 | 原因 |
| --- | --- | --- |
| `schema.sql` 直接切到新模型 | 继续兼容旧短期预测表语义 | 旧表语义已经无法承接粮温测点、汇总分析、版本链修正和高温预警 |
| mock 数据按“稳定仓 / 风险仓 / 修正仓”三条故事线组织 | 只做零散演示数据 | 这样最适合答辩展示“首次预测、误差验证、修正预测、预警”完整闭环 |
| 先重写数据库，再动 Java 层 | 先改接口再改数据库 | 新路线的核心断点就在数据模型，数据库不先定，后端接口会反复返工 |

## 关键文件 / 产物

| 文件 | 作用 | 相关性 |
| --- | --- | --- |
| `backend/src/main/resources/db/schema.sql` | 新滚动预测闭环数据库结构与 mock 数据真相源 | 最高 |
| `zzz-docs/设计文档/粮仓环境数据预测管理平台-PRD-滚动预测闭环版.md` | 新业务主线 PRD | 最高 |
| `zzz-docs/设计文档/数据库设计-滚动预测与高温预警版.md` | 新数据库真相源 | 最高 |
| `zzz-docs/设计文档/滚动预测改造-字段与接口变更方案.md` | 旧模型到新模型的桥接方案 | 最高 |
| `zzz-docs/设计文档/后端接口与DTO-VO-Mapper设计-滚动预测版.md` | 新接口口径 | 高 |
| `zzz-docs/设计文档/mock数据设计-滚动预测闭环版.md` | 新 mock 数据组织方案 | 高 |
| `docs/superpowers/plans/2026-04-08-rolling-forecast-schema-refactor.md` | schema 实施计划 | 高 |
| `NEXT-SESSION-PROMPT.md` | 下次会话直接恢复提示 | 高 |

## 关键验证结果

- 文本级自查已完成：
  - 已确认 `grain_temp_point`、`grain_temp_record`、`grain_temp_summary` 存在
  - 已确认 `prediction_task.parent_task_id`、`forecast_start_time`、`forecast_end_time` 存在
  - 已确认 `prediction_result.phase_type`、`error_value`、`warning_level`、`is_corrected` 存在
- 数据库真实导入验证未完成：
  - 执行 `mysql -uroot -p123456 grain_env_predict --execute="SOURCE backend/src/main/resources/db/schema.sql; SHOW TABLES;"`
  - 返回：`Access denied for user 'root'@'localhost'`

## 风险 / 阻塞项 / 开放问题

- [ ] 当前 MySQL 凭据与文档中的 `root/123456` 不一致，导致 `schema.sql` 未实际导入验证
- [ ] Java 实体、Mapper、Service、Controller 仍是旧模型，和新版 `schema.sql` 尚未对齐
- [ ] 前端 `PredictionView.vue`、`DataView.vue`、`grain.js` 仍沿用旧预测口径
- [ ] `workflow.md` 仍是早期骨架表述，后续可再做一次收口，但不影响当前恢复

## 立即下一步

1. 先确认可用的 MySQL 账号密码，实际执行新的 `schema.sql`
2. 再基于新表结构重写后端实体与 Mapper：
   - `PredictionTask`
   - `PredictionResult`
   - 新增 `GrainTempPoint`
   - 新增 `GrainTempRecord`
   - 新增 `GrainTempSummary`
3. 改造后端服务与接口：
   - `SensorDataImportService`
   - `PredictionService`
   - 新增粮温导入与查询接口
4. 最后改前端：
   - `DataView.vue`
   - `PredictionView.vue`
   - `frontend/src/api/grain.js`

## 恢复指引

1. 先读取 `NEXT-SESSION-PROMPT.md`
2. 再读取 `.devflow/grain-platform-bootstrap/state.md`
3. 再读取本 handoff
4. 再读取以下五份真相源：
   - `zzz-docs/设计文档/粮仓环境数据预测管理平台-PRD-滚动预测闭环版.md`
   - `zzz-docs/设计文档/数据库设计-滚动预测与高温预警版.md`
   - `zzz-docs/设计文档/滚动预测改造-字段与接口变更方案.md`
   - `zzz-docs/设计文档/后端接口与DTO-VO-Mapper设计-滚动预测版.md`
   - `zzz-docs/设计文档/mock数据设计-滚动预测闭环版.md`
5. 从“立即下一步”的第 1 条继续

## 可从活跃上下文移除的内容

- 关于是否继续优先做大屏真实化的讨论
- 关于按月预测还是按天预测的来回讨论
- schema 字段命名微调时的中间草稿
