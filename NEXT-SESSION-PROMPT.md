你现在在仓库 `D:\Users\Mobius\Desktop\mine\AAA-code\bs-test-01`，请继续这个毕业设计项目，并严格优先读取和遵守以下文档，然后再开始任何分析、启动验证或开发。

必须先读取：
1. `zzz-docs/任务书.md`
2. `zzz-docs/开题报告.md`
3. `.explore/grain-platform-bootstrap/state.md`
4. `.explore/grain-platform-bootstrap/handoffs/2026-04-08-015-dashboard-and-fixed-template-pause-ready.md`
5. 再按需读取以下真相源文档：
   - `zzz-docs/设计文档/数据库设计-滚动预测与高温预警版.md`
   - `zzz-docs/设计文档/滚动预测改造-字段与接口变更方案.md`
   - `zzz-docs/设计文档/后端接口与DTO-VO-Mapper设计-滚动预测版.md`
   - `zzz-docs/验证/数据库优先MVP-回归验证清单.md`

当前唯一有效主线：
- 本期是“数据库优先 MVP”
- 不再把“预测 -> 修正 -> 再预测”作为本期必做链路
- 修正链字段可以保留，但本期不做修正接口和前端入口
- 不拆独立归档表，预测归档继续由 `prediction_task + prediction_result` 承担
- `frontend-next/` 只作为静态原型参考，不是正式实现目标

本轮最新已完成：
- 首页仪表盘已切到预警优先口径：
  - 后端 `dashboard` 聚合优先展示 `grain_temp_summary.warning_*` 与 `prediction_result.warning_*`
  - `DashboardView.vue` 与 `BigScreenView.vue` 已同步新统计口径
- 粮温导入已从“行式 MVP”升级到固定模板方向：
  - `/api/grain-temp/import/template` 现在下载固定 `XLSX` 模板
  - `GrainTempImportService` 已支持“基础信息 + 层号/点位矩阵”解析
  - 旧 CSV / 行式 Excel 兼容逻辑仍保留
- 已补回归验证清单：
  - `zzz-docs/验证/数据库优先MVP-回归验证清单.md`
- 已完成静态验证：
  - `backend/` 的 `mvn -q -DskipTests compile` 通过
  - `frontend/` 的 `npm run build` 通过

当前明确未完成：
- 运行态 smoke 还没补完
- 当前阻塞不是首页或模板代码本身，而是本机 MySQL 认证失败
- 关键报错：`Access denied for user 'root'@'localhost' (using password: YES)`

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

当前下一步重点：
1. 先恢复本机 MySQL 可用凭据或 Spring Boot 本地连接配置
2. 再按 `zzz-docs/验证/数据库优先MVP-回归验证清单.md` 补运行态 smoke：
   - `/api/dashboard/overview`
   - `/api/grain-temp/import/template`
   - 固定 XLSX 导入
   - 旧 CSV / 行式 Excel 兼容
3. 若 smoke 补齐成功，再考虑补一个 PowerShell 验收脚本

当前本地环境注意事项：
- 当前后端联调端口统一使用：`8081`
- 当前前端默认 API 地址：`http://localhost:8081`
- 当前 `8080` 在本机会话里可能被旧 Java 进程占用，不要误判为后端代码故障
- 若旧文档与最新 handoff / state 冲突，以最新 handoff 和 `state.md` 为准
- 当前运行态阻塞优先级高于继续做新功能

如果用户没有再次改变方向，就从“下一步重点”的第 1 条开始推进。
