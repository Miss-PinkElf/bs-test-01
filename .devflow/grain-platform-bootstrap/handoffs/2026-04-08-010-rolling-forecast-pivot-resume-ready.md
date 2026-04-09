# Handoff

## 基础信息

- 创建时间：2026-04-08
- mission：grain-platform-bootstrap
- 当前阶段：Realign / Truth-source rewrite before major refactor
- handoff 编号：010
- 是否 superseded：否

## 当前目标

- 记录项目路线从“多指标独立短期预测”切换到“粮温滚动预测闭环”的阶段性变更。
- 让下一次会话不再默认沿旧路线继续做展示大屏和多指标预测补丁，而是先按新真相源评估重构边界。

## 当前进度

- 已完成与用户的长轮需求对齐。
- 已确认老师最新要求包括：
  - 不要只做 24 小时短期预测
  - 以前 8 个月真实数据作为训练参考
  - 预测后 2 个月每天数据
  - 新真实数据回填旧预测并验证误差
  - 对后续预测进行修正
  - 增加高温预警
  - 核心重点在数据库设计
- 已确认粮温 XLS 示例的核心结构是“上半部分测点温度矩阵 + 下半部分汇总分析”，第一版导入只解析上半部分，汇总由系统自动生成。

## 本轮完成内容

- [x] 新增 `zzz-docs/设计文档/粮仓环境数据预测管理平台-PRD-滚动预测闭环版.md`
- [x] 新增 `zzz-docs/设计文档/数据库设计-滚动预测与高温预警版.md`
- [x] 新增 `zzz-docs/设计文档/滚动预测改造-字段与接口变更方案.md`
- [x] 新增 `zzz-docs/设计文档/后端接口与DTO-VO-Mapper设计-滚动预测版.md`
- [x] 新增 `zzz-docs/设计文档/mock数据设计-滚动预测闭环版.md`
- [x] 更新 `zzz-docs/开发指导版PRD-粮仓环境数据预测管理平台.md` 顶部真相源说明与关键目标口径
- [x] 更新 `zzz-docs/设计文档/数据库设计定稿.md` 顶部版本说明
- [x] 更新 `zzz-docs/设计文档/后端接口与DTO-VO-Mapper设计.md` 顶部版本说明
- [x] 更新 `NEXT-SESSION-PROMPT.md`
- [x] 更新 `.devflow/grain-platform-bootstrap/` 下的 `state.md`、`decision-log.md`、`checkpoints.md`

## 新的真相源清单

优先级最高：

1. `zzz-docs/设计文档/粮仓环境数据预测管理平台-PRD-滚动预测闭环版.md`
2. `zzz-docs/设计文档/数据库设计-滚动预测与高温预警版.md`
3. `zzz-docs/设计文档/滚动预测改造-字段与接口变更方案.md`
4. `zzz-docs/设计文档/后端接口与DTO-VO-Mapper设计-滚动预测版.md`
5. `zzz-docs/设计文档/mock数据设计-滚动预测闭环版.md`
6. `.devflow/grain-platform-bootstrap/state.md`
7. 本 handoff

## 当前判断

1. 现有代码不是完全没用，但当前预测主线相关代码很可能需要明显改造：
   - `backend/src/main/resources/db/schema.sql`
   - `backend/src/main/java/com/grain/platform/service/PredictionService.java`
   - `backend/src/main/java/com/grain/platform/service/SensorDataImportService.java`
   - `frontend/src/views/PredictionView.vue`
   - `frontend/src/views/DataView.vue`
   - `frontend/src/api/grain.js`
2. 现有登录、仓库、用户、前端框架、启动脚本等基础设施大体可保留。
3. 后续更像是“以现有项目为基础做半重写”，而不是继续小步补丁式迭代。

## 立即下一步

1. 先读取新的五份真相源文档
2. 输出实施计划
3. 先改 `schema.sql` 和数据库结构
4. 再改后端接口与服务
5. 最后改前端预测页与导入页

## 可从活跃上下文移除的内容

- 关于“到底按月预测还是按天预测”的多轮来回讨论
- 关于“层温是新指标还是测点维度”的探索过程
- 关于是否继续优先做展示大屏真实化的旧默认路线
