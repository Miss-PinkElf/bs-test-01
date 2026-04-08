# Handoff

## 基础信息

- 创建时间：2026-04-08
- mission：grain-platform-bootstrap
- 当前阶段：Apply first round completed / Pause-ready
- handoff 编号：014
- 是否 superseded：否

## 当前目标

- 固化“数据库优先 MVP”第一轮实现成果，确保下次恢复时无需重新排查数据库和首轮改造状态。
- 为下一轮工作明确一个更短的切入点：先补首页仪表盘口径，再细化固定模板粮温导入。

## 当前进度

- 已完成数据库优先 MVP 第一轮主链：
  - 新 `schema.sql` 已真实导库验证通过
  - 粮温主链表已就位：`grain_temp_point` / `grain_temp_record` / `grain_temp_summary`
  - 预测归档表继续使用：`prediction_task` / `prediction_result`
  - 湿度、二氧化碳继续使用：`sensor_data`
- 已完成后端第一轮改造：
  - 预测接口改为按天预测
  - 预测结果落库到 `prediction_task + prediction_result`
  - 新增粮温接口：
    - `/api/grain-temp/import`
    - `/api/grain-temp/records`
    - `/api/grain-temp/summaries`
- 已完成前端第一轮改造：
  - `PredictionView.vue` 支持预测对象选择、可选预测天数、实际值/预测值双线图
  - `DataView.vue` 区分“粮温主线 / 普通环境数据”
  - `frontend/src/api/grain.js` 已适配新主线
- 已完成验证：
  - 前端 `npm run build` 通过
  - 后端本地启动成功
  - 粮温汇总接口与预测接口烟雾测试通过
  - 烟雾测试已生成新预测任务归档：`prediction_task.id = 5`

## 本轮完成内容

- [x] 确认可用的本地数据库连接并完成新 schema 真实导入
- [x] 按新表结构改造后端实体、Mapper、Service、Controller
- [x] 新增粮温导入/查询接口
- [x] 改造前端预测页与数据页/API 适配层
- [x] 完成前端构建验证
- [x] 完成后端本地启动与接口烟雾测试
- [x] 更新 `.gitignore`，忽略本地 Maven 仓库与本地 settings 文件
- [x] 回写 `.explore` 状态记录并创建本 handoff

## 关键决策与原因

| 决策 | 备选方案 | 原因 |
| --- | --- | --- |
| 粮温导入本轮先用“行式模板”完成 MVP | 直接实现复杂矩阵式固定 XLS 模板 | 先保证导入、入库、汇总、预测、归档整条链路跑通，降低本轮风险 |
| 预测页本轮固定沿温度主线推进 | 重新扩回多指标独立预测页面 | 避免滑回旧路线，保持与“数据库优先 MVP”口径一致 |
| 先补 `.gitignore` 忽略本地 Maven 产物 | 保留本地仓库与本地 settings 进入工作区 | 减少脏文件噪音，避免误提交本机专用文件 |

## 关键文件 / 产物

| 文件 | 作用 | 相关性 |
| --- | --- | --- |
| `backend/src/main/resources/db/schema.sql` | 当前数据库真相源 | 最高 |
| `backend/src/main/resources/application.yml` | 当前本地后端数据库连接配置 | 最高 |
| `backend/src/main/java/com/grain/platform/service/PredictionService.java` | 当前预测主链核心服务 | 最高 |
| `backend/src/main/java/com/grain/platform/service/GrainTempImportService.java` | 粮温导入 MVP 服务 | 最高 |
| `backend/src/main/java/com/grain/platform/controller/GrainTempController.java` | 粮温导入/查询接口入口 | 高 |
| `frontend/src/views/PredictionView.vue` | 当前预测页正式实现 | 高 |
| `frontend/src/views/DataView.vue` | 当前数据页正式实现 | 高 |
| `frontend/src/api/grain.js` | 前端 API 适配层 | 高 |
| `.explore/grain-platform-bootstrap/state.md` | 当前 mission 状态真相源 | 最高 |
| `NEXT-SESSION-PROMPT.md` | 下次恢复可直接复制的提示词 | 最高 |

## 关键验证结果

- 数据库验证通过：
  - 新 `schema.sql` 已成功导入
  - 重点表创建成功
- 前端验证通过：
  - `frontend/` 的 `npm run build` 已通过
- 后端验证通过：
  - 本地启动成功
  - `GET /api/grain-temp/summaries?warehouseId=1` 返回真实汇总数据
  - `POST /api/predictions` 成功创建预测任务并落库
- 落库验证通过：
  - 新预测任务已写入 `prediction_task`
  - 新预测结果已写入 `prediction_result`

## 风险 / 阻塞项 / 开放问题

- [ ] 粮温导入当前仍是数据库优先 MVP 的“行式模板”，尚未升级为更贴近老师预期的复杂矩阵式固定 XLS 模板。
- [ ] 仪表盘首页仍需切到“真实高温预警 + 预测高温预警”的新主线展示口径。
- [ ] 修正链字段已保留，但本期仍未做修正入口与修正页面；这与当前范围收口一致，不视为阻塞。

## 立即下一步

1. 先改仪表盘首页，让预警与统计优先展示：
   - `grain_temp_summary.warning_*`
   - `prediction_result.warning_*`
2. 再细化粮温导入，把当前行式模板逐步升级为更贴近老师预期的固定 XLS 模板。
3. 最后补回归验证清单，必要时补充脚本化验收。

## 恢复指引

1. 先读取根目录 `NEXT-SESSION-PROMPT.md`
2. 再读取 `.explore/grain-platform-bootstrap/state.md`
3. 再读取本 handoff：`2026-04-08-014-db-first-mvp-apply-round1-pause-ready.md`
4. 若旧文档、旧 handoff 与本 handoff 冲突，以本 handoff 和 `state.md` 为准
5. 从“立即下一步”的第 1 条继续

## 可从活跃上下文移除的内容

- MySQL 凭据排查过程
- Maven 本地仓库权限绕过过程
- MyBatis XML 转义错误的逐步调试过程
- 本轮构建与烟雾测试的中间日志
