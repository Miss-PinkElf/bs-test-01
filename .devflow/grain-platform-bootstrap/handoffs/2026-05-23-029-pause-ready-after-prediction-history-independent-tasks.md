# Handoff

## 基础信息

- 创建时间：2026-05-23
- mission：grain-platform-bootstrap
- 当前阶段：Pause-ready after 预测历史任务恢复为独立保存
- handoff 编号：029
- 是否 superseded：否

## 当前目标

- 收口温度预测页口径：保留“预测开始时间可选”的时间修复，同时恢复“多次预测任务独立保存”的原始业务语义，避免后一次预测覆盖前一次历史任务。

## 当前进度

- 已完成本轮 Mini Align、最小范围代码修复、定向单元测试、后端编译、devflow 记录更新和恢复提示词更新。
- 当前真相源继续为 `.devflow/grain-platform-bootstrap/`。
- `.explore/grain-platform-bootstrap/` 仍仅作为历史快照，不作为当前真相源。

## 本轮完成内容

- [x] 按根目录 `NEXT-SESSION-PROMPT-DEVFLOW.md` 恢复上下文。
- [x] 使用 `devflow` + 头脑风暴确认“只撤销覆盖逻辑，保留时间相关修复”的最小方案。
- [x] 新增轻量计划：`.devflow/grain-platform-bootstrap/plans/2026-05-23-prediction-history-independent-tasks.md`。
- [x] 修改 `backend/src/main/java/com/grain/platform/service/PredictionService.java`：
  - 移除同仓库、同指标、同预测对象的新预测删旧预测逻辑。
  - 保留 `forecastStartTime`、训练样本自动截断、午夜时间对齐逻辑。
- [x] 修改 `backend/src/main/java/com/grain/platform/mapper/PredictionTaskMapper.java` 与 `backend/src/main/resources/mapper/PredictionTaskMapper.xml`：
  - 删除仅为“覆盖旧预测”引入的范围查询。
- [x] 调整 `backend/src/test/java/com/grain/platform/service/PredictionServiceTest.java`：
  - 保留指定预测开始时间、训练样本自动截断、午夜时间对齐测试。
  - 将“删除旧预测”测试改为“不会删除已有任务，历史独立保留”。
- [x] 更新 devflow 记录：
  - `.devflow/grain-platform-bootstrap/plans/active-plan-links.md`
  - `.devflow/grain-platform-bootstrap/bug-log.md`
  - `.devflow/grain-platform-bootstrap/state.md`
  - `.devflow/grain-platform-bootstrap/checkpoints.md`
  - `.devflow/grain-platform-bootstrap/handoffs/index.md`
  - `NEXT-SESSION-PROMPT-DEVFLOW.md`

## 验证结果

- `backend/`：`mvn -q test -Dtest=PredictionServiceTest` 通过。
- `backend/`：`mvn -q -DskipTests compile` 通过。

## 关键决策与原因

| 决策 | 备选方案 | 原因 |
| --- | --- | --- |
| 只撤销“覆盖旧预测”逻辑 | 回退整轮 2026-05-21 预测改动 | `forecastStartTime`、训练样本自动截断、午夜时间对齐本身是正确修复，不应一起撤销 |
| 保留前端当前历史任务切换交互 | 再追加“默认只高亮最新一条”之类展示改造 | 当前前端已支持从历史表格切换任意一次预测任务查看，本轮无需扩大改动面 |
| 本轮不补做前端构建 | 再跑一次 `frontend/ npm run build` | 本轮未修改前端代码，优先保留最小验证闭环，减少无关耗时 |
| 本轮不恢复已被误删的历史预测数据 | 直接补数据迁移或恢复脚本 | 当前需求只要求修正之后的保存语义；已被覆盖删除的数据无法从现有库内无损恢复 |

## 关键文件 / 产物

| 文件 | 作用 | 相关性 |
| --- | --- | --- |
| `backend/src/main/java/com/grain/platform/service/PredictionService.java` | 恢复历史预测独立保存 | 本轮后端核心修复 |
| `backend/src/test/java/com/grain/platform/service/PredictionServiceTest.java` | 校验不会删旧任务且时间修复仍有效 | 本轮定向验证 |
| `backend/src/main/java/com/grain/platform/mapper/PredictionTaskMapper.java` | 删除无用范围查询声明 | 覆盖逻辑清理 |
| `backend/src/main/resources/mapper/PredictionTaskMapper.xml` | 删除无用范围查询 SQL | 覆盖逻辑清理 |
| `.devflow/grain-platform-bootstrap/plans/2026-05-23-prediction-history-independent-tasks.md` | 本轮轻量计划与验证结果 | 恢复与追溯入口 |
| `NEXT-SESSION-PROMPT-DEVFLOW.md` | 下次可直接复制的恢复提示词 | 本轮收尾更新 |

## 风险 / 阻塞项 / 开放问题

- [ ] 尚未在重启后的 `8081` 后端上做预测页人工复测，需确认多次预测后历史记录会独立累积保存。
- [ ] 已经在错误覆盖口径下被删除的历史预测任务，本轮不会自动恢复。
- [ ] `schema.sql` 中 demo 任务的 `prediction_result` 仍是 `step 1 / 5 / 10`，且结果时间仍是 `00:00:00`；如重置演示库后不执行新预测，初始 demo 任务仍会显示旧口径。
- [ ] 系统管理员角色页面人工复测尚未做。
- [ ] 普通环境模板页面完整链路复测尚未做：“下载模板 -> Excel 打开/保存 -> 上传导入”。
- [ ] 粮温导入 deadlock 第二轮止血代码已完成，但“同仓库多 Excel 并发导入”真实场景仍未最终复测确认。
- [ ] 验收脚本可选增强仍未做：`keyword` 分页断言、`pointNo` / `tempMin` / `tempMax` / `filter-options` 断言。
- [ ] 答辩文档可选增强仍未做：可补“当前演示历史数据统一截止到 `2026-04-25`”、截图、ER 图和预测页口径说明。

## 立即下一步

1. 若继续验证预测页：
   - 重启本地 `8081` 后端。
   - 进入预测页，使用同一仓库、同一预测对象连续执行两次预测。
   - 确认历史记录新增两条独立任务，且仍可切换查看任意一次任务。
   - 确认同一天实际值和预测值仍落在同一个时间点，不再出现 `00:00:00` / `08:40:00` 断裂。
2. 若继续处理 demo 旧口径：
   - 单独讨论是否更新 `backend/src/main/resources/db/schema.sql` 中 demo `prediction_result`，改为每天一条并统一到 `08:40:00`。
3. 若继续做页面复测：
   - 管理员角色页面、普通环境模板导入页面、粮温 deadlock 真实并发导入可按 `state.md` 与 `NEXT-SESSION-PROMPT-DEVFLOW.md` 中的顺序继续。

## 恢复指引

1. 先读取 `.devflow/grain-platform-bootstrap/handoffs/index.md`
2. 再读取本 handoff：`.devflow/grain-platform-bootstrap/handoffs/2026-05-23-029-pause-ready-after-prediction-history-independent-tasks.md`
3. 然后读取 `.devflow/grain-platform-bootstrap/state.md`
4. 必要时读取：
   - `.devflow/grain-platform-bootstrap/plans/2026-05-23-prediction-history-independent-tasks.md`
   - `.devflow/grain-platform-bootstrap/plans/2026-05-21-prediction-start-time-and-latest-cover.md`
   - `.devflow/grain-platform-bootstrap/bug-log.md`
   - `.devflow/grain-platform-bootstrap/checkpoints.md`
   - `NEXT-SESSION-PROMPT-DEVFLOW.md`
5. 从“立即下一步”的第 1 条开始继续

## 可从活跃上下文移除的内容

- 本轮关于“页面默认高亮最新一条”和“是否影响历史任务切换”的澄清过程。
- 本轮针对 2026-05-21 覆盖逻辑来源的中间 git 检索输出。
