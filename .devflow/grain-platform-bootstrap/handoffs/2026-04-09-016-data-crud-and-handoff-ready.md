# Handoff

## 基础信息

- 创建时间：2026-04-09
- mission：grain-platform-bootstrap
- 当前阶段：Apply data CRUD completed / Pause-ready
- handoff 编号：016
- 是否 superseded：否

## 当前目标

- 固化本轮“数据主线 CRUD + 温度联动重算”的实现、验证和恢复入口。
- 让下次恢复时可以直接从“初始化策略 / 后台管理 CRUD / 验收脚本”三选一继续，而不再重复梳理已完成的数据主线工作。

## 当前进度

- 已完成数据库优先 MVP 主线：
  - 粮温主线采用 `grain_temp_point + grain_temp_record + grain_temp_summary`
  - 预测归档继续使用 `prediction_task + prediction_result`
  - 湿度、二氧化碳继续使用 `sensor_data`
- 已完成首页第二轮与固定模板升级：
  - 首页仪表盘聚合优先展示真实预警与预测预警
  - `DashboardView.vue` 与 `BigScreenView.vue` 已同步新口径
  - `/api/grain-temp/import/template` 已切到固定 `XLSX`
  - `GrainTempImportService` 已支持“基础信息 + 层号/点位矩阵”解析
  - 旧 CSV / 行式 Excel 兼容逻辑保留
- 已解除本机数据库阻塞：
  - 本机 MySQL `root` 密码已恢复为 `123456`
  - `backend/src/main/resources/application.yml` 默认配置可直接连通
- 已完成运行态 smoke：
  - `/api/dashboard/overview`
  - `/api/grain-temp/import/template`
  - 固定 XLSX 导入
  - 旧 CSV / 旧行式 `.xls` 兼容
  - `/api/predictions/tasks` 与 `/api/predictions/tasks/1`
- 已完成数据主线 CRUD 第二轮：
  - `grain_temp_record` 新增 / 编辑 / 删除
  - `sensor_data` 新增 / 编辑 / 删除
  - 温度原始记录变更后会自动重算同仓同时间的 `grain_temp_summary` 与真实预警
  - `frontend/src/views/DataView.vue` 已升级为统一数据维护页

## 本轮完成内容

- [x] 恢复本机 MySQL 凭据到 `root/123456`
- [x] 用默认 datasource 配置在 `8081` 成功启动后端
- [x] 补完数据库优先 MVP 的运行态 smoke
- [x] 新增数据主线 CRUD 实施计划：`docs/superpowers/plans/2026-04-09-data-crud-and-temperature-recompute.md`
- [x] 实现 `grain_temp_record` CRUD
- [x] 实现 `sensor_data` CRUD
- [x] 实现温度原始记录变更后的汇总与真实预警联动重算
- [x] 升级 `DataView.vue` 为粮温 / 普通环境统一维护页
- [x] 完成 `backend/` 编译验证
- [x] 完成 `frontend/` 构建验证
- [x] 回写 `.explore` 状态与本 handoff

## 关键决策与原因

| 决策 | 备选方案 | 原因 |
| --- | --- | --- |
| 温度 CRUD 作用于 `grain_temp_record` 原始记录，而不是 `grain_temp_summary` | 直接让用户编辑汇总表 | 汇总结果应由原始记录自动生成，手改汇总会破坏“原始数据 -> 汇总 -> 预警”的数据库主链 |
| 湿度 / 二氧化碳继续在 `sensor_data` 上做单值 CRUD | 也给普通环境数据加汇总层 | 当前 PRD 已明确普通环境数据保持简单单值模型，避免范围膨胀 |
| 温度变更后联动重算真实预警，但不覆盖历史预测归档 | 新真实数据一进来就重写 `prediction_task / prediction_result` | 预测归档需要保留当时的预测结果；真实数据变化只应影响真实汇总与真实预警 |
| 把 CRUD 实施计划显式回链到 `.explore` | 只把 plan 放在 `docs/` | 用户明确要求用 `devflow` 管理过程，mission 真相源必须能直接指到这份 plan |

## 关键文件 / 产物

| 文件 | 作用 | 相关性 |
| --- | --- | --- |
| `docs/superpowers/plans/2026-04-09-data-crud-and-temperature-recompute.md` | 本轮 CRUD 实施计划 | 最高 |
| `backend/src/main/java/com/grain/platform/service/GrainTempService.java` | 温度 CRUD 与汇总/真实预警联动重算核心服务 | 最高 |
| `backend/src/main/java/com/grain/platform/service/SensorDataService.java` | 普通环境数据 CRUD 核心服务 | 高 |
| `backend/src/main/java/com/grain/platform/controller/GrainTempController.java` | 粮温原始记录 CRUD 接口入口 | 高 |
| `backend/src/main/java/com/grain/platform/controller/SensorDataController.java` | 普通环境数据 CRUD 接口入口 | 高 |
| `backend/src/main/resources/mapper/GrainTempRecordMapper.xml` | 粮温原始记录 CRUD SQL | 高 |
| `backend/src/main/resources/mapper/GrainTempSummaryMapper.xml` | 温度联动重算后的汇总落库 / 删除 SQL | 高 |
| `backend/src/main/resources/mapper/SensorDataMapper.xml` | 普通环境数据 CRUD SQL | 高 |
| `frontend/src/views/DataView.vue` | 统一数据维护页 | 最高 |
| `frontend/src/api/grain.js` | CRUD API 封装与数据归一化 | 高 |
| `.devflow/grain-platform-bootstrap/state.md` | 当前 mission 状态真相源 | 最高 |
| `NEXT-SESSION-PROMPT.md` | 下次恢复可直接复制的提示词 | 最高 |

## 关键验证结果

- 静态验证通过：
  - `backend/`：`mvn -q -DskipTests compile`
  - `frontend/`：`npm run build`
- 运行态验证通过：
  - 温度原始记录 `POST /api/grain-temp/records` 成功新增记录
  - 温度原始记录 `PUT /api/grain-temp/records/{id}` 成功更新记录
  - 温度原始记录 `DELETE /api/grain-temp/records/{id}` 成功删除记录
  - 温度原始记录变更后，`/api/dashboard/overview` 的 `realAlertCount`、`latestAlerts`、`latestGrainSummaries`、`warehouseHealthList` 均发生联动变化
  - 普通环境数据 `POST /api/sensor-data`、`PUT /api/sensor-data/{id}`、`DELETE /api/sensor-data/{id}` 均成功
- 环境已回到干净种子态：
  - 当前 `grainSummaryCount = 27`
  - 一号仓湿度记录恢复为初始 1 条

## 风险 / 阻塞项 / 开放问题

- [ ] `spring.sql.init.mode=always` 仍会在每次后端启动时重建演示库，测试数据不能跨重启保留。
- [ ] 用户 / 仓库管理尚未补完整 CRUD。
- [ ] PowerShell 验收脚本仍未补。
- [ ] 本轮未动修正链入口；这仍与当前数据库优先 MVP 范围一致。

## 立即下一步

1. 先决定是否保留 `spring.sql.init.mode=always`，若不保留则收口成本最低的一种本地初始化策略。
2. 若继续补后台管理闭环，优先做仓库 CRUD，再做用户 CRUD。
3. 补一个 PowerShell 验收脚本，串起后端启动、固定模板下载、导入、CRUD smoke 和首页概览检查。

## 恢复指引

1. 先读取根目录 `NEXT-SESSION-PROMPT.md`
2. 再读取 `.devflow/grain-platform-bootstrap/state.md`
3. 再读取本 handoff：`2026-04-09-016-data-crud-and-handoff-ready.md`
4. 按需读取 `docs/superpowers/plans/2026-04-09-data-crud-and-temperature-recompute.md`
5. 从“立即下一步”的第 1 条开始继续，不要再回到 MySQL 凭据排查或数据主线 CRUD 设计阶段

## 可从活跃上下文移除的内容

- 温度 CRUD DTO / Mapper / Service 逐文件补齐时的中间 patch 过程
- MySQL 密码恢复、空密码兜底和多次 `spring-boot:run` 的试错过程
- 运行态 smoke 中用于验证联动的临时测试数据与删除回滚细节
