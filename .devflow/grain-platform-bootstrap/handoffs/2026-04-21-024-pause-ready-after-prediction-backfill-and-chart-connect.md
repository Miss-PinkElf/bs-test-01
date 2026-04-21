# Handoff

## 基础信息

- 创建时间：2026-04-21
- mission：grain-platform-bootstrap
- 当前阶段：Pause-ready after 全仓演示数据补齐 + 历史预测任务真实值回填 + 预测图断线收口
- handoff 编号：024
- 是否 superseded：否

## 当前目标

- 保持“数据库优先 MVP”演示可用，并收口最近一轮与预测页相关的数据库口径、历史任务详情口径和双线图显示体验，方便下轮继续联调或整理答辩材料。

## 当前进度

- 已完成：**演示库 SQL 收口**
  - `backend/src/main/resources/db/schema.sql` 已改为“先删库再建库”，可直接作为重置数据库真相源。
  - 6 个仓库均已具备完整的温度、湿度、二氧化碳历史数据，统一截止到 `2026-04-25`。
  - 3 号仓保留 `MAINTENANCE` 状态，但仍保留完整历史数据与预测归档。
  - 每个仓库都已有至少 1 条温度预测任务。
- 已完成：**历史预测任务真实值回填**
  - `PredictionService` 已将“训练样本”与“任务详情展示样本”拆开。
  - 查看历史预测任务时，不再只显示执行当时的训练快照，而是会把预测区间内后来录入的真实值也回填到 `resultList` 中做对照。
  - 已在临时新后端进程（`18082`）上验证：一号仓旧任务详情能看到 `2026-04-27 08:40:00` 的真实值回填。
- 已完成：**预测图断线收口**
  - `frontend/src/views/PredictionView.vue` 的“实际值”“预测值”双线均已开启 `connectNulls: true`。
  - 中间某天没有值时，折线会跨空值继续连线，不再视觉断开。
- 已完成：**静态验证**
  - `backend/`：`mvn -q -DskipTests compile`
  - `frontend/`：`npm run build`

## 本轮完成内容

- [x] 把 `schema.sql` 收口为“重置数据库真相源”
- [x] 把 6 仓演示数据补齐并收口到 `2026-04-25`
- [x] 为历史预测任务补“预测区间内真实值回填”逻辑
- [x] 收口预测图断线显示
- [x] 更新 `devflow` 计划、决策、状态、checkpoint

## 关键决策与原因

| 决策 | 原因 |
| --- | --- |
| `schema.sql` 开头改为 `DROP DATABASE IF EXISTS + CREATE DATABASE` | 当前它本来就是重置数据库入口，整库重建比逐表删除更直观，也更符合用户预期 |
| 历史预测任务详情改为“回填后续真实值” | 用户明确指出旧任务在录入新真实值后看不到对照，当前仅回放训练快照不符合业务直觉和答辩讲解方式 |
| 双线图启用 `connectNulls` | 回填真实值与稀疏预测点混合后，中间会存在空值；若不跨空值连线，视觉上会误以为数据断掉 |

## 关键文件

| 文件 | 作用 |
| --- | --- |
| `backend/src/main/resources/db/schema.sql` | 演示库与重置数据库真相源 |
| `backend/src/main/java/com/grain/platform/service/PredictionService.java` | 历史预测任务详情的真实值回填与时间轴合并 |
| `frontend/src/views/PredictionView.vue` | 预测页图表说明文案与 `connectNulls` |
| `.devflow/grain-platform-bootstrap/plans/2026-04-21-full-demo-data-through-0425-for-all-warehouses.md` | 本轮 SQL 口径计划 |
| `.devflow/grain-platform-bootstrap/plans/2026-04-21-prediction-task-actual-backfill-compare.md` | 本轮预测页口径计划 |
| `NEXT-SESSION-PROMPT-DEVFLOW.md` | 下轮可直接复制的恢复提示词 |

## 风险 / 阻塞 / 开放问题

- [ ] **本地 `8081` 旧进程**：如果本地页面仍看不到“预测区间内真实值回填”或折线连线效果，优先确认当前是否还跑着旧的 `8081` 后端进程；本轮运行态验证是在临时 `18082` 新进程上完成的。
- [ ] **demo 预测点时间仍不统一**：当前 demo 任务的 `prediction_result.result_time` 仍是 `00:00:00`，而真实粮温汇总通常是 `08:40:00`，同一天会出现两种时刻，视觉上仍略割裂。
- [ ] **demo 预测点仍偏稀疏**：`schema.sql` 中的 demo 任务结果目前仍是 `step 1 / 5 / 10` 这类稀疏点，不是按 `forecastDays` 每天一条；这已经讨论过，但本轮尚未继续改。
- [ ] **粮温导入 deadlock**：并发导入的第二轮止血代码已完成，但真实“同仓库多 Excel 并发导入”场景仍待最终复测。
- [ ] **验收脚本可选增强**：`run-acceptance-smoke.ps1` 还没专门断言 `keyword` 分页、`pointNo` / `tempMin` / `tempMax` / `filter-options`。
- [ ] **文档可选增强**：若答辩前继续收口，可考虑在论文/设计文档里明确“当前演示历史数据统一截止到 `2026-04-25`”。

## 立即下一步

1. 若要立即在本地页面看到本轮预测页改动，先重启本地 `8081` 后端，再刷新前端页面。
2. 若继续收口预测页，优先处理：
   - demo 任务未来预测点按天补齐
   - demo 预测点时刻与真实粮温统一到 `08:40:00`
3. 若继续联调数据导入，优先复测“同仓库多 Excel 并发导入” deadlock 是否彻底收口。
4. 若转去答辩材料，可从最新 `state.md` + 本 handoff + 相关计划中提炼“数据库口径 / 预测页口径 / 页面截图说明”。

## 恢复指引

1. 根目录 `NEXT-SESSION-PROMPT-DEVFLOW.md`
2. `.devflow/grain-platform-bootstrap/state.md`
3. 本文件：`handoffs/2026-04-21-024-pause-ready-after-prediction-backfill-and-chart-connect.md`
4. 按需：
   - `.devflow/grain-platform-bootstrap/plans/2026-04-21-full-demo-data-through-0425-for-all-warehouses.md`
   - `.devflow/grain-platform-bootstrap/plans/2026-04-21-prediction-task-actual-backfill-compare.md`
   - `.devflow/grain-platform-bootstrap/checkpoints.md`
