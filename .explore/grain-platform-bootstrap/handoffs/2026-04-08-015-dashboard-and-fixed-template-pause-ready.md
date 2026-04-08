# Handoff

## 基础信息

- 创建时间：2026-04-08
- mission：grain-platform-bootstrap
- 当前阶段：Apply second round completed / Pause-ready
- handoff 编号：015
- 是否 superseded：否

## 当前目标

- 固化第二轮进展，确保下次恢复时不再重复核对首页改造、固定 XLSX 模板升级和运行态阻塞原因。
- 把恢复入口收敛到一个清晰起点：先处理 MySQL 认证问题，再补运行态 smoke。

## 当前进度

- 已完成首页仪表盘第二轮改造：
  - 后端 `dashboard` 聚合已切到真实预警 + 预测预警口径
  - 首页优先展示：
    - `grain_temp_summary.warning_*`
    - `prediction_result.warning_*`
  - `DashboardView.vue` 和 `BigScreenView.vue` 已同步新统计字段与展示文案
- 已完成粮温导入第二轮改造：
  - `/api/grain-temp/import/template` 已改为固定 `XLSX` 模板下载
  - `GrainTempImportService` 已支持固定模板的“基础信息 + 层号/点位矩阵”解析
  - 旧 CSV / 行式 Excel 兼容逻辑保留
- 已完成验证文档补充：
  - 新增 `zzz-docs/验证/数据库优先MVP-回归验证清单.md`
- 已完成静态验证：
  - `backend/`：`mvn -q -DskipTests compile` 通过
  - `frontend/`：`npm run build` 通过
- 运行态 smoke 已尝试但未完成：
  - `spring-boot:run` 启动失败
  - 已定位根因是本机 MySQL 认证失败，而不是本轮首页或固定模板代码直接报错

## 本轮完成内容

- [x] 改造后端首页聚合 SQL、DTO、Service 到预警优先口径
- [x] 改造前端首页与大屏展示到真实预警 / 预测预警口径
- [x] 升级粮温固定模板下载为 `XLSX`
- [x] 实现固定模板矩阵解析，并保留旧格式兼容
- [x] 补回归验证清单
- [x] 完成后端编译验证
- [x] 完成前端构建验证
- [x] 定位运行态 smoke 阻塞原因
- [x] 回写 `.explore` 状态并准备本 handoff

## 关键决策与原因

| 决策 | 备选方案 | 原因 |
| --- | --- | --- |
| 首页统计优先切到真实预警 + 预测预警 | 继续沿旧 `sensor_data` 统计口径 | 当前主线已明确以粮温汇总与预测归档为中心，旧口径会误导答辩展示 |
| 固定模板采用“兼容升级” | 直接删除旧 CSV / 行式导入 | 已跑通的数据库优先 MVP 不应因模板升级而回退 |
| 先在当前节点暂停，不继续处理 MySQL | 继续深挖本机数据库认证 | 用户要休息，当前最有价值的是把阻塞原因和恢复起点写清楚 |

## 关键文件 / 产物

| 文件 | 作用 | 相关性 |
| --- | --- | --- |
| `backend/src/main/resources/mapper/DashboardMapper.xml` | 首页新口径 SQL 聚合真相源 | 最高 |
| `backend/src/main/java/com/grain/platform/service/DashboardService.java` | 首页聚合服务 | 高 |
| `backend/src/main/java/com/grain/platform/service/GrainTempImportService.java` | 固定 XLSX 模板生成与矩阵解析核心服务 | 最高 |
| `backend/src/main/java/com/grain/platform/controller/GrainTempController.java` | 固定模板下载接口入口 | 高 |
| `frontend/src/views/DashboardView.vue` | 正式首页展示 | 高 |
| `frontend/src/views/BigScreenView.vue` | 答辩大屏统计同步 | 中 |
| `frontend/src/views/DataView.vue` | 固定模板导入说明入口 | 高 |
| `frontend/src/api/grain.js` | 首页与导入 API 适配层 | 高 |
| `zzz-docs/验证/数据库优先MVP-回归验证清单.md` | 本轮新增回归清单 | 最高 |
| `.explore/grain-platform-bootstrap/state.md` | 当前 mission 状态真相源 | 最高 |
| `NEXT-SESSION-PROMPT.md` | 下次恢复可直接复制的提示词 | 最高 |

## 关键验证结果

- 编译验证通过：
  - `backend/` 的 `mvn -q -DskipTests compile` 通过
- 构建验证通过：
  - `frontend/` 的 `npm run build` 通过
- 运行态阻塞已定位：
  - `mvn -q spring-boot:run -Dspring-boot.run.arguments=--server.port=8081` 启动失败
  - Spring Boot 在数据库初始化阶段报错：`Access denied for user 'root'@'localhost' (using password: YES)`
  - 结论：当前 smoke 阻塞点在本机 MySQL 认证，不在首页或固定模板解析逻辑

## 风险 / 阻塞项 / 开放问题

- [ ] 本机 MySQL 当前拒绝 `root/123456`，导致后端运行态 smoke 无法继续。
- [ ] 固定 XLSX 模板的下载 / 导入 / 汇总联动 smoke 仍未补完。
- [ ] 旧 CSV / 行式 Excel 的运行态兼容 smoke 仍未补完。
- [ ] 修正链字段仍只保留扩展能力，本期不做入口；这与当前范围一致，不视为阻塞。

## 立即下一步

1. 先检查并恢复本机 MySQL 可用凭据或 Spring Boot 本地连接配置。
2. 再按 `zzz-docs/验证/数据库优先MVP-回归验证清单.md` 依次补：
   - `/api/dashboard/overview` smoke
   - `/api/grain-temp/import/template` 下载 smoke
   - 固定 XLSX 导入 smoke
   - 旧 CSV / 行式 Excel 兼容 smoke
3. 若 smoke 补齐成功，再考虑补一个 PowerShell 脚本串起模板下载、导入和首页概览检查。

## 恢复指引

1. 先读取根目录 `NEXT-SESSION-PROMPT.md`
2. 再读取 `.explore/grain-platform-bootstrap/state.md`
3. 再读取本 handoff：`2026-04-08-015-dashboard-and-fixed-template-pause-ready.md`
4. 若旧 handoff、旧 prompt 与当前状态冲突，以本 handoff 和 `state.md` 为准
5. 从“立即下一步”的第 1 条继续，不要先做新功能扩展

## 可从活跃上下文移除的内容

- 首页旧口径 SQL 与新口径 SQL 的逐步替换过程
- 固定模板解析代码的中间改写过程
- 两轮 `spring-boot:run` 尝试与日志抓取命令细节
