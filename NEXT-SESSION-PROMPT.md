你现在在仓库 `D:\Users\Mobius\Desktop\mine\AAA-code\bs-test-01`，请继续这个毕业设计项目，并严格优先读取和遵守以下文档，然后再开始任何分析、启动验证或开发。

重要更新（2026-04-08）：
1. 当前项目方向已正式从“多指标独立短期预测 + 展示大屏真实化优先”切换为“粮温滚动预测闭环 + 数据库重构准备”。
2. 当前正式业务主线是：
   - 固定模板粮温 XLS 导入
   - 粮温原始测点入库
   - 系统自动生成层温与整仓汇总
   - 以前 8 个月真实数据为训练参考，预测后 2 个月每天数据
   - 新真实数据回填旧预测，计算误差并验证可行性
   - 基于新真实数据修正后续预测并继续滚动
   - 增加真实高温预警与预测高温预警
3. 当前数据库真相源已经完成重写，但 Java 层和前端页面还没有跟上新模型。
4. 若旧 handoff、旧 PRD、旧数据库定稿与新文档冲突，以新增滚动预测文档和最新 handoff 为准。

必须先做的事情：
1. 读取 `zzz-docs/设计文档/粮仓环境数据预测管理平台-PRD-滚动预测闭环版.md`
2. 读取 `zzz-docs/设计文档/数据库设计-滚动预测与高温预警版.md`
3. 读取 `zzz-docs/设计文档/滚动预测改造-字段与接口变更方案.md`
4. 读取 `zzz-docs/设计文档/后端接口与DTO-VO-Mapper设计-滚动预测版.md`
5. 读取 `zzz-docs/设计文档/mock数据设计-滚动预测闭环版.md`
6. 读取 `.explore/grain-platform-bootstrap/state.md`
7. 读取 `.explore/grain-platform-bootstrap/handoffs/2026-04-08-011-schema-refactor-resume-ready.md`
8. 读取 `NEXT-SESSION-PROMPT.md` 自身，确认本轮恢复目标

当前已经完成的工作：
- 已完成任务书与开题报告的 Markdown 转换与对齐
- 已完成需求清单、来源映射、技术栈对齐
- 已完成旧版“开发指导版 PRD”与旧数据库定稿
- 已完成后端骨架、正式 Vue 前端骨架、启动脚本、8081 联调口径收口
- 已完成旧路线下的真实持久层打通、用户页真实展示、预测页真实归档回显、多指标独立预测和仪表盘首页真实化
- 已完成新一轮需求对齐，确认老师最新重点是“数据库 + 粮温滚动预测闭环 + 高温预警”
- 已新增以下新真相源文档：
  - `zzz-docs/设计文档/粮仓环境数据预测管理平台-PRD-滚动预测闭环版.md`
  - `zzz-docs/设计文档/数据库设计-滚动预测与高温预警版.md`
  - `zzz-docs/设计文档/滚动预测改造-字段与接口变更方案.md`
  - `zzz-docs/设计文档/后端接口与DTO-VO-Mapper设计-滚动预测版.md`
  - `zzz-docs/设计文档/mock数据设计-滚动预测闭环版.md`
- 已新增 schema 重构实施计划：
  - `docs/superpowers/plans/2026-04-08-rolling-forecast-schema-refactor.md`
- 已重写 `backend/src/main/resources/db/schema.sql`，当前新结构包括：
  - `grain_temp_point`
  - `grain_temp_record`
  - `grain_temp_summary`
  - 升级后的 `prediction_task`
  - 升级后的 `prediction_result`
- 已在 `schema.sql` 中写入稳定仓、风险仓、修正仓三类 mock 数据，用于演示：
  - 首次预测
  - 真实值回填
  - 修正预测
  - 高温预警
- 已完成新的 `.explore` 状态记录与最新 handoff

当前未完成 / 关键阻塞：
- 新 `schema.sql` 还没有完成真实导库验证
- 执行 `mysql -uroot -p123456 grain_env_predict --execute="SOURCE backend/src/main/resources/db/schema.sql; SHOW TABLES;"` 时返回：
  - `Access denied for user 'root'@'localhost'`
- 说明当前 MySQL 可用账号密码与文档中的 `root/123456` 不一致，必须先确认真实凭据
- 后端 Java 实体、Mapper、Service、Controller 仍是旧模型
- 前端 `PredictionView.vue`、`DataView.vue`、`frontend/src/api/grain.js` 仍是旧预测口径

当前正式路线：
- 前端正式实现使用 `Vue 3 + Vite + Vue Router + Pinia + Element Plus + ECharts + Axios`
- 后端使用 `Spring Boot + MyBatis + MySQL`
- `frontend-next/` 只作为静态原型参考和答辩成品预期展示，不作为正式实现目标
- 当前工作方式应视为“以现有项目为基础的半重写”，而不是继续沿旧路线小修小补

当前本地环境注意事项：
- 当前后端联调端口统一使用：`8081`
- 当前前端默认 API 地址：`http://localhost:8081`
- 当前 `8080` 在本机会话里可能被旧 Java 进程占用，不要误判为后端代码故障
- 当前环境已确认 `java`、`node`、`npm`、`mvn`、`mysql` 可用
- 但当前 MySQL 登录凭据需要重新确认，`root/123456` 目前无法导入新 schema

脚本启动方式：
- Windows PowerShell：
  - 一键启动：`powershell -ExecutionPolicy Bypass -File .\scripts\start-all.ps1`
  - 单独启动后端：`powershell -ExecutionPolicy Bypass -File .\scripts\start-backend.ps1`
  - 单独启动前端：`powershell -ExecutionPolicy Bypass -File .\scripts\start-frontend.ps1`
- mac：
  - 一键启动：`bash ./scripts/start-all.sh`
  - 单独启动后端：`bash ./scripts/start-backend.sh`
  - 单独启动前端：`bash ./scripts/start-frontend.sh`

最重要的约束：
- 不要再把范围扩展到机器人、数字孪生、LoRa/5G/GSM、区块链、复杂深度学习
- 不要再默认沿旧的“多指标独立短期预测”和“大屏真实化优先”路线推进
- 后续开发必须严格以新增滚动预测真相源文档为准
- 优先完成数据库、导入、滚动预测、验证修正、高温预警这条主业务链
- 不要把 `frontend-next/` 误当成正式实现目标，它只负责帮助理解成品展示效果

默认下一步任务：
1. 先确认真实可用的 MySQL 账号密码
2. 实际执行新的 `backend/src/main/resources/db/schema.sql`
3. 验证表是否创建成功，重点检查：
   - `grain_temp_point`
   - `grain_temp_record`
   - `grain_temp_summary`
   - `prediction_task`
   - `prediction_result`
4. 再按新表结构改后端实体、Mapper、Service、Controller
5. 最后改前端预测页、导入页和 API 适配层

如果用户没有改变方向，就从第 1 步开始推进。
