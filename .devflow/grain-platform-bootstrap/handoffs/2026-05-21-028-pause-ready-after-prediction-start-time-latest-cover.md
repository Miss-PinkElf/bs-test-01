# Handoff

## 基础信息

- 创建时间：2026-05-21
- mission：grain-platform-bootstrap
- 当前阶段：Pause-ready after 预测开始时间可选与最新预测覆盖旧预测
- handoff 编号：028
- 是否 superseded：否

## 当前目标

- 收口温度预测页：支持选择预测开始时间；数据库中同仓库、同指标、同预测对象以最后一次预测为准；修复预测开始时间选择后图表实际值 / 预测值错位的问题。

## 当前进度

- 已完成本轮 Mini Align、轻量计划、实现、定向单元测试、后端编译、前端构建和 devflow 记录。
- 当前真相源继续为 `.devflow/grain-platform-bootstrap/`。
- `.explore/grain-platform-bootstrap/` 仍仅作为历史快照，不作为当前真相源。

## 本轮完成内容

- [x] 按根目录 `NEXT-SESSION-PROMPT-DEVFLOW.md` 恢复上下文。
- [x] 使用 `devflow` + 头脑风暴对齐预测页方案。
- [x] 新增轻量计划：`.devflow/grain-platform-bootstrap/plans/2026-05-21-prediction-start-time-and-latest-cover.md`。
- [x] 修改 `frontend/src/views/PredictionView.vue`：
  - 预测参数区新增“预测开始”日期时间选择器。
  - 留空时仍沿用原自动顺延逻辑。
- [x] 修改 `frontend/src/api/grain.js`：
  - 预测请求新增 `forecastStartTime`。
- [x] 修改 `frontend/src/styles.css`：
  - 新增预测开始时间选择器的 CSS 类，避免行内样式。
- [x] 修改 `backend/src/main/java/com/grain/platform/dto/prediction/PredictionRequest.java`：
  - 新增 `forecastStartTime` 字段。
- [x] 修改 `backend/src/main/java/com/grain/platform/service/ForecastService.java`：
  - 支持从指定预测开始时间连续生成预测点。
- [x] 修改 `backend/src/main/java/com/grain/platform/service/PredictionService.java`：
  - 预测入库改为事务。
  - 新预测入库前按 `warehouse_id + metric_code + target_type` 删除旧 `prediction_result` 与 `prediction_task`。
  - 请求携带 `forecastStartTime` 时，训练样本默认自动截到预测开始时间之前。
  - 当预测开始时间为 `00:00:00` 时，自动对齐到真实样本采样时刻，例如粮温对齐到当天 `08:40:00`。
  - 新预测返回结果改走展示链路，预测区间内已有真实值仍会回填到图中做对照。
- [x] 修改 `backend/src/main/java/com/grain/platform/mapper/PredictionTaskMapper.java` 与 `backend/src/main/resources/mapper/PredictionTaskMapper.xml`：
  - 新增同口径旧任务 ID 查询。
- [x] 新增 `backend/src/test/java/com/grain/platform/service/PredictionServiceTest.java`：
  - 覆盖指定预测开始时间。
  - 覆盖同口径旧预测删除。
  - 覆盖训练样本自动截断。
  - 覆盖午夜预测开始时间自动对齐真实样本采样时刻。
- [x] 更新 devflow 记录：
  - `.devflow/grain-platform-bootstrap/plans/active-plan-links.md`
  - `.devflow/grain-platform-bootstrap/bug-log.md`
  - `.devflow/grain-platform-bootstrap/state.md`
  - `.devflow/grain-platform-bootstrap/checkpoints.md`

## 验证结果

- `backend/`：`mvn -q test -Dtest=PredictionServiceTest` 通过。
- `backend/`：`mvn -q -DskipTests compile` 通过。
- `frontend/`：`npm run build` 通过。
- `git diff --check` 无格式错误，仅有 CRLF 提示。

## 关键决策与原因

| 决策 | 备选方案 | 原因 |
| --- | --- | --- |
| 新预测覆盖同口径旧任务 | 只在页面展示最新一条 | 用户明确要求“数据库里面的数据，以最后一次预测为准” |
| 删除旧 `prediction_task` 与 `prediction_result` 后再插入新任务 | 只覆盖 `prediction_result` | 旧任务若保留，会继续出现在预测记录和统计口径中 |
| 选择预测开始时间时自动截断训练样本 | 保持全量历史训练并报错 | 用户要支持选择历史预测开始时间，已有后续真实值应作为图表对照，不应阻止预测 |
| 午夜预测开始时间自动对齐真实样本采样时刻 | 要求用户手工选择 `08:40:00` | 粮温真实数据固定在 `08:40:00` 左右，自动对齐能避免同日实际值 / 预测值横轴断裂 |
| 本轮不更新 `zzz-docs/` 公共设计文档 | 同步改接口设计文档 | 本轮重点是阶段性交接和恢复记录；若要写进答辩材料或正式设计文档，下轮单独收口更清晰 |

## 关键文件 / 产物

| 文件 | 作用 | 相关性 |
| --- | --- | --- |
| `frontend/src/views/PredictionView.vue` | 预测页新增预测开始时间输入 | 本轮前端核心修复 |
| `frontend/src/api/grain.js` | 预测请求新增 `forecastStartTime` | 前后端接口串联 |
| `backend/src/main/java/com/grain/platform/service/PredictionService.java` | 训练样本截断、旧预测覆盖、时间点对齐 | 本轮后端核心修复 |
| `backend/src/main/java/com/grain/platform/service/ForecastService.java` | 支持指定预测开始时间 | 预测算法入口 |
| `backend/src/test/java/com/grain/platform/service/PredictionServiceTest.java` | 定向单元测试 | 本轮新增验证 |
| `.devflow/grain-platform-bootstrap/plans/2026-05-21-prediction-start-time-and-latest-cover.md` | 本轮轻量计划与执行结果 | 恢复与追溯入口 |
| `.devflow/grain-platform-bootstrap/bug-log.md` | 问题现象、问题原因、解决方案记录 | 问题清单 |
| `NEXT-SESSION-PROMPT-DEVFLOW.md` | 下次可复制恢复提示词 | 本次收尾更新 |

## 风险 / 阻塞项 / 开放问题

- [ ] 本轮已完成后端单元测试、后端编译和前端构建，但尚未在重启后的 `8081` 后端上做完整页面人工复测。
- [ ] 需要重启本地 `8081` 后端后再复测预测页，否则页面可能仍调用旧进程。
- [ ] 旧数据库中已有 `00:00:00` 的旧预测任务不会自动迁移；重新执行同仓库、同指标、同预测对象预测后会被新预测覆盖。
- [ ] `schema.sql` 中 demo 任务的 `prediction_result` 仍是 `step 1 / 5 / 10`，且结果时间仍是 `00:00:00`；如果重置演示库后不执行新预测，初始 demo 任务仍会显示旧口径。
- [ ] 系统管理员角色页面人工复测尚未做。
- [ ] 普通环境模板页面完整链路复测尚未做：“下载模板 -> Excel 打开/保存 -> 上传导入”。
- [ ] 粮温导入 deadlock 第二轮止血代码已完成，但“同仓库多 Excel 并发导入”真实场景仍未最终复测确认。
- [ ] 验收脚本可选增强仍未做：`keyword` 分页断言、`pointNo` / `tempMin` / `tempMax` / `filter-options` 断言。
- [ ] 答辩文档可选增强仍未做：可补“当前演示历史数据统一截止到 `2026-04-25`”、截图、ER 图和预测页口径说明。

## 立即下一步

1. 若继续验证预测页：
   - 重启本地 `8081` 后端。
   - 进入预测页。
   - 选择预测开始时间后执行预测。
   - 确认预测记录中同仓库、同预测对象只保留最后一次预测。
   - 确认图表中同一天实际值和预测值落在同一个时间点，不再出现 `00:00:00` / `08:40:00` 断裂。
2. 若要彻底消除重置库后的 demo 旧口径：
   - 单独讨论是否更新 `schema.sql` 中 demo `prediction_result`，改为每天一条并统一到 `08:40:00`。
3. 若继续验证管理员角色修复：
   - 重启本地 `8081` 后端。
   - 进入用户管理页，点击“新增用户”，确认角色下拉只剩“仓库管理员”和“参观者”。
4. 若继续验证普通环境导入：
   - 重启本地 `8081` 后端。
   - 在“普通环境数据”模式下载模板，使用 Excel 打开/保存后上传。

## 恢复指引

1. 先读取 `.codex/skills/devflow/SKILL.md`。
2. 再读取 `zzz-docs/任务书.md` 与 `zzz-docs/开题报告.md`。
3. 读取 `.devflow/grain-platform-bootstrap/state.md`。
4. 读取 `.devflow/grain-platform-bootstrap/handoffs/index.md`。
5. 读取本 handoff：`.devflow/grain-platform-bootstrap/handoffs/2026-05-21-028-pause-ready-after-prediction-start-time-latest-cover.md`。
6. 按需读取：
   - `.devflow/grain-platform-bootstrap/plans/2026-05-21-prediction-start-time-and-latest-cover.md`
   - `.devflow/grain-platform-bootstrap/bug-log.md`
   - `.devflow/grain-platform-bootstrap/checkpoints.md`
   - `.devflow/grain-platform-bootstrap/plans/active-plan-links.md`
   - `.devflow/grain-platform-bootstrap/handoffs/2026-05-21-027-pause-ready-after-admin-role-assignment-guard.md`

## 可从活跃上下文移除的内容

- 本轮对预测链路、ECharts tooltip 和时间轴断点的中间排查输出。
- 用户截图中的临时页面状态。
- 本轮测试 mock 调整过程。
