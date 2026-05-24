# Handoff

## 基础信息

- 创建时间：2026-05-24
- mission：grain-platform-bootstrap
- 当前阶段：Pause-ready after 旧库预警回写工具与 schema 重置口径收口
- handoff 编号：031
- 是否 superseded：否

## 当前目标

- 收口这次首页 / 环境数据页粮温口径修复的“历史数据与重置库”尾巴，避免出现：
  - 页面读取层已经按新规则显示，但数据库里旧 `grain_temp_summary` 仍保留旧预警字段；
  - `schema.sql` 重置演示库后，又重新生成带旧口径的 demo 数据。

## 当前进度

- 已完成“运行时显示口径修复”后的第二轮收尾：
  - 已补旧库批量回写工具；
  - 已补纯 SQL 版本；
  - 已同步收口 `schema.sql` 中与本次问题直接相关的演示数据口径；
  - 已更新 devflow 计划与 `state.md`。
- 当前真相源继续为 `.devflow/grain-platform-bootstrap/`。

## 本轮完成内容

- [x] 新增旧库批量回写脚本：
  - `scripts/rebuild-grain-temp-summary-warning.ps1`
- [x] 新增旧库批量回写 SQL：
  - `scripts/rebuild-grain-temp-summary-warning.sql`
- [x] 回写规则统一为：
  - `max_temp >= 28.00` -> `WARNING`
  - `25.00 <= max_temp < 28.00` -> `ATTENTION`
  - `max_temp < 25.00` -> `NORMAL`
- [x] 回写字段包含：
  - `warning_level`
  - `warning_flag`
  - `warning_message`
  - `analysis_result`
  - `analysis_remark`
- [x] 脚本已做预览 + 二次确认：
  - 先展示不符合新规则的汇总行
  - 用户输入 `y` 后再执行批量更新
- [x] 修改 `backend/src/main/resources/db/schema.sql`
  - `grain_temp_summary.analysis_result` 不再保留旧 demo 的 `维护观察` 特判
  - `grain_temp_summary.analysis_remark` 改为按统一温度规则生成
  - `prediction_task.forecast_start_time / forecast_end_time / based_on_actual_end_time` 统一改到 `08:40:00`
  - `prediction_result.result_time` 统一改到 `08:40:00`
  - `prediction_task` 与 `prediction_result` 中低于 `25°C` 的对比仓 demo 样例改回 `NORMAL`
- [x] 更新 devflow 记录：
  - `.devflow/grain-platform-bootstrap/plans/2026-05-24-dashboard-health-and-warning-threshold-fix.md`
  - `.devflow/grain-platform-bootstrap/state.md`
  - `NEXT-SESSION-PROMPT-DEVFLOW.md`

## 关键决策与原因

| 决策 | 备选方案 | 原因 |
| --- | --- | --- |
| 既补“旧库回写工具”，也改 `schema.sql` | 只做其中一个 | 用户既可能保留当前库继续用，也可能直接重置演示库，两条路都要收口 |
| 回写 `analysis_result` / `analysis_remark` | 只回写 `warning_*` 字段 | 用户已确认“改呗”，统一分析结果能减少数据库里旧口径残留 |
| `schema.sql` 里的 demo 预测时间统一改为 `08:40:00` | 保留 `00:00:00` | 与真实粮温采样时刻对齐，减少预测页“同一天两种时间点”造成的视觉割裂 |
| 对比仓 `23.28` / `23.46` demo 预测样例改回 `NORMAL` | 保留旧 `ATTENTION` 演示样例 | 本轮真实预警规则已经统一为 `< 25°C` 不预警，重置库后不应再复活旧样例 |

## 关键文件 / 产物

| 文件 | 作用 | 相关性 |
| --- | --- | --- |
| `scripts/rebuild-grain-temp-summary-warning.ps1` | 保留当前库时，一次性修正旧 `grain_temp_summary` 预警与分析字段 | 本轮新增核心工具 |
| `scripts/rebuild-grain-temp-summary-warning.sql` | 在 MySQL 中手工执行的等价回写 SQL | 本轮新增核心工具 |
| `backend/src/main/resources/db/schema.sql` | 重置演示库时的新统一口径真相源 | 本轮新增核心收口 |
| `.devflow/grain-platform-bootstrap/plans/2026-05-24-dashboard-health-and-warning-threshold-fix.md` | 记录本轮首页口径修复与后续补充收口 | 本轮计划真相源 |
| `.devflow/grain-platform-bootstrap/state.md` | 当前状态、开放问题、下一步 | 恢复入口 |
| `NEXT-SESSION-PROMPT-DEVFLOW.md` | 下次继续的直接复制提示词 | 恢复入口 |

## 如何使用

### 场景一：保留当前数据库，只修旧汇总数据

- 运行：
  - `.\scripts\rebuild-grain-temp-summary-warning.ps1`
- 或在 MySQL 中执行：
  - `scripts/rebuild-grain-temp-summary-warning.sql`

### 场景二：整库重置为新演示数据

- 运行：
  - `.\scripts\reset-demo-db.ps1`

## 验证状态

- 本轮未新增编译或构建验证；本次收尾以脚本、SQL、种子数据口径同步为主。
- 上一轮已通过：
  - `backend/`：`mvn -q -DskipTests compile`
  - `frontend/`：`npm run build`

## 风险 / 阻塞项 / 开放问题

- [ ] 旧库回写工具还没有在用户本地数据库上实际执行。
- [ ] `scripts/reset-demo-db.ps1` 还没有在本轮新 `schema.sql` 上再跑一次运行态确认。
- [ ] 首页与环境数据页人工复测尚未完成，仍需要重启本地 `8081` 后端后查看页面。
- [ ] 预测页人工复测尚未完成，需确认：
  - 预测开始时间页面交互
  - 历史任务独立保存
  - `08:40:00` 对齐后的同日真实值 / 预测值展示
- [ ] 系统管理员角色页面人工复测尚未做。
- [ ] 普通环境模板页面完整链路实测尚未做。
- [ ] 粮温并发导入 deadlock 第二轮止血代码仍待真实多 Excel 场景最终确认。
- [ ] 验收脚本尚未补 `keyword` 分页与粮温筛选条件的专门断言。

## 立即下一步

1. 先决定验证路径：
   - 如果保留当前库：运行 `scripts/rebuild-grain-temp-summary-warning.ps1`
   - 如果整库重置：运行 `scripts/reset-demo-db.ps1`
2. 重启本地 `8081` 后端。
3. 打开 `/environment`：
   - 检查 `23.x°C` 是否为 `NORMAL`
   - 检查 `25°C ~ 28°C` 是否为 `ATTENTION`
4. 打开 `/dashboard`：
   - 检查健康度「均温 / 峰值」是否已为历史口径
5. 如继续联调预测页：
   - 再验证预测开始时间、历史任务独立保存、`08:40:00` 对齐效果

## 恢复指引

1. 先读取 `.devflow/grain-platform-bootstrap/handoffs/index.md`
2. 再读取本 handoff：
   - `.devflow/grain-platform-bootstrap/handoffs/2026-05-24-031-pause-ready-after-warning-rebuild-tools-and-schema-reset-alignment.md`
3. 然后读取：
   - `.devflow/grain-platform-bootstrap/state.md`
   - `.devflow/grain-platform-bootstrap/plans/2026-05-24-dashboard-health-and-warning-threshold-fix.md`
   - `NEXT-SESSION-PROMPT-DEVFLOW.md`
4. 如需更多背景，再按需读取：
   - `.devflow/grain-platform-bootstrap/handoffs/2026-05-24-030-pause-ready-after-dashboard-health-and-warning-threshold-fix.md`
   - `.devflow/grain-platform-bootstrap/bug-log.md`
   - `.devflow/grain-platform-bootstrap/checkpoints.md`

## 可从活跃上下文移除的内容

- 本轮关于“analysis_result 要不要一起改”的即时确认过程
- 本轮对 `schema.sql` 中 demo 时间与旧预警样例的逐段定位输出
