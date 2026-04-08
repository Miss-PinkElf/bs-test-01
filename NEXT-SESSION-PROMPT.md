你现在在仓库 `D:\Users\Mobius\Desktop\mine\AAA-code\bs-test-01`，请继续这个毕业设计项目，并严格优先读取和遵守以下文档，然后再开始任何分析、启动验证或开发。

重要更新（2026-04-08）：
1. 当前项目主线已经再次收口，不再把“预测 -> 修正 -> 再预测”作为本期必做链路。
2. 当前本期正式主线改为：
   - 固定模板粮温 XLS 导入
   - 粮温原始测点入库
   - 系统自动生成层温与整仓汇总
   - 支持按历史真实数据做按天预测
   - 预测结果与真实结果都保存到数据库
   - 页面用两条线展示：实际值、预测值
   - 增加真实高温预警与预测高温预警
   - 支持湿度、二氧化碳简单数据录入、导入、查询与图表
3. 当前老师重点是：数据库设计、数据保存、数据管理、数据归档。
4. 修正链路本期降级为扩展功能：
   - 可以保留数据库扩展字段
   - 但本期不做修正接口和前端入口
5. 本期不拆独立归档表：
   - 预测归档继续由 `prediction_task` + `prediction_result` 承担

必须先做的事情：
1. 读取 `zzz-docs/任务书.md`
2. 读取 `zzz-docs/开题报告.md`
3. 读取 `.explore/grain-platform-bootstrap/state.md`
4. 读取 `.explore/grain-platform-bootstrap/handoffs/2026-04-08-013-pause-ready-db-first-mvp.md`
5. 再按需读取以下真相源文档：
   - `zzz-docs/设计文档/数据库设计-滚动预测与高温预警版.md`
   - `zzz-docs/设计文档/滚动预测改造-字段与接口变更方案.md`
   - `zzz-docs/设计文档/后端接口与DTO-VO-Mapper设计-滚动预测版.md`

当前已经完成的工作：
- 已完成任务书与开题报告的 Markdown 转换与对齐
- 已完成需求清单、来源映射、技术栈对齐
- 已完成旧版“开发指导版 PRD”与旧数据库定稿
- 已完成后端骨架、正式 Vue 前端骨架、启动脚本、8081 联调口径收口
- 已完成旧路线下的真实持久层打通、用户页真实展示、预测页真实归档回显、多指标独立预测和仪表盘首页真实化
- 已完成滚动预测阶段的真相源重写
- 已重写 `backend/src/main/resources/db/schema.sql`
- 已完成 `.explore` 状态记录与最新 handoff 更新
- 已完成本轮需求对齐，确认本期改为“数据库优先 MVP”

当前正式本期路线：
- 前端正式实现使用 `Vue 3 + Vite + Vue Router + Pinia + Element Plus + ECharts + Axios`
- 后端使用 `Spring Boot + MyBatis + MySQL`
- `frontend-next/` 只作为静态原型参考，不作为正式实现目标
- 当前工作方式应视为“以现有项目为基础的半重写”

当前数据库 / 业务口径：
- 粮温走主线：
  - `grain_temp_point`
  - `grain_temp_record`
  - `grain_temp_summary`
- 预测归档继续走：
  - `prediction_task`
  - `prediction_result`
- 湿度、二氧化碳继续走简单单值表：
  - `sensor_data`
- 修正链字段可以保留：
  - `parent_task_id`
  - `task_round`
  - `trigger_type`
  - `adjust_status`
  - `is_corrected`
- 但本期不要实现修正入口和修正页面

当前未完成 / 关键阻塞：
- 新 `schema.sql` 还没有完成真实导库验证
- 执行 `mysql -uroot -p123456 grain_env_predict --execute="SOURCE backend/src/main/resources/db/schema.sql; SHOW TABLES;"` 时返回：
  - `Access denied for user 'root'@'localhost'`
- 说明当前 MySQL 可用账号密码与文档中的 `root/123456` 不一致，必须先确认真实凭据
- 后端 Java 实体、Mapper、Service、Controller 仍是旧预测口径
- 前端 `PredictionView.vue`、`DataView.vue`、`frontend/src/api/grain.js` 仍未按新主线改造

当前本地环境注意事项：
- 当前后端联调端口统一使用：`8081`
- 当前前端默认 API 地址：`http://localhost:8081`
- 当前 `8080` 在本机会话里可能被旧 Java 进程占用，不要误判为后端代码故障
- 当前环境已确认 `java`、`node`、`npm`、`mvn`、`mysql` 可用
- 但当前 MySQL 登录凭据需要重新确认

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
- 不要再把“修正链本期必做”当作默认前提
- 本期优先完成数据库、导入、保存、管理、归档、双线图、高温预警这条主链
- 不要把 `frontend-next/` 误当成正式实现目标

默认下一步任务：
1. 先确认真实可用的 MySQL 账号密码
2. 实际执行新的 `backend/src/main/resources/db/schema.sql`
3. 验证表是否创建成功，重点检查：
   - `grain_temp_point`
   - `grain_temp_record`
   - `grain_temp_summary`
   - `prediction_task`
   - `prediction_result`
   - `sensor_data`
4. 再按新表结构改后端实体、Mapper、Service、Controller
   - 保留扩展字段
   - 但本期不实现修正入口
5. 最后改前端：
   - 预测页支持可选预测天数
   - 图表展示实际值 / 预测值双线
   - 导入页区分粮温与普通环境数据
   - API 适配层切换到当前主线

如果用户没有再次改变方向，就从第 1 步开始推进。
