```
你现在在仓库 `E:\Learn\Vs\Code\bs-test-01`，请继续这个毕业设计项目，并严格优先读取和遵守以下文档，然后再开始任何分析、启动验证或开发。

必须先做的事情：
1. 读取 `zzz-docs/开发指导版PRD-粮仓环境数据预测管理平台.md`
2. 读取 `zzz-docs/设计文档/需求清单-任务书与开题报告对齐.md`
3. 读取 `.explore/grain-docs-alignment/handoffs/2026-04-06-001-resume-ready.md`
4. 读取 `.explore/grain-platform-bootstrap/handoffs/2026-04-07-008-prediction-history-and-user-page-real-api-resume-ready.md`
5. 读取 `.explore/grain-platform-bootstrap/handoffs/2026-04-07-007-import-mock-data-and-startup-fixes-resume-ready.md`
6. 读取 `.explore/grain-platform-bootstrap/handoffs/2026-04-07-006-controller-logs-and-resume-ready.md`
7. 读取 `.explore/grain-platform-bootstrap/handoffs/2026-04-06-005-startup-scripts-and-resume-ready.md`
8. 读取 `.explore/grain-platform-bootstrap/handoffs/2026-04-06-004-backend-real-persistence-resume-ready.md`
9. 读取 `.explore/grain-docs-alignment/state.md` 和 `.explore/grain-platform-bootstrap/state.md`
10. 读取 `zzz-docs/设计文档/数据库设计定稿.md`
11. 读取 `zzz-docs/设计文档/后端接口与DTO-VO-Mapper设计.md`
12. 读取 `frontend-next/README.md`
13. 读取 `NEXT-SESSION-PROMPT.md` 自身，确认本轮恢复目标

当前已经完成的工作：
- 已完成任务书与开题报告的 Markdown 转换与对齐
- 已完成需求清单、来源映射、技术栈对齐
- 已完成“开发指导版 PRD”
- 已完成后端骨架、Vue 早期骨架、PowerShell 启动脚本
- 已完成 `frontend-next/` 的高保真静态原型增强，补齐了用户管理、环境数据图表、预测页、展示大屏和原型说明文档
- 已修复 `frontend-next/` 的 Next SWC 依赖问题，并重新通过 `npm run build` 验证
- 已完成数据库 SQL 定稿和表说明
- 已完成后端接口清单、DTO/VO、Mapper 设计
- 已将 `frontend/` 升级为正式版 Vue 前端骨架，接入 `Vue Router + Pinia + Element Plus + Axios + ECharts`
- 已补齐正式前端的登录、仪表盘、用户管理、仓库管理、环境数据、预测页和展示大屏入口
- 已在 `frontend/` 执行 `npm install` 和 `npm run build`，构建通过
- 已完成后端主链路从 `DemoDataService` 到真实 `MyBatis + MySQL` 的替换
- 已在真实数据库下打通登录、仓库、环境数据、预测归档四条后端主链路
- 已在 `8081` 端口完成最小联调验证
- 已将 Windows 启动脚本统一收口到 `8081`，并新增 mac 启动脚本：
  - `scripts/start-backend.ps1`
  - `scripts/start-frontend.ps1`
  - `scripts/start-all.ps1`
  - `scripts/start-backend.sh`
  - `scripts/start-frontend.sh`
  - `scripts/start-all.sh`
- 已为后端核心 Controller 增加简短接口日志与简单注释，提交哈希：`f97f08c`
- 已完成环境数据 CSV / Excel 导入、CSV 模板下载与乱码修复
- 已将仓库管理、环境数据页的手动录入改为弹窗
- 已补充数据库 mock 数据，并已实际导入本地 MySQL
- 已将前端默认接口地址与后端配置收口到 `8081`
- 已为 Windows / mac 后端脚本增加 8081 端口占用检查与自动清理
- 已修复 Windows `start-backend.ps1` 的 PowerShell `$PID` 冲突报错
- 已完成预测页真实归档与点击回显闭环：
  - 历史归档记录已切到真实接口
  - 执行预测后会自动刷新历史列表
  - 点击历史归档记录可回显任务摘要、结果表格和图表
- 已完成用户页真实接口与角色选项展示：
  - 新增 `GET /api/users`
  - 新增 `GET /api/roles/options`
  - `frontend/src/views/UsersView.vue` 已切到真实用户列表、真实角色说明和真实统计卡
- 已完成指标选项真实接口与多指标独立预测主线：
  - 新增 `GET /api/metrics/options`
  - 已将预测接口收口为 `POST /api/predictions`
  - 已支持温度、湿度、二氧化碳浓度按所选指标独立预测
  - 已按指标阈值计算风险等级
- 已在 `backend/src/main/resources/db/schema.sql` 中补充湿度与二氧化碳浓度预测 mock 数据
- 已完成仪表盘首页真实化：
  - 近期预警真实化
  - 仓库健康度真实化
  - 最近采样记录真实化
  - 预测归档卡片不再使用 mock 兜底
- 已同步开发指导版 PRD、后端接口设计、数据库设计、README 与 `.explore/` 主状态文档
- 最近已提交：
  - `f7039e8` `补齐预测页真实归档与回显交互`
  - `6ea2343` `补齐用户页真实接口与角色选项展示`
  - 本轮新增改动尚未提交

当前正式路线：
- 前端正式实现使用 `Vue 3 + Vite + Vue Router + Pinia + Element Plus + ECharts + Axios`
- 后端使用 `Spring Boot + MyBatis + MySQL`
- `frontend-next/` 只作为静态原型参考和答辩成品预期展示，不作为正式前端路线

当前本地环境注意事项：
- 本地数据库名：`grain_env_predict`
- 数据库用户名：`root`
- 数据库密码：`123456`
- 当前后端联调端口统一使用：`8081`
- 当前前端默认 API 地址：`http://localhost:8081`
- 如果继续联调，优先让前端请求指向 `http://localhost:8081`
- 当前 `8080` 在本机会话里可能被旧 Java 进程占用，不要误判为后端代码故障
- 当前环境已确认 `mvn`、`mysql` 可用
- 当前数据库 mock 数据已确认导入：
  - `warehouse = 6`
  - `sensor_data = 36`
  - `prediction_task = 5+`（已补湿度与二氧化碳浓度预测样本）
- 当前后端核心接口启动后应能直接看到简短日志输出
- Windows `scripts/start-backend.ps1` 启动前会自动清理 `8081`
- 当前指标选项真实接口：`GET /api/metrics/options`
- 当前预测接口：`POST /api/predictions`
- 当前预测归档真实结构以 `prediction_task + prediction_result` 为准，数据库表结构本轮未新增字段

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
- 后续开发必须严格以开发指导版 PRD 为准
- 优先完成完整业务流程，而不是堆高级功能
- 不要把 `frontend-next/` 误当成正式实现目标，它只负责帮助理解最终成品会长什么样

默认下一步任务：
1. 已完成指标选项真实接口、多指标独立预测主线与仪表盘首页真实化：
   - 已新增 `GET /api/metrics/options`
   - 已将前端硬编码指标切到真实接口
   - 已将预测接口收口为 `POST /api/predictions`
   - 已支持温度、湿度、二氧化碳浓度按所选指标独立预测
   - 已完成仪表盘首页概览、近期预警、仓库健康度、最近采样记录真实化
2. 下一步优先推进展示大屏进一步真实化：
   - 尽量复用 `fetchOverview()` 的真实数据
   - 继续替换大屏中的 mock 指标与预警底板
   - 收口展示文案里旧的“温度预测”单指标口径
3. 完成大屏后，再评估是否补用户新增、编辑、状态切换等正式操作接口
4. 如进入收尾阶段，再统一清理旧 handoff / spec / 历史文档里的旧口径
5. 本轮已经补了 `.explore/` handoff 与 `NEXT-SESSION-PROMPT.md`，下一次可直接按本提示词恢复

如果用户没有改变方向，就从第 2 步开始推进。
```
