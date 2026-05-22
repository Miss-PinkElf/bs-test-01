```text
你现在在仓库根目录下的 `bs-test-01`。请继续毕业设计项目，并严格先走 devflow 恢复流程后再开始任何分析、验证或开发。

【协作硬约束（必读）】
- 必须始终使用简体中文。
- 实现类需求（多文件、前后端、新交互）默认须先输出：理解 + 方案 + 待确认，禁止同一条回复里直接大范围改代码。
- 只有我明确说「直接做」「不用讨论」等豁免语时，才可跳过对齐。
- 过程记录默认走 `devflow`，当前 mission 真相源在 `.devflow/grain-platform-bootstrap/`。
- 进入实现前先头脑风暴；完成代码修改后先询问是否需要提交，除非我已经明确要求提交。
- 如果提交，提交信息必须使用中文。
- 不需要做全局 ESLint；不影响运行的 TypeScript 报错可以先不处理，若要顺手修 TS 错误必须先确认。
- 仓库内提到路径时使用相对路径。

【必须先读取】
1. `.codex/skills/devflow/SKILL.md`
2. `zzz-docs/任务书.md`
3. `zzz-docs/开题报告.md`
4. `.devflow/grain-platform-bootstrap/state.md`
5. `.devflow/grain-platform-bootstrap/handoffs/index.md`
6. `.devflow/grain-platform-bootstrap/handoffs/2026-05-23-029-pause-ready-after-prediction-history-independent-tasks.md`
7. 按需读取：
   - `.devflow/grain-platform-bootstrap/plans/active-plan-links.md`
   - `.devflow/grain-platform-bootstrap/plans/2026-05-21-prediction-start-time-and-latest-cover.md`
   - `.devflow/grain-platform-bootstrap/bug-log.md`
   - `.devflow/grain-platform-bootstrap/checkpoints.md`
   - `.devflow/grain-platform-bootstrap/handoffs/2026-05-21-027-pause-ready-after-admin-role-assignment-guard.md`
   - `zzz-docs/验证/数据库优先MVP-回归验证清单.md`
   - `zzz-docs/设计文档/项目整体架构与前后端初学者说明.md`
   - `zzz-docs/设计文档/数据库设计与表字段初学者说明.md`

【当前主线与口径】
- 本期仍是「数据库优先 MVP」。
- 不做「预测 -> 修正 -> 再预测」必做入口，修正相关字段可以保留为扩展。
- 预测归档继续使用：`prediction_task + prediction_result`。
- 温度主线走粮温模块；普通环境数据主要维护湿度和二氧化碳。
- 正式前端：`frontend/`；`frontend-next/` 仅静态原型参考。
- `.explore/grain-platform-bootstrap/` 仅历史快照，不作为当前真相源。
- 权限口径：`ADMIN` 是系统内置总管理员角色，不允许在用户管理里新增分配给普通账号；新增用户只允许选择仓库管理员和参观者。
- 预测页新口径：支持选择预测开始时间；多次预测的数据独立保存，前端可在历史记录中切换任意一次预测任务查看。

【最近一次完成：预测历史任务恢复为独立保存，并保留预测开始时间修复】
1. 问题现象：
   - 预测页只能选择预测天数，不能选择预测开始时间。
   - 2026-05-21 曾误把逻辑改成“后一次预测覆盖前一次”，导致历史预测无法并存回看。
   - 页面复测时，选择历史预测开始时间会报“预测开始时间应晚于训练样本最后时间”。
   - 图表中同一天实际值和预测值看起来断开，tooltip 在 `08:40:00` 的真实点上显示预测值为空。
2. 问题原因：
   - `frontend/src/views/PredictionView.vue` 只提交 `forecastDays`。
   - `PredictionRequest` 没有 `forecastStartTime`。
   - `ForecastService` 固定从训练样本最后时间后一日开始预测。
   - `PredictionService.predict` 原本每次新增 `prediction_task + prediction_result`，2026-05-21 又被误加了“按范围删除旧预测”的逻辑。
   - 未设置高级训练区间时，后端会把该仓库全部真实粮温汇总纳入训练样本；如果已有后续真实值，就会误判预测开始时间不晚于训练样本最后时间。
   - 页面选择日期时间时可能提交 `00:00:00`，而真实粮温汇总通常是 `08:40:00`，导致同一天被拆成两个横轴点。
3. 已完成修复：
   - `frontend/src/views/PredictionView.vue`
     - 预测参数区新增“预测开始”日期时间选择器。
     - 留空时仍沿用自动顺延逻辑。
   - `frontend/src/api/grain.js`
     - 预测请求新增 `forecastStartTime`。
   - `frontend/src/styles.css`
     - 新增预测开始时间选择器 CSS 类，避免行内样式。
   - `backend/src/main/java/com/grain/platform/dto/prediction/PredictionRequest.java`
     - 新增 `forecastStartTime`。
   - `backend/src/main/java/com/grain/platform/service/ForecastService.java`
     - 支持从指定预测开始时间连续生成预测点。
   - `backend/src/main/java/com/grain/platform/service/PredictionService.java`
     - `predict` 改为事务。
     - 请求携带 `forecastStartTime` 时，训练样本默认自动截到预测开始时间之前。
     - 预测开始时间为 `00:00:00` 时，自动对齐到训练样本最后一条的采样时刻，例如粮温对齐到当天 `08:40:00`。
     - 新预测返回结果改走展示链路，预测区间内已有真实值仍会回填到图中做对照。
   - `backend/src/test/java/com/grain/platform/service/PredictionServiceTest.java`
     - 覆盖指定预测开始时间、保留历史任务、训练样本自动截断、午夜预测开始时间自动对齐真实样本采样时刻。
   - `.devflow/grain-platform-bootstrap/plans/2026-05-23-prediction-history-independent-tasks.md`
     - 记录本次对 2026-05-21 错误口径的纠偏。
   - 已新增 / 更新 devflow 记录：
     - `.devflow/grain-platform-bootstrap/plans/2026-05-21-prediction-start-time-and-latest-cover.md`
     - `.devflow/grain-platform-bootstrap/plans/active-plan-links.md`
     - `.devflow/grain-platform-bootstrap/bug-log.md`
     - `.devflow/grain-platform-bootstrap/state.md`
     - `.devflow/grain-platform-bootstrap/checkpoints.md`
     - `.devflow/grain-platform-bootstrap/handoffs/2026-05-23-029-pause-ready-after-prediction-history-independent-tasks.md`
4. 已通过验证：
   - `backend/`：`mvn -q test -Dtest=PredictionServiceTest`
   - `backend/`：`mvn -q -DskipTests compile`

【本轮未完成 / 未讨论完 / 开放问题】
1. **预测页人工复测尚未在最新后端上完整完成**
   - 后端单测、后端编译已通过。
   - 需要重启本地 `8081` 后端后再复测页面。
   - 复测重点：
     - 选择预测开始时间后执行预测。
     - 多次预测后历史记录会独立累积保存。
     - 同一天实际值和预测值不再因为 `00:00:00` / `08:40:00` 分裂成两个横轴点。
2. **旧数据库中的旧预测任务不会自动迁移**
   - 已有 `00:00:00` 的旧预测任务不会自动修改。
   - 已经在错误覆盖口径下被删除的历史预测任务，本轮不会自动恢复。
3. **demo 种子预测结果仍偏旧口径**
   - `backend/src/main/resources/db/schema.sql` 中 demo 任务的 `prediction_result` 仍是 `step 1 / 5 / 10`。
   - demo `prediction_result.result_time` 仍是 `00:00:00`。
   - 如果重置演示库后不执行新预测，初始 demo 任务仍会显示旧口径。
4. **系统管理员角色页面人工复测尚未做**
   - 已完成单元测试、后端编译和前端构建。
   - 尚未启动页面点击验证：“用户管理 -> 新增用户 -> 角色下拉只剩仓库管理员和参观者”。
   - 复测前先确认本地 `8081` 后端已重启到最新代码。
5. **后端运行态负向 smoke 可选**
   - 可直接调用新增 / 编辑用户接口提交 `ADMIN`，确认后端返回业务错误。
6. **历史误建管理员账号未自动清理**
   - 已阻止继续新增、提权和删除管理员账号。
   - 如果数据库中已经存在历史误建的第二管理员，本轮不会自动停用或降权；如需要，下次单独讨论处理策略。
7. **普通环境模板页面实测尚未做**
   - 后端解析单测与编译已通过。
   - 还没有做浏览器页面完整链路：“下载普通环境模板 -> Excel 打开/保存 -> 上传导入”。
8. **粮温导入 deadlock 真实复测**
   - 第二轮止血代码已完成。
   - 但“同仓库多 Excel 并发导入”真实场景还没最终确认彻底收口。
9. **验收脚本可选增强**
   - `run-acceptance-smoke.ps1` 还没专门断言 `keyword` 分页。
   - 还没专门断言 `pointNo` / `tempMin` / `tempMax` / `filter-options`。
10. **答辩文档可选增强**
   - 可补充“当前演示历史数据统一截止到 `2026-04-25`”说明。
   - 可继续整理论文截图、ER 图和预测页口径说明。

【下次从这里继续】
1. 若继续验证预测页修复：
   - 先重启本地 `8081` 后端。
   - 进入预测页。
   - 选择预测开始时间后执行预测。
   - 确认预测记录会新增独立历史任务，且仍可切换查看任意一次任务。
   - 确认图表中同一天实际值和预测值落在同一个时间点。
2. 若要彻底消除重置库后的 demo 旧口径：
   - 先按 Mini Align 讨论是否更新 `schema.sql` 中 demo `prediction_result`。
   - 可选方向：按 `forecastDays` 每天一条，并统一到 `08:40:00`。
3. 若继续验证管理员角色修复：
   - 先重启本地 `8081` 后端。
   - 进入用户管理页。
   - 点击“新增用户”。
   - 确认角色下拉只剩“仓库管理员”和“参观者”，不再可选“管理员”。
   - 可选：用接口提交 `ADMIN` 做后端负向 smoke。
4. 若继续验证普通环境导入修复：
   - 先重启本地 `8081` 后端。
   - 进入数据页，切换到“普通环境数据”。
   - 下载普通环境模板。
   - 可用 Excel 打开/保存后上传，确认不再出现第 2 行 `collectedAt` 时间格式错误。
5. 若继续数据导入联调：
   - 先复测普通环境模板导入。
   - 再复测“同仓库多 Excel 并发导入”粮温 deadlock 场景。
6. 若继续写答辩或交接材料：
   - 优先复用两份初学者文档和里面的 PlantUML 图，再按目标场景裁剪。

【本地环境】
- 联调端口：`8081`
- 前端默认 API：`http://localhost:8081`
- 占端口时先清理旧 Java 进程
- 库重置：`scripts/reset-demo-db.ps1`
```
