你现在在仓库 `D:\Users\Mobius\Desktop\mine\AAA-code\bs-test-01`，请继续这个毕业设计项目，并严格优先读取和遵守以下文档，然后再开始任何分析、启动验证或开发。

必须先读取：
1. `zzz-docs/任务书.md`
2. `zzz-docs/开题报告.md`
3. `.explore/grain-platform-bootstrap/state.md`
4. `.explore/grain-platform-bootstrap/handoffs/2026-04-09-017-frontend-list-unification-and-data-pagination-ready.md`
5. 再按需读取以下真相源文档：
   - `zzz-docs/设计文档/数据库设计-滚动预测与高温预警版.md`
   - `zzz-docs/设计文档/滚动预测改造-字段与接口变更方案.md`
   - `zzz-docs/设计文档/后端接口与DTO-VO-Mapper设计-滚动预测版.md`
   - `zzz-docs/验证/数据库优先MVP-回归验证清单.md`
   - `docs/superpowers/plans/2026-04-09-data-crud-and-temperature-recompute.md`
   - `docs/superpowers/plans/2026-04-09-frontend-list-unification.md`

当前唯一有效主线：
- 本期是“数据库优先 MVP”
- 不再把“预测 -> 修正 -> 再预测”作为本期必做链路
- 修正链字段可以保留，但本期不做修正接口和前端入口
- 不拆独立归档表，预测归档继续由 `prediction_task + prediction_result` 承担
- `frontend-next/` 只作为静态原型参考，不是正式实现目标

本轮最新已完成：
- 本机 MySQL 已恢复为默认可用凭据：
  - `root / 123456`
  - `backend/src/main/resources/application.yml` 可直接连接
- 数据库优先 MVP 运行态 smoke 已补齐：
  - `/api/dashboard/overview`
  - `/api/grain-temp/import/template`
  - 固定 XLSX 导入
  - 旧 CSV / 旧行式 `.xls` 兼容
  - `/api/predictions/tasks`
  - `/api/predictions/tasks/1`
- 数据主线 CRUD 已完成：
  - `grain_temp_record` 已支持新增 / 编辑 / 删除
  - `sensor_data` 已支持新增 / 编辑 / 删除
  - 温度原始记录变更后会自动重算同仓同时间的 `grain_temp_summary` 与真实预警
  - `frontend/src/views/DataView.vue` 已升级为统一数据维护页
- 后台页列表展示已完成第一轮统一：
  - `UsersView.vue`、`WarehouseView.vue`、`DataView.vue`、`PredictionView.vue` 已统一补上 `Element Plus` 表格分页
  - `PredictionView.vue` 的历史归档记录已由卡片流改为表格分页
  - `DashboardView.vue` 的仓库健康度已改为表格分页
  - `DashboardView.vue` 的近期预警已改为 `el-scrollbar` + 下滑增量加载
- 数据管理页记录列表已完成后端分页适配：
  - `GET /api/grain-temp/records` 已切到分页返回
  - `GET /api/sensor-data` 已切到分页返回
  - `DataView.vue` 的粮温原始记录表与环境数据表已改为后端分页联动
  - 环境趋势图已切到 `/api/sensor-data/trend`
- 静态验证已通过：
  - `backend/` 的 `mvn -q -DskipTests compile` 通过
  - `frontend/` 的 `npm run build` 通过

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

当前明确未完成：
1. `spring.sql.init.mode=always` 仍会在每次后端启动时重建演示库
2. 仓库管理 CRUD 还没补完整
3. 用户管理 CRUD 还没补完整
4. PowerShell 验收脚本还没补
5. `UsersView.vue` 右侧角色说明仍保留卡片说明区
6. `/screen` 大屏还没纳入这轮展示统一

当前下一步重点：
1. 先评估是否保留 `spring.sql.init.mode=always`，若不保留则收口本地初始化策略
2. 若继续补后台管理闭环，优先做仓库 CRUD，再做用户 CRUD
3. 若先补验收能力，则补一个 PowerShell 脚本串起：
   - 后端启动
   - 固定模板下载
   - 固定模板导入
   - 旧 CSV / 旧行式 Excel 导入
   - 温度 / 环境数据 CRUD smoke
   - 首页概览检查

当前本地环境注意事项：
- 当前后端联调端口统一使用：`8081`
- 当前前端默认 API 地址：`http://localhost:8081`
- 当前 `8080` 在本机会话里可能被旧 Java 进程占用，不要误判为后端代码故障
- 当前后端此刻通常是可用的，但恢复时仍应先确认 `8081` 是否已有 Java 进程占用
- 若旧文档与最新 handoff / state 冲突，以最新 handoff 和 `state.md` 为准

如果用户没有再次改变方向，就从“当前下一步重点”的第 1 条开始推进。
