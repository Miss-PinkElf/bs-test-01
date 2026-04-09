# 初始化策略收口设计

## 目标

把当前“后端每次启动自动重建演示库”的行为改成更可控的两段式流程：

1. 默认启动后端时保留现有数据库数据。
2. 只有在开发者显式执行重置入口时，才重建并回填演示库。

这次只处理初始化策略，不顺带扩展仓库 CRUD、用户 CRUD 或验收脚本。

## 现状问题

- `backend/src/main/resources/application.yml` 当前使用 `spring.sql.init.mode=always`。
- `backend/src/main/resources/db/schema.sql` 同时包含：
  - 建库
  - 删表
  - 建表
  - 演示数据回填
- 结果是后端每次启动都会清掉本地联调数据，不利于导入回归、CRUD 联调和答辩演示准备。

## 方案对比

### 方案 A：默认 `never`，单独提供数据库重置脚本

- 做法：
  - 把默认 `spring.sql.init.mode` 改成 `never`
  - 新增 PowerShell 重置脚本，显式执行 `schema.sql`
- 优点：
  - 默认行为最安全
  - 重置动作与应用启动解耦
  - 非常适合当前 Windows 本地开发环境
- 缺点：
  - 第一次使用前需要先跑一次重置脚本

### 方案 B：保留默认自动初始化，另加环境变量开关

- 做法：
  - 继续保留自动初始化，只在某些启动参数下跳过
- 优点：
  - 初次启动门槛低
- 缺点：
  - 默认行为仍然危险
  - 更容易误操作清库

### 方案 C：拆分 `schema.sql` / `data.sql`

- 做法：
  - 建表和种子数据拆成两份，再通过不同 profile 组合加载
- 优点：
  - 结构最规范
- 缺点：
  - 当前改动面更大
  - 对本轮“先稳定联调”的价值不如方案 A 高

## 选型

采用方案 A。

理由：

- 当前任务目标是尽快稳定住本地开发态，而不是做完整初始化体系重构。
- 用户本机和当前联调流程都以 PowerShell 为主，增加显式重置脚本最直接。
- 该方案与后续 PowerShell 验收脚本也天然兼容。

## 设计

### 配置层

- 修改 `backend/src/main/resources/application.yml`
  - 将 `spring.sql.init.mode` 从 `always` 改为 `never`
  - 保留 `schema-locations`，作为显式重置与未来 profile 复用入口

### 脚本层

- 新增 `scripts/reset-demo-db.ps1`
  - 默认按 `root / 123456 / localhost / 3306 / grain_env_predict` 连接
  - 从仓库内 `backend/src/main/resources/db/schema.sql` 读取 SQL
  - 通过本机 `mysql` 客户端显式执行
  - 执行前检查 `mysql` 是否存在、SQL 文件是否存在
  - 成功后输出明确提示

### 启动入口

- 更新 `scripts/start-backend.ps1`
  - 启动前提示“当前不会自动重建演示库”
  - 需要重置时提示使用 `scripts/reset-demo-db.ps1`
- 更新 `scripts/start-backend.sh`
  - 保持相同语义，避免双端入口口径漂移

### 文档

- 更新 `README.md`
- 更新 `zzz-docs/验证/数据库优先MVP-回归验证清单.md`
- 更新 `.devflow` 当前计划入口与状态记录

## 错误处理

- 若未安装 `mysql` CLI，重置脚本直接失败并给出明确提示。
- 若 `schema.sql` 路径不存在，脚本直接失败，避免误报重置成功。
- 若数据库认证失败，脚本保留 `mysql` 原始错误，便于继续排查本地凭据。

## 验证

### 静态验证

- `backend/` 执行 `mvn -q -DskipTests compile`

### 运行态验证

1. 执行 `.\scripts\reset-demo-db.ps1`
2. 启动后端到 `8081`
3. 请求 `/api/dashboard/overview`
4. 确认应用能正常启动，且重启后不会再自动清库
