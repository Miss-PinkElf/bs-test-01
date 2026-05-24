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
6. `.devflow/grain-platform-bootstrap/handoffs/2026-05-24-030-pause-ready-after-dashboard-health-and-warning-threshold-fix.md`
7. 按需读取：
   - `.devflow/grain-platform-bootstrap/plans/active-plan-links.md`
   - `.devflow/grain-platform-bootstrap/plans/2026-05-24-dashboard-health-and-warning-threshold-fix.md`
   - `.devflow/grain-platform-bootstrap/plans/2026-05-23-prediction-history-independent-tasks.md`
   - `.devflow/grain-platform-bootstrap/plans/2026-05-21-prediction-start-time-and-latest-cover.md`
   - `.devflow/grain-platform-bootstrap/bug-log.md`
   - `.devflow/grain-platform-bootstrap/checkpoints.md`
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
- 粮温真实预警口径：`>= 28°C` 为 `WARNING`，`25°C ~ 28°C` 为 `ATTENTION`，`< 25°C` 为 `NORMAL`。

【最近一次完成：首页健康度历史温度口径与粮温预警阈值修复】
1. 问题现象：
   - 首页「仓库运行健康度」中的「均温 / 峰值」显示效果与「最新粮温汇总」非常接近，不符合“展示历史汇总均值与峰值”的理解。
   - 环境数据页中，6 号仓最高温仅 `23.x°C` 却显示 `ATTENTION`。
2. 问题原因：
   - `DashboardMapper.xml` 的健康度 SQL 混用了最新真实均温与最新预测峰值。
   - `schema.sql` 演示种子数据中对 6 号仓写了 `23.20` 的特殊低阈值预警。
   - 粮温服务与预测服务此前使用 `28 * 0.9 = 25.2°C` 作为关注下限。
   - 读取层直接信任库里已有的 `warning_level` / `warning_flag`，旧脏标记会继续影响页面。
3. 已完成修复：
   - `backend/src/main/resources/mapper/DashboardMapper.xml`
     - 健康度接口改为返回 `historyAvgTemp` / `historyMaxTemp`
     - 首页真实预警、最新粮温汇总、健康度分页读取层统一按 `max_temp` 重算真实预警等级
   - `backend/src/main/resources/mapper/GrainTempSummaryMapper.xml`
     - 粮温汇总列表分页按 `max_temp` 计算有效预警等级与说明
   - `backend/src/main/resources/db/schema.sql`
     - 移除 6 号仓 `23.20` 特殊低阈值预警
     - 演示库粮温汇总统一为 `>= 28` / `>= 25` / `< 25` 三段规则
   - `backend/src/main/java/com/grain/platform/service/GrainTempService.java`
   - `backend/src/main/java/com/grain/platform/service/GrainTempImportService.java`
   - `backend/src/main/java/com/grain/platform/service/PredictionService.java`
     - 手动重算、导入汇总、温度预测风险等级统一使用 `25°C` 关注下限
   - `frontend/src/api/grain.js`
   - `frontend/src/views/DashboardView.vue`
   - `frontend/src/views/BigScreenView.vue`
     - 首页与大屏改读历史温度字段，大屏兜底文案从“预测峰值”收口为“峰值”
   - 已新增 / 更新 devflow 记录：
     - `.devflow/grain-platform-bootstrap/plans/2026-05-24-dashboard-health-and-warning-threshold-fix.md`
     - `.devflow/grain-platform-bootstrap/bug-log.md`
     - `.devflow/grain-platform-bootstrap/checkpoints.md`
     - `.devflow/grain-platform-bootstrap/state.md`
     - `.devflow/grain-platform-bootstrap/handoffs/2026-05-24-030-pause-ready-after-dashboard-health-and-warning-threshold-fix.md`
4. 已通过验证：
   - `backend/`：`mvn -q -DskipTests compile`
   - `frontend/`：`npm run build`

【本轮未完成 / 未讨论完 / 开放问题】
1. **首页与环境数据页人工复测尚未完成**
   - 需要重启本地 `8081` 后端后再查看页面。
   - 复测重点：
     - `/environment` 中最高温 `23.x°C` 的粮温汇总应为 `NORMAL`
     - `/environment` 中最高温 `25°C ~ 28°C` 的记录应为 `ATTENTION`
     - `/dashboard` 中健康度「均温 / 峰值」不应再等同最新粮温汇总
2. **预测页人工复测尚未在最新后端上完整完成**
   - 需要重启本地 `8081` 后端后再复测页面。
   - 复测重点：
     - 选择预测开始时间后执行预测
     - 多次预测后历史记录独立累积保存
     - 同一天实际值和预测值不再因为 `00:00:00` / `08:40:00` 分裂
3. **旧数据库中的旧预测任务不会自动迁移**
   - 已有 `00:00:00` 的旧预测任务不会自动修改。
   - 已经在错误覆盖口径下被删除的历史预测任务，本轮不会自动恢复。
4. **demo 种子预测结果仍偏旧口径**
   - `backend/src/main/resources/db/schema.sql` 中 demo 任务的 `prediction_result` 仍是 `step 1 / 5 / 10`
   - demo `prediction_result.result_time` 仍是 `00:00:00`
   - 如果重置演示库后不执行新预测，初始 demo 任务仍会显示旧口径
5. **系统管理员角色页面人工复测尚未做**
   - 已完成单元测试、后端编译和前端构建。
   - 尚未启动页面点击验证：“用户管理 -> 新增用户 -> 角色下拉只剩仓库管理员和参观者”。
6. **普通环境模板页面实测尚未做**
   - 后端解析单测与编译已通过。
   - 还没有做浏览器页面完整链路：“下载普通环境模板 -> Excel 打开/保存 -> 上传导入”。
7. **粮温导入 deadlock 真实复测**
   - 第二轮止血代码已完成。
   - “同仓库多 Excel 并发导入”真实场景还没最终确认彻底收口。
8. **验收脚本可选增强**
   - `run-acceptance-smoke.ps1` 还没专门断言 `keyword` 分页。
   - 还没专门断言 `pointNo` / `tempMin` / `tempMax` / `filter-options`。
9. **答辩文档可选增强**
   - 可补充“当前演示历史数据统一截止到 `2026-04-25`”说明。
   - 可继续整理论文截图、ER 图和预测页口径说明。

【下次从这里继续】
1. 若继续验证首页与环境数据页修复：
   - 先重启本地 `8081` 后端
   - 进入 `/environment`
   - 检查 `23.x°C` 是否不再触发 `ATTENTION`
   - 再进入 `/dashboard`
   - 检查健康度「均温 / 峰值」是否为历史口径
2. 若继续验证预测页修复：
   - 先重启本地 `8081` 后端
   - 进入预测页
   - 选择预测开始时间后执行预测
   - 确认预测记录会新增独立历史任务，且仍可切换查看任意一次任务
   - 确认图表中同一天实际值和预测值落在同一个时间点
3. 若要彻底消除重置库后的 demo 旧口径：
   - 先按 Mini Align 讨论是否更新 `schema.sql` 中 demo `prediction_result`
   - 可选方向：按 `forecastDays` 每天一条，并统一到 `08:40:00`
4. 若继续验证管理员角色修复：
   - 先重启本地 `8081` 后端
   - 进入用户管理页
   - 点击“新增用户”
   - 确认角色下拉只剩“仓库管理员”和“参观者”
5. 若继续验证普通环境导入修复：
   - 先重启本地 `8081` 后端
   - 进入数据页，切换到“普通环境数据”
   - 下载普通环境模板
   - 用 Excel 打开/保存后上传，确认不再出现第 2 行 `collectedAt` 时间格式错误

【本地环境】
- 联调端口：`8081`
- 前端默认 API：`http://localhost:8081`
- 占端口时先清理旧 Java 进程
- 库重置：`scripts/reset-demo-db.ps1`
```
