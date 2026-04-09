# Handoff

## 基础信息

- 创建时间：2026-04-09
- mission：grain-platform-bootstrap
- 当前阶段：Pause-ready after init-strategy closure and warehouse CRUD
- handoff 编号：019
- 是否 superseded：否

## 当前目标

- 在“数据库优先 MVP”主线下继续补后台管理闭环。
- 目前优先级已切到：用户管理 CRUD。

## 当前进度

- 已完成初始化策略收口：
  - `backend/src/main/resources/application.yml` 已改为 `spring.sql.init.mode=never`
  - 新增显式重置脚本：`scripts/reset-demo-db.ps1`
  - 默认后端启动不再自动重建演示库
- 已完成仓库管理 CRUD：
  - 后端补齐 `PUT /api/warehouses/{id}`、`DELETE /api/warehouses/{id}`
  - 前端 `WarehouseView.vue` 补齐新增/编辑/删除交互
  - 仓库删除被引用时返回友好业务提示
- 已完成验证证据：
  - `backend/`：`mvn -q -DskipTests compile` 通过
  - `frontend/`：`npm run build` 通过
  - 运行态 smoke：仓库 create -> update -> delete 完整链路通过，临时数据已清理
- 当前会话里“用户 CRUD”尚未进入实现阶段：
  - 只完成了代码读取、边界确认和口径收敛
  - 尚未新增用户 DTO、接口、Mapper 写入和前端弹窗交互代码

## 本轮完成内容

- [x] 初始化策略从自动重建改为默认保留数据
- [x] 新增显式数据库重置脚本入口
- [x] 完成仓库管理 CRUD 后端与前端改造
- [x] 完成仓库 CRUD 静态与运行态验证
- [x] 更新 `devflow` 的状态、决策、checkpoint、计划索引
- [x] 生成本次 pause-ready handoff

## 关键决策与原因

| 决策 | 备选方案 | 原因 |
| --- | --- | --- |
| 默认不再 `spring.sql.init.mode=always` | 保持每次启动重建演示库 | 避免联调数据与验收样本被反复清空，保证后续 CRUD 与脚本验证稳定 |
| 初始化采用“显式重置脚本” | Spring profile 自动切换 | 当前本地联调以 PowerShell 为主，脚本入口更直接、可控 |
| 仓库删除保持硬删 + 引用拦截提示 | 直接改逻辑删除 | 当前范围内最小改动即可满足后台闭环，避免额外口径扩散 |
| 用户 CRUD 本轮不抢做 | 强行继续实现用户 CRUD | 用户明确要求先休息并生成交接，避免中断状态下继续实现引入半成品 |

## 关键文件 / 产物

| 文件 | 作用 | 相关性 |
| --- | --- | --- |
| `backend/src/main/resources/application.yml` | 默认 SQL 初始化策略改为 `never` | 最高 |
| `scripts/reset-demo-db.ps1` | 显式重置演示库入口 | 最高 |
| `backend/src/main/java/com/grain/platform/controller/WarehouseController.java` | 仓库 CRUD 接口补齐 | 高 |
| `backend/src/main/java/com/grain/platform/service/WarehouseService.java` | 仓库更新/删除业务与约束提示 | 高 |
| `backend/src/main/resources/mapper/WarehouseMapper.xml` | 仓库 update/delete SQL | 高 |
| `frontend/src/views/WarehouseView.vue` | 仓库管理页新增编辑删除交互 | 高 |
| `frontend/src/api/grain.js` | 仓库 update/delete API 封装 | 高 |
| `.devflow/grain-platform-bootstrap/state.md` | 最新任务状态真相源 | 最高 |
| `.devflow/grain-platform-bootstrap/checkpoints.md` | 最新里程碑记录 | 高 |
| `.devflow/grain-platform-bootstrap/decision-log.md` | 关键决策沉淀 | 高 |

## 风险 / 阻塞项 / 开放问题

- [ ] 用户管理 CRUD 仍未实现（当前仅列表/角色选项只读）。
- [ ] PowerShell 验收脚本仍未落地。
- [ ] `UsersView.vue` 右侧角色说明卡片仍保留当前展示形态（尚未统一为表格方案）。
- [ ] `/screen` 大屏尚未纳入本轮展示统一。

## 立即下一步

1. 进入 Apply，优先实现用户 CRUD（后端 + 前端）。
2. 用户 CRUD 通过静态与运行态验证后，补 PowerShell 验收脚本。
3. 再评估 `UsersView.vue` 角色说明区和 `/screen` 的展示统一收口。

## 恢复指引

1. 先读取根目录：`NEXT-SESSION-PROMPT-DEVFLOW.md`
2. 再读取：
   - `.codex/skills/devflow/SKILL.md`
   - `zzz-docs/任务书.md`
   - `zzz-docs/开题报告.md`
   - `.devflow/grain-platform-bootstrap/state.md`
   - `.devflow/grain-platform-bootstrap/handoffs/2026-04-09-019-pause-ready-after-init-and-warehouse-crud.md`
3. 按需读取：
   - `.devflow/grain-platform-bootstrap/plans/active-plan-links.md`
   - `docs/superpowers/plans/2026-04-09-init-strategy-closure.md`
   - `docs/superpowers/plans/2026-04-09-warehouse-crud.md`
4. 从“立即下一步”的第 1 条继续，不要重复做已完成的初始化收口和仓库 CRUD。

## 可从活跃上下文移除的内容

- 本轮用户 CRUD 讨论中多次中断的重复口径确认文本。
- 仓库 CRUD 运行态 smoke 第一次被旧进程干扰的中间排查输出细节。
