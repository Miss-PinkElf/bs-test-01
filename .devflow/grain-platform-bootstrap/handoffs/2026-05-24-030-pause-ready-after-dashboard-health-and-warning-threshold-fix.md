# Handoff

## 基础信息

- 创建时间：2026-05-24
- mission：grain-platform-bootstrap
- 当前阶段：Pause-ready after 首页健康度历史温度口径与粮温预警阈值修复
- handoff 编号：030
- 是否 superseded：否

## 当前目标

- 收口首页与环境数据页的粮温展示口径：
  - 首页「仓库运行健康度」中的「均温 / 峰值」改为基于全历史粮温汇总的历史均温与历史最高温。
  - 粮温真实预警统一为 `>= 28°C` 为 `WARNING`、`25°C ~ 28°C` 为 `ATTENTION`、`< 25°C` 为 `NORMAL`。
  - 读取层不再盲信旧库中的 `warning_level` / `warning_flag`，避免历史脏数据继续展示错误预警。

## 当前进度

- 已完成本轮最小范围实现、前后端静态验证、devflow 记录更新与恢复提示更新。
- 当前真相源继续为 `.devflow/grain-platform-bootstrap/`。
- `.explore/grain-platform-bootstrap/` 仍仅作为历史快照，不作为当前真相源。

## 本轮完成内容

- [x] 新增轻量计划：`.devflow/grain-platform-bootstrap/plans/2026-05-24-dashboard-health-and-warning-threshold-fix.md`
- [x] 修改 `backend/src/main/resources/mapper/DashboardMapper.xml`
  - 健康度接口字段改为 `historyAvgTemp` / `historyMaxTemp`
  - 首页健康度不再使用“最新真实均温 + 最新预测峰值”混合口径
  - 首页真实预警、最新粮温汇总、健康度分页读取层统一按 `max_temp` 重算真实预警等级
- [x] 修改 `backend/src/main/resources/mapper/GrainTempSummaryMapper.xml`
  - 粮温汇总列表分页按 `max_temp` 计算有效 `warningLevel` / `warningFlag` / `warningMessage`
  - 过滤条件与关键词检索同步改为基于有效预警口径
- [x] 修改 `backend/src/main/resources/db/schema.sql`
  - 移除 6 号仓 `23.20` 特殊低阈值预警
  - 演示库粮温汇总统一为 `>= 28` / `>= 25` / `< 25` 三段规则
- [x] 修改 `backend/src/main/java/com/grain/platform/service/GrainTempService.java`
  - 手动新增 / 编辑 / 删除联动重算时，温度关注阈值改为 `25°C`
- [x] 修改 `backend/src/main/java/com/grain/platform/service/GrainTempImportService.java`
  - 固定模板导入后的汇总预警规则改为 `25°C` 关注下限
- [x] 修改 `backend/src/main/java/com/grain/platform/service/PredictionService.java`
  - 温度预测风险等级和单点预警判定改为 `25°C` 关注下限
- [x] 修改前端：
  - `frontend/src/api/grain.js`
  - `frontend/src/views/DashboardView.vue`
  - `frontend/src/views/BigScreenView.vue`
  - 首页与大屏改读历史温度字段；大屏兜底文案从“预测峰值”收口为“峰值”
- [x] 更新 devflow 记录：
  - `.devflow/grain-platform-bootstrap/bug-log.md`
  - `.devflow/grain-platform-bootstrap/checkpoints.md`
  - `.devflow/grain-platform-bootstrap/state.md`
  - `.devflow/grain-platform-bootstrap/handoffs/index.md`
  - `NEXT-SESSION-PROMPT-DEVFLOW.md`

## 验证结果

- `backend/`：`mvn -q -DskipTests compile` 通过
- `frontend/`：`npm run build` 通过

## 关键决策与原因

| 决策 | 备选方案 | 原因 |
| --- | --- | --- |
| 读取层按 `max_temp` 动态重算真实预警 | 只改 `schema.sql`，等用户重置库后自然生效 | 用户当前库里可能已有旧脏标记，只改种子数据无法立刻修复页面 |
| 健康度改为 `historyAvgTemp / historyMaxTemp` | 沿用旧字段名继续塞新含义 | 旧字段名 `latestForecastValue` 容易继续制造误解 |
| 温度关注阈值统一为 `25°C` | 继续使用 `28 * 0.9 = 25.2°C` | 用户已明确 `25°C ~ 28°C` 为关注区间，需要和真实展示口径保持一致 |
| 本轮不新增后端单元测试 | 追加 Dashboard / GrainTempSummary SQL 级测试 | 当前先完成最小静态闭环；后续若继续演进首页聚合，再补针对性测试更划算 |

## 关键文件 / 产物

| 文件 | 作用 | 相关性 |
| --- | --- | --- |
| `backend/src/main/resources/mapper/DashboardMapper.xml` | 首页健康度、真实预警、最新粮温汇总读取口径修复 | 本轮核心后端改动 |
| `backend/src/main/resources/mapper/GrainTempSummaryMapper.xml` | 环境数据页粮温汇总真实预警读取修复 | 本轮核心后端改动 |
| `backend/src/main/resources/db/schema.sql` | 演示库种子预警规则统一 | 重置库后的长期一致性 |
| `backend/src/main/java/com/grain/platform/service/GrainTempService.java` | 手动维护链路真实预警规则统一 | 运行态写入链路 |
| `backend/src/main/java/com/grain/platform/service/GrainTempImportService.java` | 导入链路真实预警规则统一 | 运行态写入链路 |
| `backend/src/main/java/com/grain/platform/service/PredictionService.java` | 温度预测风险等级规则统一 | 预测展示链路 |
| `frontend/src/views/DashboardView.vue` | 首页健康度展示修复 | 前端验证入口 |
| `frontend/src/views/BigScreenView.vue` | 大屏温度峰值文案与兜底展示修复 | 前端一致性 |
| `.devflow/grain-platform-bootstrap/plans/2026-05-24-dashboard-health-and-warning-threshold-fix.md` | 本轮轻量计划与验证结果 | 恢复与追溯入口 |

## 风险 / 阻塞项 / 开放问题

- [ ] 本轮只完成静态验证，尚未在重启后的 `8081` 后端上做页面人工复测。
- [ ] 当前用户本地如果仍跑着旧的 `8081` 后端进程，页面不会立即体现这次修复，需要重启后端。
- [ ] `schema.sql` 中 demo 预测任务的 `prediction_result` 仍是 `step 1 / 5 / 10`，且 `result_time` 仍是 `00:00:00`；若重置演示库但不重新预测，预测页仍会有旧口径视觉残留。
- [ ] 预测页人工复测尚未完成：需要确认预测开始时间、历史任务独立保存、同日真实/预测对齐三项页面效果。
- [ ] 系统管理员角色页面人工复测尚未做。
- [ ] 普通环境模板页面完整链路实测尚未做。
- [ ] 粮温多 Excel 并发导入 deadlock 第二轮止血代码仍待真实并发场景最终确认。
- [ ] 验收脚本尚未补 `keyword` 分页、`pointNo` / `tempMin` / `tempMax` / `filter-options` 断言。

## 立即下一步

1. 若继续验证本轮首页 / 环境数据修复：
   - 重启本地 `8081` 后端
   - 打开 `/environment`
   - 检查最高温 `23.x°C` 的粮温汇总是否为 `NORMAL`
   - 检查最高温 `25°C ~ 28°C` 的记录是否为 `ATTENTION`
   - 检查最高温 `>= 28°C` 的记录是否为 `WARNING`
   - 打开 `/dashboard`
   - 检查「仓库运行健康度」中的「均温 / 峰值」是否不再等同于最新粮温汇总
2. 若继续验证预测页修复：
   - 重启本地 `8081` 后端
   - 进入预测页做多次预测
   - 确认历史任务独立累积保存
   - 确认同一天真实值和预测值不再因 `00:00:00` / `08:40:00` 分裂
3. 若继续处理演示库旧预测口径：
   - 单独讨论是否将 `schema.sql` 里的 demo `prediction_result` 改为每天一条，并统一到 `08:40:00`

## 恢复指引

1. 先读取 `.devflow/grain-platform-bootstrap/handoffs/index.md`
2. 再读取本 handoff：`.devflow/grain-platform-bootstrap/handoffs/2026-05-24-030-pause-ready-after-dashboard-health-and-warning-threshold-fix.md`
3. 然后读取 `.devflow/grain-platform-bootstrap/state.md`
4. 必要时读取：
   - `.devflow/grain-platform-bootstrap/plans/2026-05-24-dashboard-health-and-warning-threshold-fix.md`
   - `.devflow/grain-platform-bootstrap/plans/2026-05-23-prediction-history-independent-tasks.md`
   - `.devflow/grain-platform-bootstrap/plans/2026-05-21-prediction-start-time-and-latest-cover.md`
   - `.devflow/grain-platform-bootstrap/bug-log.md`
   - `.devflow/grain-platform-bootstrap/checkpoints.md`
   - `NEXT-SESSION-PROMPT-DEVFLOW.md`
5. 从“立即下一步”的第 1 条开始继续

## 可从活跃上下文移除的内容

- 本轮逐段替换 `DashboardMapper.xml` 中重复真实预警查询的中间 diff 核对过程
- 本轮关于 `/data` 与 `/environment` 路由别名差异的即时澄清过程
