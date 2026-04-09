# Handoff

## 基础信息

- 创建时间：2026-04-09
- mission：grain-platform-bootstrap
- 当前阶段：Apply frontend list unification + data pagination completed / Pause-ready
- handoff 编号：017
- 是否 superseded：否

## 当前目标

- 固化本轮“后台页列表统一 + 数据管理页后端分页”改造结果。
- 让下次恢复时可以直接从“初始化策略 / 仓库 CRUD / 用户 CRUD / 验收脚本”继续，而不再重复梳理前端展示统一与分页接口细节。

## 当前进度

- 已完成数据库优先 MVP 主线：
  - 粮温主线采用 `grain_temp_point + grain_temp_record + grain_temp_summary`
  - 预测归档继续使用 `prediction_task + prediction_result`
  - 湿度、二氧化碳继续使用 `sensor_data`
- 已完成运行态与静态主线验证：
  - `/api/dashboard/overview`
  - `/api/grain-temp/import/template`
  - 固定 XLSX 导入
  - 旧 CSV / 旧行式 `.xls` 兼容
  - `/api/predictions/tasks` 与 `/api/predictions/tasks/1`
  - `backend/` 编译通过
  - `frontend/` 构建通过
- 已完成数据主线 CRUD：
  - `grain_temp_record` 新增 / 编辑 / 删除
  - `sensor_data` 新增 / 编辑 / 删除
  - 温度原始记录变更后自动重算同仓同时间的 `grain_temp_summary` 与真实预警
- 已完成后台页列表展示统一第一轮：
  - `UsersView.vue`、`WarehouseView.vue`、`DataView.vue`、`PredictionView.vue` 已统一为 `Element Plus` 表格分页
  - `PredictionView.vue` 历史归档记录由卡片流改为表格分页
  - `DashboardView.vue` 仓库健康度改为表格分页
  - `DashboardView.vue` 近期预警改为 `el-scrollbar` + 下滑增量加载
- 已完成数据管理页后端分页适配：
  - `GET /api/grain-temp/records` 返回 `PageResult<GrainTempRecordItemDto>`
  - `GET /api/sensor-data` 返回 `PageResult<SensorDataPointDto>`
  - `DataView.vue` 的粮温原始记录表与环境数据表改为后端分页联动
  - 环境趋势图改为走 `/api/sensor-data/trend`，避免只渲染当前页数据

## 本轮完成内容

- [x] 新增前端组合式工具：`useClientPagination`
- [x] 新增前端组合式工具：`useIncrementalList`
- [x] 后台主要结构化列表统一为 `Element Plus` 表格 + 分页
- [x] 仪表盘近期预警改为滚动加载
- [x] 为 `grain_temp_record` 列表补后端分页
- [x] 为 `sensor_data` 列表补后端分页
- [x] 将 `DataView.vue` 的记录表切到后端分页
- [x] 将环境趋势图切到独立趋势接口
- [x] 完成 `backend/` 静态编译验证
- [x] 完成 `frontend/` 构建验证
- [x] 更新 `.explore` 状态、handoff 索引与恢复提示词

## 关键决策与原因

| 决策 | 备选方案 | 原因 |
| --- | --- | --- |
| 后台页结构化列表默认统一为 `el-table + el-pagination` | 保留部分卡片流 | 当前后台页主要是业务列表，统一表格更利于答辩演示、维护与后续 CRUD 扩展 |
| 仪表盘“近期预警”保留流式展示，但改为 `el-scrollbar` 增量加载 | 也改成普通表格 | 预警更像事件流，不必强行表格化；滚动加载更符合阅读场景 |
| `grain_temp_record` / `sensor_data` 改为后端分页 | 继续只做前端本地分页 | 这两张表最容易增长，先补后端分页更合理 |
| 环境趋势图继续走独立趋势接口，而不是直接复用分页列表 | 图表只渲染当前页数据 | 图表需要完整趋势，不应受当前页分页截断影响 |

## 关键文件 / 产物

| 文件 | 作用 | 相关性 |
| --- | --- | --- |
| `docs/superpowers/plans/2026-04-09-frontend-list-unification.md` | 本轮前端列表统一实施计划 | 最高 |
| `frontend/src/composables/useClientPagination.js` | 前端本地分页组合式工具 | 高 |
| `frontend/src/composables/useIncrementalList.js` | 前端滚动增量加载工具 | 高 |
| `frontend/src/views/DashboardView.vue` | 仪表盘列表统一与预警滚动加载 | 高 |
| `frontend/src/views/PredictionView.vue` | 预测页结果表与归档表统一 | 高 |
| `frontend/src/views/DataView.vue` | 数据管理页后端分页联动核心页面 | 最高 |
| `frontend/src/api/grain.js` | 前端分页接口封装与趋势接口封装 | 最高 |
| `backend/src/main/java/com/grain/platform/controller/GrainTempController.java` | 粮温记录分页接口入口 | 高 |
| `backend/src/main/java/com/grain/platform/controller/SensorDataController.java` | 环境数据分页接口入口 | 高 |
| `backend/src/main/java/com/grain/platform/service/GrainTempService.java` | 粮温记录分页服务 | 高 |
| `backend/src/main/java/com/grain/platform/service/SensorDataService.java` | 环境数据分页服务 | 高 |
| `backend/src/main/resources/mapper/GrainTempRecordMapper.xml` | 粮温记录分页 SQL | 高 |
| `backend/src/main/resources/mapper/SensorDataMapper.xml` | 环境数据分页 SQL | 高 |
| `.explore/grain-platform-bootstrap/state.md` | 当前 mission 状态真相源 | 最高 |
| `NEXT-SESSION-PROMPT.md` | 下次恢复可直接复制的提示词 | 最高 |

## 关键验证结果

- 静态验证通过：
  - `backend/`：`mvn -q -DskipTests compile`
  - `frontend/`：`npm run build`
- 当前前端行为已收口为：
  - 用户列表、仓库列表、预测结果、预测历史归档、仓库健康度、最新粮温汇总均为表格分页
  - 仪表盘近期预警为 `el-scrollbar` + 下滑增量加载
  - `DataView.vue` 的粮温原始记录与环境数据列表为后端分页
  - 环境趋势图不受当前页分页截断影响

## 风险 / 阻塞项 / 开放问题

- [ ] `spring.sql.init.mode=always` 仍会在每次后端启动时重建演示库，测试数据不能跨重启保留。
- [ ] 用户 / 仓库管理尚未补完整 CRUD。
- [ ] PowerShell 验收脚本仍未补。
- [ ] `UsersView.vue` 右侧角色说明仍保留卡片说明区，`/screen` 大屏未纳入本轮统一范围。
- [ ] 本轮未动修正链入口；这仍与当前数据库优先 MVP 范围一致。

## 立即下一步

1. 先决定是否保留 `spring.sql.init.mode=always`，若不保留则收口成本最低的一种本地初始化策略。
2. 若继续补后台管理闭环，优先做仓库 CRUD，再做用户 CRUD。
3. 若先补验收能力，则补一个 PowerShell 脚本，串起后端启动、固定模板下载、导入、CRUD smoke 和首页概览检查。
4. 如果还想继续收口前端展示，再决定是否把 `UsersView.vue` 的角色说明卡片也改成表格，以及是否把 `/screen` 纳入统一规范。

## 恢复指引

1. 先读取根目录 `NEXT-SESSION-PROMPT.md`
2. 再读取 `.explore/grain-platform-bootstrap/state.md`
3. 再读取本 handoff：`2026-04-09-017-frontend-list-unification-and-data-pagination-ready.md`
4. 按需读取：
   - `docs/superpowers/plans/2026-04-09-data-crud-and-temperature-recompute.md`
   - `docs/superpowers/plans/2026-04-09-frontend-list-unification.md`
5. 从“立即下一步”的第 1 条开始继续，不要再回到前端列表统一和数据页分页设计阶段

## 可从活跃上下文移除的内容

- 本轮前端样式统一时对每个页面逐个排查的过程
- `el-table` / `el-pagination` / `el-scrollbar` 组件替换时的中间 patch 细节
- 后端分页 SQL 与前端分页联调时的中间试错过程
