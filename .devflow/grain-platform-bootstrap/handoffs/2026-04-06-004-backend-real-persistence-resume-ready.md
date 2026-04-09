# Handoff

## 基础信息

- 创建时间：2026-04-06
- mission：grain-platform-bootstrap
- 当前阶段：Handoff / Ready for resume
- handoff 编号：004
- 是否 superseded：否

## 当前目标

- 保存“后端主链路已切到真实 MyBatis + MySQL 持久层，并完成最小验证”的可恢复状态。
- 让下一次会话可以直接进入 `frontend/` 与真实 API 的联调，而不需要重新梳理本轮后端改造。

## 当前进度

- 已把 `backend/` 数据库连接切到本地 `grain_env_predict`。
- 已将 `schema.sql` 中默认数据库切到 `grain_env_predict`。
- 已补齐 `ApiResponse`、`PageResult`、`IdVO`、全局异常处理等基础层。
- 已新增并接入真实 MyBatis Mapper 与 XML。
- 已完成登录、仓库、环境数据、预测归档四条主链路从 `DemoDataService` 到真实持久层的替换。
- 已把 `DemoDataService` 收口为非主链路占位类，避免继续污染真实流程。
- 已开始把前后端字段命名从旧 `metricType` 向正式 `metricCode` 收口。
- 已在 `8081` 端口完成后端最小联调验证。

## 本轮完成内容

- [x] 后端数据库配置切换到 `grain_env_predict`
- [x] 新增后端公共返回结构和异常处理
- [x] 接入真实 `UserMapper / RoleMapper / AuthViewMapper`
- [x] 接入真实 `WarehouseMapper`
- [x] 接入真实 `SensorDataMapper`
- [x] 接入真实 `PredictionTaskMapper / PredictionResultMapper`
- [x] 改造 `AuthController / WarehouseController / SensorDataController / PredictionController`
- [x] 改造 `DashboardController` 为最小真实统计版本
- [x] 修复 entity / DTO 与 MyBatis 映射方式不兼容的问题
- [x] 后端 `mvn -q -DskipTests compile` 编译通过
- [x] 完成最小接口验证

## 关键验证结果

本轮在 `http://localhost:8081` 下实测通过：

1. `POST /api/auth/login`
   - 使用 `admin / 123456` 登录成功
2. `GET /api/warehouses`
   - 成功返回真实仓库列表
3. `POST /api/sensor-data`
   - 成功写入环境数据，返回新增记录 id
4. `POST /api/predictions/temperature`
   - 成功读取历史数据、执行预测、写入 `prediction_task` 和 `prediction_result`，并返回预测结果

## 关键决策与原因

| 决策 | 备选方案 | 原因 |
| --- | --- | --- |
| 先打通后端四条主链路，再做前端联调 | 先扩用户管理、预测历史等接口 | 先保证主业务闭环，最符合毕设 MVP 优先级 |
| 临时在 `8081` 验证后端 | 强行处理 `8080` 旧进程 | 当前 `8080` 被旧 Java 进程占用，直接切 `8081` 更稳，不打断已有会话 |
| entity 从 record 收口为普通 Java 类 | 继续保留 record | 当前 MyBatis 主键回填与 setter 映射更适合普通类，能减少联调故障 |
| DTO / 请求命名向 `metricCode` 收口 | 继续扩散旧 `metricType` | 与数据库定稿和接口设计一致，后续联调成本更低 |

## 关键文件 / 产物

| 文件 | 作用 | 相关性 |
| --- | --- | --- |
| `backend/src/main/resources/application.yml` | 本地数据库连接配置 | 最高 |
| `backend/src/main/resources/db/schema.sql` | 本地数据库初始化脚本 | 最高 |
| `backend/src/main/java/com/grain/platform/common/ApiResponse.java` | 统一返回结构 | 高 |
| `backend/src/main/java/com/grain/platform/common/GlobalExceptionHandler.java` | 全局异常处理 | 高 |
| `backend/src/main/java/com/grain/platform/service/AuthService.java` | 真实登录服务 | 高 |
| `backend/src/main/java/com/grain/platform/service/WarehouseService.java` | 真实仓库服务 | 高 |
| `backend/src/main/java/com/grain/platform/service/SensorDataService.java` | 真实环境数据服务 | 高 |
| `backend/src/main/java/com/grain/platform/service/PredictionService.java` | 真实预测归档服务 | 最高 |
| `backend/src/main/resources/mapper/*.xml` | MyBatis SQL 真相源 | 最高 |
| `frontend/src/api/grain.js` | 前端 API 适配层，已开始向真实接口字段收口 | 高 |
| `NEXT-SESSION-PROMPT.md` | 下次会话直接复制的恢复提示词 | 高 |

## 风险 / 阻塞项 / 开放问题

- [ ] `frontend/` 还没有完整切到真实 API，目前只是部分字段和提示语已收口
- [ ] 用户管理、角色选项、指标选项、预测历史等正式接口还没接真实持久层
- [ ] `8080` 当前被旧 Java 进程占用，所以下次若要本地跑通后端，要么先清理旧进程，要么继续临时用 `8081`
- [ ] 仪表盘目前只接了最小真实统计，健康度、最近采样、更多聚合仍待后续增强

## 立即下一步

1. 先读取 `NEXT-SESSION-PROMPT.md`
2. 再读取本 handoff 与 `.devflow/grain-platform-bootstrap/state.md`
3. 进入 `frontend/` 联调：
   - 登录页
   - 仓库页
   - 环境数据页
   - 预测页
4. 再补用户管理、角色选项、指标选项、预测历史等接口
5. 最后收口仪表盘增强和端口统一

## 环境注意事项

- 本地数据库：`grain_env_predict`
- 数据库用户：`root`
- 数据库密码：空
- 当前验证端口：`8081`
- 若继续用 `8081` 联调，前端需要把 `VITE_API_BASE` 指到 `http://localhost:8081`

## 恢复指引

1. 先读取 `NEXT-SESSION-PROMPT.md`
2. 再读取 `.devflow/grain-platform-bootstrap/handoffs/index.md`
3. 再读取本 handoff
4. 再读取 `.devflow/grain-platform-bootstrap/state.md`
5. 必要时补读：
   - `zzz-docs/开发指导版PRD-粮仓环境数据预测管理平台.md`
   - `zzz-docs/设计文档/数据库设计定稿.md`
   - `zzz-docs/设计文档/后端接口与DTO-VO-Mapper设计.md`
6. 从“立即下一步”的第 3 条开始继续

## 可从活跃上下文移除的内容

- entity 用 record 还是普通类的来回试错过程
- 预测接口 500 的中间排查细节
- 8080 / 8081 切换过程中的临时后台任务失败通知
