# Phase 12: Windows 免依赖运行封装与根目录 README 双运行说明（手动运行 / 免安装运行） - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-04-11
**Phase:** 12-Windows 免依赖运行封装与根目录 README 双运行说明（手动运行 / 免安装运行）
**Areas discussed:** 免安装运行形态, 数据库方案, 分发边界, 启动与 README

---

## 免安装运行形态

| Option | Description | Selected |
|--------|-------------|----------|
| Portable 发布目录 | 前端 `dist` + 后端 `jar` + 内置 JRE + 内置 MySQL，portable 脚本一键起停 | ✓ |
| 源码仓库直接带运行时 | 仍按源码开发方式启动，只是把运行时一起塞进仓库 | |
| 其他 | 自定义方案 | |

**User's choice:** 按推荐方案执行。  
**Notes:** 免安装模式不再依赖 `node`、`npm`、`mvn` 参与运行，目标是更接近“解压就能跑”的 Windows portable 包。

---

## 数据库方案

| Option | Description | Selected |
|--------|-------------|----------|
| 真实 MySQL 便携版 | 继续使用 MySQL，只是改成 portable 形态，复用 Phase 11 baseline/reset | ✓ |
| 嵌入式数据库 | 只求能演示，可不保持 MySQL 一致性 | |
| 其他 | 自定义方案 | |

**User's choice:** 按推荐方案执行。  
**Notes:** 用户接受继续沿用 MySQL，不希望为了 portable 运行专门换数据库栈。

---

## 分发边界

| Option | Description | Selected |
|--------|-------------|----------|
| 生成单独发布目录 | portable 包生成到单独目录，通过 `.gitignore` 忽略，不提交仓库 | ✓ |
| 仓库内直接携带大文件 | 把 runtime 和产物直接放仓库中分发 | |
| 其他 | 自定义方案 | |

**User's choice:** 免安装运行包不放仓库，直接 `.gitignore` 忽略，自己压缩后发给别人。  
**Notes:** 这是本 phase 最明确的硬约束之一，planner / executor 不能把大体积运行时重新写回 git 管理。

---

## 启动与 README

| Option | Description | Selected |
|--------|-------------|----------|
| 单主脚本 + 双模式 README | portable 模式尽量一个主脚本启动；README 同时写开发态手动运行和 portable 运行 | ✓ |
| 多脚本手动编排 | 用户自己分步骤先开数据库再开前后端 | |
| 其他 | 自定义方案 | |

**User's choice:** 按推荐方案执行。  
**Notes:** 默认保留数据，单独提供 reset 入口；README 需要把开发态依赖安装与 portable 免安装运行讲清楚。

---

## the agent's Discretion

- 发布目录具体命名
- portable 进程编排与日志形式
- 前端静态资源具体托管方式

## Deferred Ideas

- macOS / Linux portable 方案
- 正式安装器（MSI / EXE）
- 依赖 Docker Desktop / WSL2 的容器化方案
