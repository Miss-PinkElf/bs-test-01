# Handoff

## 基础信息

- 创建时间：2026-04-06
- mission：grain-platform-bootstrap
- 当前阶段：Handoff / Ready for resume
- handoff 编号：003
- 是否 superseded：否

## 当前目标

- 保留“数据库定稿 + 后端接口设计 + 正式 Vue 前端骨架升级完成”的可恢复状态。
- 让下一次会话可以直接进入后端真实持久层替换和前后端联调，而不需要重新梳理本轮成果。

## 当前进度

- 已完成数据库 SQL 定稿与表说明。
- 已完成后端接口清单、DTO/VO、Mapper 设计文档。
- 已完成 `frontend/` 正式技术路线升级，切换到 `Vue Router + Pinia + Element Plus + Axios + ECharts`。
- 已补齐登录、仪表盘、用户管理、仓库管理、环境数据、预测页、展示大屏页面入口。
- 已在 `frontend/` 执行 `npm install` 与 `npm run build`，前端构建通过。
- 后端当前仍以 `DemoDataService` 为主，尚未按数据库定稿接入真实 MyBatis 持久层。

## 本轮完成内容

- [x] 输出数据库定稿文档与正式版 `schema.sql`
- [x] 输出后端接口与 DTO/VO/Mapper 设计文档
- [x] 将预测归档结构从单表收敛为 `prediction_task + prediction_result`
- [x] 将 `frontend/` 升级为正式版 Vue 技术栈骨架
- [x] 新增正式版布局、登录态 store、展示大屏和用户管理页
- [x] 完成前端依赖安装与构建验证
- [x] 同步更新 `.explore/grain-platform-bootstrap` 下的 state / decision-log / checkpoints / spec

## 关键决策与原因

| 决策 | 备选方案 | 原因 |
| --- | --- | --- |
| 预测归档采用 `prediction_task + prediction_result` 双表 | 继续使用单表 `prediction_record` | 更适合表达“一次任务 + 多个预测点”，便于预测历史、详情和归档展示 |
| 正式前端先完成技术路线升级与页面骨架 | 等后端真实落库后再升级前端 | 先把正式栈和页面信息架构稳定下来，能更快形成可演示前端外壳 |
| 前端 API 层先做字段归一化兼容 | 强行要求后端先同步重构所有返回结构 | 后端当前仍是 demo 接口，前端适配层能减少联调阻塞 |
| 后端设计文档统一沉淀到 `zzz-docs/设计文档/` | 分散写到 README 或 `.explore/` | 用户明确要求后端数据设计和接口设计都沉淀到该目录，便于集中查阅 |

## 关键文件 / 产物

| 文件 | 作用 | 相关性 |
| --- | --- | --- |
| [开发指导版PRD-粮仓环境数据预测管理平台.md](D:\Users\Mobius\Desktop\mine\AAA-code\bs-test-01\zzz-docs\开发指导版PRD-粮仓环境数据预测管理平台.md) | 后续开发总蓝图 | 最高 |
| [数据库设计定稿.md](D:\Users\Mobius\Desktop\mine\AAA-code\bs-test-01\zzz-docs\设计文档\数据库设计定稿.md) | 正式数据库真相源 | 最高 |
| [后端接口与DTO-VO-Mapper设计.md](D:\Users\Mobius\Desktop\mine\AAA-code\bs-test-01\zzz-docs\设计文档\后端接口与DTO-VO-Mapper设计.md) | 正式接口与后端分层设计真相源 | 最高 |
| [schema.sql](D:\Users\Mobius\Desktop\mine\AAA-code\bs-test-01\backend\src\main\resources\db\schema.sql) | 数据库正式 SQL | 最高 |
| [design.md](D:\Users\Mobius\Desktop\mine\AAA-code\bs-test-01\.explore\grain-platform-bootstrap\spec\design.md) | 当前 mission 的结构与接口摘要 | 高 |
| [tasks.md](D:\Users\Mobius\Desktop\mine\AAA-code\bs-test-01\.explore\grain-platform-bootstrap\spec\tasks.md) | 当前 mission 的任务状态 | 高 |
| [frontend-next/README.md](D:\Users\Mobius\Desktop\mine\AAA-code\bs-test-01\frontend-next\README.md) | 静态原型定位说明，避免误把 Next 原型当正式前端 | 高 |
| [ConsoleLayout.vue](D:\Users\Mobius\Desktop\mine\AAA-code\bs-test-01\frontend\src\layout\ConsoleLayout.vue) | 正式前端后台布局入口 | 高 |
| [grain.js](D:\Users\Mobius\Desktop\mine\AAA-code\bs-test-01\frontend\src\api\grain.js) | 前端 API 适配层，兼容当前 demo 后端返回结构 | 高 |

## 风险 / 阻塞项 / 开放问题

- [ ] 本机仍未安装 Maven，后端命令行开发和验证仍受限
- [ ] 后端仍依赖 `DemoDataService`，真实 MyBatis 持久层尚未打通
- [ ] 用户管理、角色选项、指标选项、预测历史等正式接口虽已设计，但还未真实落库
- [ ] 前端构建已通过，但仍存在 Vite 大包体积告警，当前不阻塞 MVP
- [ ] `frontend-next/` 仍需保持“静态原型参考”定位，不能反客为主变成正式实现路线

## 立即下一步

1. 先读取 `zzz-docs/开发指导版PRD-粮仓环境数据预测管理平台.md`、`zzz-docs/设计文档/数据库设计定稿.md`、`zzz-docs/设计文档/后端接口与DTO-VO-Mapper设计.md`
2. 检查 `backend/` 当前 `controller`、`service`、`mapper` 与 `DemoDataService` 的现状，按数据库定稿替换真实持久层
3. 优先打通登录、仓库、环境数据、预测归档四条后端主链路
4. 完成后再把前端用户管理、角色选项、预测历史等页面从静态/兼容态切到真实 API
5. 重新做前后端联调与验证记录

## 恢复指引

1. 先读取 `.explore/grain-platform-bootstrap/handoffs/index.md`
2. 再读取本 handoff
3. 然后读取 `.explore/grain-platform-bootstrap/state.md`
4. 必要时读取 `.explore/grain-platform-bootstrap/spec/design.md` 与 `.explore/grain-platform-bootstrap/spec/tasks.md`
5. 再读取 `zzz-docs` 下的数据库和后端接口设计文档
6. 从“立即下一步”的第 1 条开始继续

## 可从活跃上下文移除的内容

- 数据库双表方案和单表方案的比较推导过程
- 前端从 `fetch` 切到 `axios`、从自写布局切到 Element Plus 布局时的过程性讨论
- `frontend/` 依赖安装和首次构建输出细节
