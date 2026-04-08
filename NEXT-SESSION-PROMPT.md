你现在在仓库 `D:\Users\Mobius\Desktop\mine\AAA-code\bs-test-01`，请继续这个毕业设计项目，并严格优先读取和遵守以下文档，然后再开始任何分析、启动验证或开发。

必须先读取：
1. `zzz-docs/任务书.md`
2. `zzz-docs/开题报告.md`
3. `.explore/grain-platform-bootstrap/state.md`
4. `.explore/grain-platform-bootstrap/handoffs/2026-04-08-014-db-first-mvp-apply-round1-pause-ready.md`
5. 再按需读取以下真相源文档：
   - `zzz-docs/设计文档/数据库设计-滚动预测与高温预警版.md`
   - `zzz-docs/设计文档/滚动预测改造-字段与接口变更方案.md`
   - `zzz-docs/设计文档/后端接口与DTO-VO-Mapper设计-滚动预测版.md`

当前唯一有效主线：
- 本期是“数据库优先 MVP”
- 不再把“预测 -> 修正 -> 再预测”作为本期必做链路
- 修正链字段可以保留，但本期不做修正接口和前端入口
- 不拆独立归档表，预测归档继续由 `prediction_task + prediction_result` 承担
- `frontend-next/` 只作为静态原型参考，不是正式实现目标

当前已经完成：
- 新 `backend/src/main/resources/db/schema.sql` 已真实导库验证通过
- 粮温主链表已跑通：
  - `grain_temp_point`
  - `grain_temp_record`
  - `grain_temp_summary`
- 预测归档表已跑通：
  - `prediction_task`
  - `prediction_result`
- 后端已完成第一轮数据库优先 MVP 改造：
  - 预测链切到 `grain_temp_summary + prediction_task + prediction_result`
  - 新增粮温接口：
    - `/api/grain-temp/import`
    - `/api/grain-temp/records`
    - `/api/grain-temp/summaries`
- 前端已完成第一轮改造：
  - `PredictionView.vue` 支持预测对象选择、预测天数、实际值/预测值双线图
  - `DataView.vue` 区分“粮温主线 / 普通环境数据”
  - `frontend/src/api/grain.js` 已适配新接口
- 已完成验证：
  - 前端 `npm run build` 通过
  - 后端可成功启动
  - 粮温汇总接口与预测接口烟雾测试通过
  - 烟雾测试已生成新预测任务归档：`prediction_task.id = 5`

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

当前未完成 / 下一步重点：
1. 先改仪表盘首页，让预警与统计优先展示：
   - `grain_temp_summary.warning_*`
   - `prediction_result.warning_*`
2. 再细化粮温导入，把当前“行式模板 MVP”逐步升级为更贴近老师预期的固定 XLS 模板
3. 最后补回归验证清单，必要时补脚本化验收

当前本地环境注意事项：
- 当前后端联调端口统一使用：`8081`
- 当前前端默认 API 地址：`http://localhost:8081`
- 当前 `8080` 在本机会话里可能被旧 Java 进程占用，不要误判为后端代码故障
- 若文档与最新 handoff / state 冲突，以 handoff 和 state 为准

如果用户没有再次改变方向，就从“下一步重点”的第 1 条开始推进。
