# Handoff

## 基础信息

- 创建时间：2026-04-07
- mission：grain-platform-bootstrap
- 当前阶段：Handoff / Ready for resume
- handoff 编号：009
- 是否 superseded：否

## 当前目标

- 保存“指标选项真实接口、多指标独立预测、仪表盘首页真实化、文档同步”这一轮的可恢复状态。
- 让下一次会话可以直接从“展示大屏进一步真实化 -> 用户管理正式操作接口评估”继续，而不需要重新梳理本轮联调成果。

## 当前进度

- 已完成预测页真实归档与用户页真实展示闭环。
- 已完成 `GET /api/metrics/options`，前端指标选项已切到真实接口。
- 已将预测主线从“仅温度预测”收口为“独立多指标预测”。
- 已将预测接口从 `POST /api/predictions/temperature` 收口为 `POST /api/predictions`。
- 已按指标阈值计算预测风险等级。
- 已补充湿度与二氧化碳浓度预测 mock 数据。
- 已完成仪表盘首页概览、近期预警、仓库健康度、最近采样记录的真实化。
- 已同步开发指导版 PRD、后端接口设计、数据库设计、README、`NEXT-SESSION-PROMPT.md` 等主真相源文档。

## 本轮完成内容

- [x] 新增 `GET /api/metrics/options`
- [x] 新增 `SensorMetricMapper / MetricService / MetricController`
- [x] 前端指标下拉切到真实接口
- [x] 环境数据页改为真实多指标选项
- [x] 预测页改为多指标独立预测
- [x] 预测接口收口为 `POST /api/predictions`
- [x] 预测返回补充 `metricCode / metricName / unit / maxThreshold`
- [x] 风险等级改为按指标阈值计算
- [x] 在 `schema.sql` 中补充湿度与二氧化碳浓度预测任务与结果数据
- [x] 仪表盘接入真实预警、仓库健康度、最近采样记录
- [x] 同步主真相源文档与恢复提示词
- [x] 完成后端编译与前端构建验证

## 关键验证与判断

1. 多指标独立预测当前已经形成可演示闭环：
   - 真实指标下拉
   - 选择仓库 + 指标 + 步数
   - 执行预测并归档
   - 读取真实历史归档
   - 点击历史记录回显图表与结果
2. 当前数据库表结构没有变更：
   - 继续使用 `sensor_metric`
   - 继续使用 `sensor_data`
   - 继续使用 `prediction_task + prediction_result`
   - 只是把原有 `metric_code` 的多指标能力真正用起来了
3. 仪表盘当前已从“真实概览 + 多块 mock”收口到“核心首页信息真实化”：
   - 首页统计卡真实化
   - 近期预警真实化
   - 仓库健康度真实化
   - 最近采样记录真实化
4. 展示大屏仍是“真实概览 + mock 底板”的混合状态，下一轮最适合继续推进这里。

## 关键决策与原因

| 决策 | 备选方案 | 原因 |
| --- | --- | --- |
| 预测主线改为“独立多指标预测” | 继续只做温度预测 / 直接做联合建模 | 既贴合任务书与开题报告中的多指标环境数据管理，又不扩展到复杂多变量建模 |
| 预测接口收口为 `POST /api/predictions` | 继续使用 `/api/predictions/temperature` | 当前实现已按 `metricCode` 通用化，接口路径也应同步语义收口 |
| 风险等级按指标阈值计算 | 继续沿用温度固定阈值 26/28 | 湿度、二氧化碳浓度与温度阈值不同，必须走指标字典阈值才能合理 |
| 当前不改数据库表结构 | 新增专门的多指标预测表或额外字段 | `metric_code` 已经足够表达多指标独立预测，改表收益低、联调风险高 |
| 仪表盘先补真实预警、健康度、最近记录 | 先做更复杂聚合与大屏重构 | 优先形成答辩首页可展示闭环，范围更稳 |

## 关键文件 / 产物

| 文件 | 作用 | 相关性 |
| --- | --- | --- |
| `backend/src/main/java/com/grain/platform/controller/MetricController.java` | 指标选项真实接口 | 最高 |
| `backend/src/main/java/com/grain/platform/service/MetricService.java` | 指标查询与阈值读取 | 最高 |
| `backend/src/main/java/com/grain/platform/mapper/SensorMetricMapper.java` | 指标字典查询 Mapper | 高 |
| `backend/src/main/resources/mapper/SensorMetricMapper.xml` | 指标字典 SQL | 高 |
| `backend/src/main/java/com/grain/platform/controller/PredictionController.java` | 通用多指标预测接口 | 最高 |
| `backend/src/main/java/com/grain/platform/service/PredictionService.java` | 多指标预测、风险等级、归档返回 | 最高 |
| `backend/src/main/java/com/grain/platform/dto/prediction/PredictionTaskResponse.java` | 预测返回扩充指标信息 | 高 |
| `backend/src/main/resources/db/schema.sql` | 多指标预测 mock 数据真相源 | 最高 |
| `backend/src/main/java/com/grain/platform/service/DashboardService.java` | 仪表盘真实统计组装 | 最高 |
| `backend/src/main/resources/mapper/DashboardMapper.xml` | 仪表盘真实查询 SQL | 最高 |
| `frontend/src/api/grain.js` | 前端 API 适配层，多指标与仪表盘真实化入口 | 最高 |
| `frontend/src/views/DataView.vue` | 环境数据页真实多指标下拉 | 高 |
| `frontend/src/views/PredictionView.vue` | 多指标独立预测页 | 最高 |
| `frontend/src/views/DashboardView.vue` | 仪表盘真实化展示 | 最高 |
| `zzz-docs/开发指导版PRD-粮仓环境数据预测管理平台.md` | 主 PRD 真相源已同步新口径 | 高 |
| `zzz-docs/设计文档/后端接口与DTO-VO-Mapper设计.md` | 接口设计真相源已同步新口径 | 高 |
| `zzz-docs/设计文档/数据库设计定稿.md` | 数据库设计真相源已同步新口径 | 高 |
| `NEXT-SESSION-PROMPT.md` | 下一次会话直接复制的恢复提示词 | 高 |

## 关键验证结果

- 后端编译通过：
  - `mvn -q -DskipTests compile --file "E:/Learn/Vs/Code/bs-test-01/backend/pom.xml"`
- 前端构建通过：
  - `npm run build --prefix "E:/Learn/Vs/Code/bs-test-01/frontend"`

## 风险 / 阻塞项 / 开放问题

- [ ] 旧 handoff / spec / 历史文档里仍残留部分旧的“温度预测”与旧接口路径描述，虽然不影响当前代码真相源，但后续可继续收口。
- [ ] 展示大屏当前仍是“真实概览 + mock 底板”的混合状态。
- [ ] 用户管理尚未补齐新增、编辑、状态切换等正式操作接口。
- [ ] 前端构建虽然通过，但仍有大 bundle 警告，当前不影响运行与答辩演示。

## 立即下一步

1. 先读取 `NEXT-SESSION-PROMPT.md`
2. 再读取本 handoff 与 `.devflow/grain-platform-bootstrap/state.md`
3. 继续推进展示大屏进一步真实化：
   - 将更多大屏指标切到真实接口
   - 评估是否复用仪表盘真实数据
   - 清理大屏中的 mock 提示与静态底板数据
4. 完成大屏后，再评估是否补用户新增、编辑、状态切换接口
5. 如时间允许，最后统一清理旧 handoff / spec 中的旧口径表述

## 环境注意事项

- 本地数据库：`grain_env_predict`
- 数据库用户：`root`
- 数据库密码：`123456`
- 当前后端联调端口：`8081`
- 当前前端默认 API 地址：`http://localhost:8081`
- 当前预测接口：`POST /api/predictions`
- 当前指标选项接口：`GET /api/metrics/options`
- 当前预测归档真实结构：`prediction_task + prediction_result`
- 当前数据库表结构未新增字段，仍沿用原有 `metric_code` 多指标能力

## 恢复指引

1. 先读取 `NEXT-SESSION-PROMPT.md`
2. 再读取 `.devflow/grain-platform-bootstrap/handoffs/index.md`
3. 再读取本 handoff
4. 再读取 `.devflow/grain-platform-bootstrap/state.md`
5. 必要时补读：
   - `frontend/src/views/PredictionView.vue`
   - `frontend/src/views/DashboardView.vue`
   - `frontend/src/views/BigScreenView.vue`
   - `frontend/src/api/grain.js`
   - `backend/src/main/java/com/grain/platform/service/PredictionService.java`
   - `backend/src/main/resources/mapper/DashboardMapper.xml`
6. 从“立即下一步”的第 3 条开始继续

## 可从活跃上下文移除的内容

- `session-handoff` 外部脚本因为 Windows 编码问题创建 handoff 失败的排查过程
- 关于“多指标预测是否要联合建模”的讨论过程
- 仪表盘 mock 清理过程中各字段映射的小范围试错细节
