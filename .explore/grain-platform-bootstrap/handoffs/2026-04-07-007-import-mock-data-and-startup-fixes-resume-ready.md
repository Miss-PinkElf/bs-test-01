# Handoff

## 基础信息

- 创建时间：2026-04-07
- mission：grain-platform-bootstrap
- 当前阶段：Handoff / Ready for resume
- handoff 编号：007
- 是否 superseded：否

## 当前目标

- 保存“环境数据导入、CSV 模板、弹窗录入、mock 数据补充、8081 收口、启动脚本端口清理修复”这一轮的可恢复状态。
- 让下一次会话可以直接从“前端页面联调验证 + 启动脚本补测 + 用户管理等剩余接口补齐”继续，而不需要重新排查本轮问题。

## 当前进度

- 已完成环境数据页的手工录入弹窗化。
- 已完成仓库管理页的新增仓库弹窗化。
- 已完成环境数据 CSV / Excel 导入接口与前端入口。
- 已完成 CSV 模板下载，并修复 Windows / Excel 打开时的中文乱码问题（BOM）。
- 已补充数据库 `schema.sql` 的 mock 数据，并已手动导入到本地 MySQL。
- 已将前端默认 API 地址从 `8080` 收口到 `8081`。
- 已将后端 `application.yml` 收口到 `8081` 与本地数据库配置。
- 已为 Windows / mac 后端启动脚本补充 8081 端口占用检查与自动清理。
- 已修复 Windows `start-backend.ps1` 中 PowerShell 只读变量 `$PID` 冲突问题，并实测脚本可正常释放 8081 后启动后端。
- 本轮已提交两个 commit：`1cf835e`、`1a30b6e`。

## 本轮完成内容

- [x] 扩充 `backend/src/main/resources/db/schema.sql` 的仓库、用户、环境数据、预测历史 mock 数据
- [x] 新增环境数据导入 DTO 与导入服务
- [x] 新增 `POST /api/sensor-data/import`
- [x] 新增 `GET /api/sensor-data/import/template`
- [x] 修复 CSV 模板乱码问题
- [x] 将 `frontend/src/views/DataView.vue` 改为“查询 + 弹窗录入 + 导入入口 + 模板下载”
- [x] 将 `frontend/src/views/WarehouseView.vue` 改为弹窗新增
- [x] 修复左侧菜单布局大面积空白问题
- [x] 将 `frontend/src/api/http.js` 默认地址收口到 `http://localhost:8081`
- [x] 将后端端口与数据库初始化配置收口到 `application.yml`
- [x] 手动执行 `schema.sql` 导入本地 MySQL
- [x] 验证本地数据数量：`warehouse=6`、`sensor_data=36`、`prediction_task=4`
- [x] 修复 Windows 启动脚本端口清理报错
- [x] 实测 Windows `start-backend.ps1` 可以关闭占用 8081 的旧 Java 进程并重新启动后端
- [x] 完成本轮代码提交

## 关键验证与判断

1. `schema.sql` 中的 mock 数据已经真实导入数据库，不再只是文件里有数据。
2. 本地 MySQL root 密码当前应以 `123456` 为准，而不是空密码。
3. `application.yml` 已明确：
   - 后端端口：`8081`
   - SQL 初始化文件：`classpath:db/schema.sql`
4. Windows CSV 模板乱码问题来自 Excel 对 UTF-8 CSV 的兼容性，当前通过 BOM 解决。
5. Windows `start-backend.ps1` 的原始报错根因是误用了 PowerShell 只读内置变量 `$PID`。
6. 后台验证任务最终显示 failed，不是因为脚本仍不可用，而是长时间运行的 `spring-boot:run` 任务在后台被终止；实际日志中已确认成功启动到 `Tomcat started on port 8081`。

## 关键决策与原因

| 决策 | 备选方案 | 原因 |
| --- | --- | --- |
| 前端手动录入先改“仓库 + 环境数据”两页弹窗 | 连预测页一起弹窗化 | 当前最直接影响录入体验的是这两页，范围更稳 |
| 导入模板优先提供 CSV | 先只做 Excel 模板 | CSV 成本更低、兼容更广，同时后端仍支持 Excel 导入 |
| mock 数据直接补进 `schema.sql` 并手动导入数据库 | 只保留页面 mock | 毕设联调和答辩演示都更依赖真实数据库有内容 |
| 启动脚本只自动清理 8081 | 同时自动杀 5173 | 避免误杀前端开发服务，范围更可控 |
| Windows 脚本提示文案临时收口为 ASCII | 继续保留中文提示 | 避免 PowerShell 编码和解析问题干扰启动验证 |

## 关键文件 / 产物

| 文件 | 作用 | 相关性 |
| --- | --- | --- |
| `backend/src/main/resources/db/schema.sql` | 本地数据库结构与演示数据真相源 | 最高 |
| `backend/src/main/resources/application.yml` | 后端端口、数据库、SQL 初始化配置 | 最高 |
| `backend/src/main/java/com/grain/platform/controller/SensorDataController.java` | 导入接口与模板下载接口 | 最高 |
| `backend/src/main/java/com/grain/platform/service/SensorDataService.java` | 环境数据录入与导入聚合服务 | 最高 |
| `backend/src/main/java/com/grain/platform/service/SensorDataImportService.java` | CSV / Excel 解析与模板生成 | 最高 |
| `backend/src/main/resources/mapper/SensorDataMapper.xml` | 环境数据批量写库 SQL | 高 |
| `frontend/src/views/DataView.vue` | 环境数据页弹窗、导入与模板下载入口 | 最高 |
| `frontend/src/views/WarehouseView.vue` | 仓库新增弹窗 | 高 |
| `frontend/src/layout/ConsoleLayout.vue` | 左侧菜单布局 | 高 |
| `frontend/src/styles.css` | 侧边栏与弹窗相关样式 | 高 |
| `frontend/src/api/http.js` | 前端默认 API 地址收口到 8081 | 高 |
| `scripts/start-backend.ps1` | Windows 后端启动与端口清理 | 最高 |
| `scripts/start-backend.sh` | mac 后端启动与端口清理 | 高 |
| `scripts/start-all.ps1` | Windows 一键启动提示更新 | 高 |
| `scripts/start-all.sh` | mac 一键启动提示更新 | 高 |

## 本轮提交记录

- `1cf835e` `完善环境数据导入与启动脚本收口`
- `1a30b6e` `修复 Windows 启动脚本端口清理报错`

## 风险 / 阻塞项 / 开放问题

- [ ] `start-all.ps1` 和 `start-all.sh` 还没有做一轮完整端到端补测
- [ ] 当前只实测了 Windows `start-backend.ps1`，mac 端口清理逻辑尚未实机验证
- [ ] 用户管理、角色选项、指标选项、预测历史等正式接口仍未补齐
- [ ] 仪表盘“健康度 / 最近采样 / 更多聚合”仍混合使用 mock 数据
- [ ] `start-backend.ps1` 当前提示文案为英文，为规避 PowerShell 编码问题；若后续要恢复中文，需顺手校验编码与执行环境
- [ ] 过程文档文件（`.explore/`、`NEXT-SESSION-PROMPT.md`）尚未提交到 git

## 立即下一步

1. 先验证前端页面是否已成功读到新导入的数据库 mock 数据：
   - 仪表盘
   - 仓库管理
   - 环境数据
   - 温度预测
2. 再补测：
   - Windows：`scripts/start-all.ps1`
   - mac：`scripts/start-backend.sh`、`scripts/start-all.sh`
3. 继续推进前端真实 API 联调与剩余正式接口：
   - 用户管理
   - 角色选项
   - 指标选项
   - 预测历史
4. 联调稳定后，再增强仪表盘聚合与演示流程

## 环境注意事项

- 本地数据库：`grain_env_predict`
- 数据库用户：`root`
- 数据库密码：`123456`
- 当前后端端口：`8081`
- 当前前端默认 API 地址：`http://localhost:8081`
- Windows 后端脚本会在启动前自动清理 `8081`
- CSV 模板已通过 UTF-8 BOM 解决 Excel 中文乱码问题

## 恢复指引

1. 先读取 `NEXT-SESSION-PROMPT.md`
2. 再读取 `.explore/grain-platform-bootstrap/handoffs/index.md`
3. 再读取本 handoff
4. 再读取 `.explore/grain-platform-bootstrap/state.md`
5. 必要时补读：
   - `backend/src/main/resources/application.yml`
   - `backend/src/main/resources/db/schema.sql`
   - `frontend/src/views/DataView.vue`
   - `scripts/start-backend.ps1`
6. 从“立即下一步”的第 1 条开始继续

## 可从活跃上下文移除的内容

- PowerShell `$PID` 报错的中间排查过程
- 第一次执行 `schema.sql` 因字符集失败的中间过程
- 后台 `spring-boot:run` 验证任务最终显示 failed 的长日志细节
