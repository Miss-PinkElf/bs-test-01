```text
你现在在仓库根目录下的 `bs-test-01`。请继续毕业设计项目，并严格先走 devflow 恢复流程后再开始任何分析、验证或开发。

【协作硬约束（必读）】
- 实现类需求（多文件、前后端、新交互）默认须先输出：理解 + 方案 + 待你确认，禁止同一条回复里直接大范围改代码。
- 只有你明确说「直接做」「不用讨论」等豁免语时，才可跳过对齐。详见 `.cursor/rules/project-zh.mdc` §3。

【必须先读取】
1. `.codex/skills/devflow/SKILL.md`
2. `zzz-docs/任务书.md`
3. `zzz-docs/开题报告.md`
4. `.devflow/grain-platform-bootstrap/state.md`
5. `.devflow/grain-platform-bootstrap/handoffs/2026-04-21-024-pause-ready-after-prediction-backfill-and-chart-connect.md`
6. 按需读取：
   - `.devflow/grain-platform-bootstrap/handoffs/index.md`
   - `.devflow/grain-platform-bootstrap/plans/active-plan-links.md`
   - `.devflow/grain-platform-bootstrap/plans/2026-04-21-full-demo-data-through-0425-for-all-warehouses.md`
   - `.devflow/grain-platform-bootstrap/plans/2026-04-21-prediction-task-actual-backfill-compare.md`
   - `.devflow/grain-platform-bootstrap/checkpoints.md`
   - `zzz-docs/验证/数据库优先MVP-回归验证清单.md`

【当前主线与口径】
- 过程记录走 `devflow`，真相源在 `.devflow/grain-platform-bootstrap/`
- `.explore/grain-platform-bootstrap/` 仅历史快照
- 本期仍是「数据库优先 MVP」
- 不做「预测 -> 修正 -> 再预测」必做入口（字段可保留）
- 预测归档继续使用：`prediction_task + prediction_result`
- 正式前端：`frontend/`；`frontend-next/` 仅静态原型参考

【这轮已完成（恢复时不必重做）】
1. `backend/src/main/resources/db/schema.sql`
   - 已改成“先删库再建库”，可以直接作为重置数据库真相源
   - 6 个仓库都补齐了完整的温度、湿度、二氧化碳历史数据
   - 历史数据统一截止到 `2026-04-25`
   - 3 号仓保持 `MAINTENANCE`，但保留完整历史数据和预测归档
2. 历史预测任务详情
   - `backend/src/main/java/com/grain/platform/service/PredictionService.java`
   - 已支持“预测区间内后续真实值回填”
   - 旧任务不再只显示执行当时训练快照
   - 运行态已在临时 `18082` 新后端进程验证：一号仓旧任务能看到 `2026-04-27 08:40:00` 的真实值
3. 预测页折线图
   - `frontend/src/views/PredictionView.vue`
   - 已补页面说明文案
   - 已给“实际值”“预测值”两条线加 `connectNulls: true`
   - 中间空值不会再把折线视觉断开
4. 静态验证已通过
   - `backend/`：`mvn -q -DskipTests compile`
   - `frontend/`：`npm run build`

【本轮未完成 / 未讨论完 / 开放问题】
1. **本地 `8081` 后端可能还是旧进程**
   - 如果页面还看不到“预测区间内真实值回填”或折线连线效果，先重启本地 `8081` 后端再刷新前端
   - 本轮运行态验证是在临时 `18082` 新后端进程上做的
2. **demo 预测结果仍偏稀疏**
   - 目前 `schema.sql` 中 demo 任务的 `prediction_result` 仍是 `step 1 / 5 / 10`
   - 还没有改成按 `forecastDays` 每天一条
3. **demo 预测点时间仍不统一**
   - demo `prediction_result.result_time` 仍是 `00:00:00`
   - 真实粮温汇总通常是 `08:40:00`
   - 这会导致同一天真实值与预测值时刻不一致，图上仍有一点割裂感
4. **粮温导入 deadlock 真实复测**
   - 第二轮止血代码已完成
   - 但“同仓库多 Excel 并发导入”真实场景还没最终确认彻底收口
5. **验收脚本可选增强**
   - `run-acceptance-smoke.ps1` 还没专门断言 `keyword` 分页
   - 还没专门断言 `pointNo` / `tempMin` / `tempMax` / `filter-options`
6. **答辩文档可选增强**
   - 可补充“当前演示历史数据统一截止到 `2026-04-25`”说明
   - 可继续整理论文截图、ER 图和预测页口径说明

【下次从这里继续】
1. 先确认本地 `8081` 是否已重启
   - 若未重启，先重启后端，再检查预测页回填效果是否正常
2. 若继续优化预测页，优先按 Mini Align 讨论：
   - 是否把 demo 任务未来预测点改成每天一条
   - 是否把 demo 预测点时间统一到 `08:40:00`
3. 若继续联调数据导入，优先复测并发导入 deadlock
4. 若转去答辩材料，直接从 `state.md` + handoff **024** + 两个 2026-04-21 plan 抽取内容

【本地环境】
- 联调端口：`8081`
- 前端默认 API：`http://localhost:8081`
- 占端口时先清理旧 Java 进程
- 库重置：`scripts/reset-demo-db.ps1`
```
