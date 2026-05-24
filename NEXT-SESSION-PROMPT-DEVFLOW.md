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
6. `.devflow/grain-platform-bootstrap/handoffs/2026-05-24-031-pause-ready-after-warning-rebuild-tools-and-schema-reset-alignment.md`
7. 按需读取：
   - `.devflow/grain-platform-bootstrap/plans/active-plan-links.md`
   - `.devflow/grain-platform-bootstrap/plans/2026-05-24-dashboard-health-and-warning-threshold-fix.md`
   - `.devflow/grain-platform-bootstrap/handoffs/2026-05-24-030-pause-ready-after-dashboard-health-and-warning-threshold-fix.md`
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
- 预测页口径：支持选择预测开始时间；多次预测的数据独立保存，前端可在历史记录中切换任意一次预测任务查看。
- 粮温真实预警口径：`>= 28°C` 为 `WARNING`，`25°C ~ 28°C` 为 `ATTENTION`，`< 25°C` 为 `NORMAL`。

【最近两轮完成内容】
1. 首页健康度历史温度口径与粮温预警阈值修复
   - `DashboardMapper.xml` 健康度接口改为 `historyAvgTemp / historyMaxTemp`
   - 首页真实预警、最新粮温汇总、健康度分页读取层统一按 `max_temp` 重算真实预警等级
   - `GrainTempSummaryMapper.xml` 粮温汇总列表分页按 `max_temp` 计算有效预警口径
   - `GrainTempService.java`、`GrainTempImportService.java`、`PredictionService.java` 统一使用 `25°C` 关注下限
   - `frontend/src/views/DashboardView.vue`、`frontend/src/views/BigScreenView.vue` 已改到历史口径
   - 静态验证已通过：
     - `backend/`：`mvn -q -DskipTests compile`
     - `frontend/`：`npm run build`
2. 旧库预警回写工具与 schema 重置口径收口
   - 新增：
     - `scripts/rebuild-grain-temp-summary-warning.ps1`
     - `scripts/rebuild-grain-temp-summary-warning.sql`
   - 作用：
     - 保留当前数据库时，可一次性重算旧 `grain_temp_summary` 的 `warning_level`、`warning_flag`、`warning_message`、`analysis_result`、`analysis_remark`
   - 已修改：
     - `backend/src/main/resources/db/schema.sql`
   - 作用：
     - 重置演示库后，`grain_temp_summary` 与 `prediction_task / prediction_result` 的 demo 数据也遵守本轮统一温度规则
     - demo 预测时间已统一到 `08:40:00`
     - 低于 `25°C` 的对比仓 demo 预测样例已改回 `NORMAL`

【两种数据处理路径】
1. 保留当前数据库，只修旧汇总数据
   - 运行：`scripts/rebuild-grain-temp-summary-warning.ps1`
   - 或在 MySQL 中执行：`scripts/rebuild-grain-temp-summary-warning.sql`
2. 整库重置为新演示数据
   - 运行：`scripts/reset-demo-db.ps1`

【本轮未完成 / 未讨论完 / 开放问题】
1. **旧库回写工具还没实际执行**
   - `scripts/rebuild-grain-temp-summary-warning.ps1` 已写好，但还没在用户本地数据库上实际跑。
2. **新 `schema.sql` 还没重新跑一遍重置验证**
   - `scripts/reset-demo-db.ps1` 还没在这轮更新后的 `schema.sql` 上再做一次运行态确认。
3. **首页与环境数据页人工复测尚未完成**
   - 需要重启本地 `8081` 后端后再查看页面。
   - 重点：
     - `/environment` 中最高温 `23.x°C` 的粮温汇总应为 `NORMAL`
     - `/environment` 中最高温 `25°C ~ 28°C` 的记录应为 `ATTENTION`
     - `/dashboard` 中健康度「均温 / 峰值」不应再等同最新粮温汇总
4. **预测页人工复测尚未完成**
   - 需要重启本地 `8081` 后端后再复测页面。
   - 重点：
     - 选择预测开始时间后执行预测
     - 多次预测后历史记录独立累积保存
     - 同一天实际值和预测值不再因为 `00:00:00` / `08:40:00` 分裂
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
9. **旧预测历史不会自动迁移**
   - 已有 `00:00:00` 的旧预测任务不会自动修改。
   - 已经在错误覆盖口径下被删除的历史预测任务，本轮不会自动恢复。

【下次从这里继续】
1. 先决定走哪条路径：
   - 保留当前库：执行 `scripts/rebuild-grain-temp-summary-warning.ps1`
   - 整库重置：执行 `scripts/reset-demo-db.ps1`
2. 重启本地 `8081` 后端。
3. 进入 `/environment`：
   - 检查 `23.x°C` 是否为 `NORMAL`
   - 检查 `25°C ~ 28°C` 是否为 `ATTENTION`
4. 进入 `/dashboard`：
   - 检查健康度「均温 / 峰值」是否为历史口径
5. 若继续验证预测页：
   - 进入预测页
   - 选择预测开始时间后执行预测
   - 确认历史任务独立保存
   - 确认同一天实际值和预测值落在同一个时间点
6. 若继续验证管理员角色修复：
   - 进入用户管理页
   - 点击“新增用户”
   - 确认角色下拉只剩“仓库管理员”和“参观者”
7. 若继续验证普通环境导入修复：
   - 进入数据页，切换到“普通环境数据”
   - 下载普通环境模板
   - 用 Excel 打开/保存后上传
   - 确认不再出现第 2 行 `collectedAt` 时间格式错误

【本地环境】
- 联调端口：`8081`
- 前端默认 API：`http://localhost:8081`
- 占端口时先清理旧 Java 进程
- 旧库修正：`scripts/rebuild-grain-temp-summary-warning.ps1`
- 库重置：`scripts/reset-demo-db.ps1`
```
