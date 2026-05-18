# Bug 记录（grain-platform-bootstrap）

> 格式：**现象** / **原因** / **解决方案** / **关联**  
> 与 `learnings.md` 配合：本文件保留可追溯案例，经验条目不重复堆砌细节。

---

## BUG-2026-04-10-001：管理端主内容区横向宽度持续增长（温度预测页最明显）

### 问题现象

- 进入 Vue 管理端后（尤以 **`/prediction` 温度预测** 为甚），页面**横向宽度持续增加**，右侧按钮、卡片等**不断被推向更右侧**，仿佛布局在无限变宽。
- 首轮仅通过 CSS为 flex /滚动视图补充 `min-width: 0`、约束 `.chart-box` 等，**未能完全消除**。

### 问题原因

1. **Element Plus `el-scrollbar`**（2.x）：实现中对内容视图注册了 **`useResizeObserver`**，且在 **`onUpdated` 钩子中调用 `update()`** 刷新滚动条状态。主内容区使用该组件时，任意子树更新都可能触发滚动条侧的更新链。
2. **ECharts 5**：对图表容器有**尺寸监听与自动 `resize`** 行为。
3. 二者与 **flex 布局默认 `min-width: auto`**（子项按内容最小宽度参与计算）叠加时，容易形成 **DOM 宽度 ↔ 滚动条/图表 resize** 的反馈，表现为横向尺寸**反复修正甚至持续外扩**。
4. 侧栏仍可使用 `el-scrollbar`；**问题集中在「主内容 +图表」同屏**场景。

### 解决方案

1. **主内容区不再使用 `el-scrollbar`**：在 `ConsoleLayout.vue` 中改为 **`div.console-main-native`**，使用 **`overflow-y: auto`**、**`overflow-x: hidden`**，并保留 **`min-width: 0`**、**`scrollbar-gutter: stable`**（`styles.css`）。
2. **收紧 flex**：`.console-body`、`.console-main` 使用 **`flex: 1 1 0`**；顶栏 `.console-header` 与子块补充 **`min-width: 0`** / **`flex-shrink: 0`**；`.page-stack`、`.el-col`、`.panel-card`、`.chart-box` 等继续约束最大宽度与溢出。
3. **预测页图表**：`PredictionView.vue` 在 `setOption` 之后于 **`requestAnimationFrame`** 内调用 **`chart.resize({ width: clientWidth, height: clientHeight, animation: { duration: 0 } })`**，减少与首帧布局叠加的抖动。

### 关联

- **代码：** `frontend/src/layout/ConsoleLayout.vue`、`frontend/src/styles.css`、`frontend/src/views/PredictionView.vue`
- **GSD quick：** `.planning/quick/260410-k32-vue-scrollbar-echarts-layout-fix/`
- **经验：** `.devflow/grain-platform-bootstrap/learnings.md`（同日期条目）

---

## BUG-2026-04-10-002：为压制横向无限变宽而全局关闭主内容 `overflow-x`，导致正常超宽内容无横向滚动且预测摘要被裁切

### 问题现象

- 在 **BUG-2026-04-10-001** 收口后，管理端主内容区改为 `.console-main-native` 原生滚动，并设置 **`overflow-x: hidden`**。
- 结果是：虽然“页面横向持续变宽”被止住，但**所有后台页面在真实超宽时也失去了正常横向滚动能力**。
- 在 `/prediction` 页，右侧「任务摘要」使用 `el-descriptions` 展示较长的时间区间与摘要文本时，会在部分宽度下出现**内容横向溢出后被直接裁切**，用户只能看到右边露出一截的灰条，无法正常拖动或阅读。

### 问题原因

1. **BUG-2026-04-10-001 的止血方案过于全局化**：为避免 `el-scrollbar + ECharts + ResizeObserver` 的横向尺寸反馈，直接在主内容容器层面禁用了所有横向滚动，而不是仅隔离高风险节点。
2. 预测页底部宽表已通过 `.prediction-history-table-wrap { overflow-x: auto; }` 做了**局部横向滚动**，但右侧摘要卡片仍然沿用 `el-descriptions` 表格式布局，且没有独立 wrapper。
3. `el-descriptions` / 卡片 body / 栅格列在窄列中若未同步补足 **`min-width: 0`**、内容换行与局部滚动保护，就会把真实溢出表现为“被上层 `overflow-x: hidden` 吃掉”，从而掩盖“这是局部内容超宽，不是整页继续失控”的区别。

### 解决方案

1. **布局层改为“允许正常横向滚动”**：保留主内容原生滚动，不把 `el-scrollbar` 放回主内容区；但将 `.console-main-native` 从 **`overflow-x: hidden`** 调整为 **`overflow-x: auto`**，让真实超宽场景恢复正常横向滚动。
2. **组件层负责避免误撑宽**：
   - 预测记录表继续保留局部 wrapper 横向滚动；
   - 任务摘要卡片新增 `task-summary-card` 与 `task-summary-table-wrap`，卡片 body / wrapper / descriptions 统一补 **`min-width: 0`**；
   - `el-descriptions__table` 使用 **`table-layout: fixed`**，内容单元格开启 **`overflow-wrap: anywhere`** / `word-break: break-word`，避免长时间串把列无限撑宽。
3. **维持上个 bug 的核心规避点**：主内容层依旧不回退到 `el-scrollbar`，避免重新把高风险的滚动条更新链与图表 resize 绑在一起。

### 关联

- **代码：** `frontend/src/styles.css`、`frontend/src/views/PredictionView.vue`
- **上游问题：** `BUG-2026-04-10-001`
- **验证：** `frontend/` 执行 `npm run build` 通过

---

## BUG-2026-04-12-003：`npm run dev` 通过 Windows PowerShell 启动后端时误报“字符串缺少终止符”

### 问题现象

- 用户在仓库根目录执行 `npm run dev` 后，后端子进程立即失败。
- 日志显示：`scripts/start-backend.ps1:70` 报 `字符串缺少终止符: "`，看起来像最后两行英文 `Write-Host` 自身写坏了。
- 直接查看脚本文本时，第 69-70 行语法表面正常，导致首轮现象与源码不一致。

### 复现方式

1. 在 Windows 环境执行 `npm run dev`。
2. `node ./scripts/dev-inline.cjs` 通过 `powershell` 拉起 `scripts/start-backend.ps1`。
3. 观察后端输出，PowerShell 在脚本尾部抛出 `TerminatorExpectedAtEndOfString`。

### 根因分析

1. 真正的问题不在第 70 行英文字符串本身，而在脚本前部中文 `Write-Host` 文本与 Windows PowerShell 5.1 对 UTF-8 无 BOM `.ps1` 的解析差异。
2. npm 入口和 `dev-inline.cjs` 均直接绑定 `powershell`，使脚本稳定落到较脆弱的宿主路径上。
3. PowerShell 在前面已错误吞掉字符串边界，最终在尾部英文行才报出“缺少终止符”，所以报错行号具有迷惑性。

### 修复动作

1. 新增 `scripts/powershell-runtime.cjs`，集中处理 Windows PowerShell 运行参数。
2. 新增 `scripts/run-powershell-script.cjs`，让 `package.json` 下的 `backend` / `frontend` / `check-env` / `reset-demo-db` 统一通过 Node 代理拉起脚本。
3. 更新 `scripts/dev-inline.cjs`，复用共享运行参数构造逻辑，而不是内嵌一套单独的 PowerShell 命令字符串。
4. 将 `start-backend.ps1`、`start-frontend.ps1`、`check-env.ps1` 中会被 npm 直接拉起的提示文本改为 ASCII，避免再次触发同类编码解析问题。

### 验证结果

- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/start-backend.ps1`：不再出现 PowerShell 解析错误；脚本继续向下执行，并进入 Maven 启动阶段。
- `npm run backend`：不再出现 `字符串缺少终止符`；最新验证已跑到 Spring Boot 启动阶段，并因 `8081` 端口被占用而失败。
- 结论：原始 PowerShell 脚本解析 bug 已收口；后续若仍失败，属于端口占用、Maven 本地环境或沙箱限制等新的独立问题。

### 关联

- **代码：** `package.json`、`scripts/dev-inline.cjs`、`scripts/powershell-runtime.cjs`、`scripts/run-powershell-script.cjs`、`scripts/start-backend.ps1`、`scripts/start-frontend.ps1`、`scripts/check-env.ps1`
- **计划：** `.devflow/grain-platform-bootstrap/plans/2026-04-12-windows-powershell-startup-script-compatibility.md`
- **验证：** `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/start-backend.ps1`、`npm run backend`

---

## BUG-2026-04-20-004：粮温多 Excel 并发导入时 `grain_temp_record` 发生 MySQL deadlock

### 问题现象

- 在数据管理页一次导入多个粮温 Excel 时，单文件导入可以成功，但并发导入 6 个 Excel 仍会间歇性失败。
- 后端报错集中在 `grain_temp_record` 的 `insert ... on duplicate key update`，异常为 `Deadlock found when trying to get lock; try restarting transaction`。
- 已做过的首轮止血方案包括：
  - 模板与导入口径改为 `warehouseCode` 优先；
  - 同批重复点位保留最后一条；
  - 导入行稳定排序；
  - 进程内锁按 `warehouseId + collectedAt` 串行。
- 但在“同一仓库、不同采集时间”的多文件并发导入场景下，死锁仍然出现，说明首轮锁粒度不足。

### 问题原因

1. `grain_temp_record` 的唯一键是 `(point_id, collected_at)`，同时存在查询索引 `(warehouse_id, collected_at)`；多请求并发写入同一仓库时，即使采集时间不同，也会在同表索引页和相关行锁上产生竞争。
2. 首轮应用层锁只锁到 `warehouseId + collectedAt`，只能挡住“同仓库同时间”的并发，挡不住“同仓库不同时间”的 6 个文件并发导入。
3. 原始 SQL 采用多值批量 `upsertBatch`，单次语句会同时持有多条记录相关锁，事务窗口更长，死锁概率被放大。
4. 仓库字段历史上同时兼容 `warehouseId` 与 `warehouseCode`，如果先按数字解析主键，再回退到编码查询，纯数字仓库编码会被误判，进一步增加导入行为与预期不一致的排查成本。

### 解决方案

1. 将导入串行锁从 `warehouseId + collectedAt` 扩大为仅按 `warehouseId`，让同一仓库的多文件导入直接排队执行。
2. `grain_temp_record` 写入从多值 `upsertBatch` 降为单条 `upsert`，并在命中 deadlock 时做有限次短暂重试，缩短单次持锁窗口。
3. 在进入写库前继续保留同批去重与稳定排序，保证“冲突时以导入的最后一条为准”。
4. 仓库引用解析改为 `warehouseCode` 优先、`warehouseId` 兼容回退，避免数字型仓库编码被误认成主键。

### 当前状态

- 代码侧已完成第二轮修复，涉及：
  - `backend/src/main/java/com/grain/platform/service/GrainTempImportService.java`
  - `backend/src/main/java/com/grain/platform/mapper/GrainTempRecordMapper.java`
  - `backend/src/main/resources/mapper/GrainTempRecordMapper.xml`
- 静态验证已通过：`backend/` 执行 `mvn -q -DskipTests compile` 成功。
- 运行态并发复测仍待用户在真实“6 个 Excel 同仓库导入”场景下确认，因此当前结论是“根因已进一步收紧，代码已落地，运行态回归待确认”。

### 关联

- **代码：** `backend/src/main/java/com/grain/platform/service/GrainTempImportService.java`、`backend/src/main/java/com/grain/platform/mapper/GrainTempRecordMapper.java`、`backend/src/main/resources/mapper/GrainTempRecordMapper.xml`
- **表结构：** `backend/src/main/resources/db/schema.sql`（`grain_temp_record`、`grain_temp_point`）
- **经验：** `.devflow/grain-platform-bootstrap/learnings.md`（2026-04-20 条目）

---

## BUG-2026-05-18-005：普通环境数据模板直接导入时报 `collectedAt` 时间格式错误

### 问题现象

- 在数据管理页切换到“普通环境数据”后，下载普通环境数据导入模板并直接导入，页面提示：
  - `第 2 行字段 collectedAt 时间格式错误，应为 yyyy-MM-dd HH:mm:ss`
- 错误出现在模板示例数据行，用户没有手工构造异常数据。

### 问题原因

1. 普通环境模板当前是 CSV 文件，模板原始文本中的 `collectedAt` 是 `2026-04-07 08:00:00`。
2. 用户用 Excel 打开或保存 CSV 后，Excel 可能自动把时间文本改写成 `2026/4/7 8:00`、`2026-4-7 8:00:00` 等常见格式。
3. 后端 `SensorDataImportService` 原先只接受严格的 `yyyy-MM-dd HH:mm:ss`，导致“直接使用模板”在 Excel 改写后也可能被导入校验拦截。
4. 普通环境模板还包含 `temperature` 示例行，容易与当前粮温主线口径混淆。

### 解决方案

1. `SensorDataImportService` 的时间解析保留 `yyyy-MM-dd HH:mm:ss` 作为标准格式，同时兼容 Excel 常见日期时间文本：
   - `yyyy-M-d H:mm:ss`
   - `yyyy-M-d H:mm`
   - `yyyy/MM/dd HH:mm:ss`
   - `yyyy/M/d H:mm:ss`
   - `yyyy/M/d H:mm`
2. 普通环境 CSV 模板示例只保留 `humidity` 与 `co2`，不再放入 `temperature` 示例。
3. 新增 `SensorDataImportServiceTest`，覆盖模板原始格式、Excel 常见改写格式和非法格式。

### 验证结果

- `backend/` 执行 `mvn -q test -Dtest=SensorDataImportServiceTest` 通过。
- `backend/` 执行 `mvn -q -DskipTests compile` 通过。

### 关联

- **代码：** `backend/src/main/java/com/grain/platform/service/SensorDataImportService.java`
- **测试：** `backend/src/test/java/com/grain/platform/service/SensorDataImportServiceTest.java`
- **计划：** `.devflow/grain-platform-bootstrap/plans/2026-05-18-sensor-data-template-collected-at-import-fix.md`
