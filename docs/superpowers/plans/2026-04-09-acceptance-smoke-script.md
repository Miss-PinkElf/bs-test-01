# Acceptance Smoke Script Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 补一个一键 PowerShell 验收脚本，把当前数据库优先 MVP 的静态验证与关键运行态 smoke 串起来。

**Architecture:** 脚本默认先重置演示库，再执行后端编译、前端构建、启动后端、用户 CRUD smoke、固定模板下载与导入、旧 CSV / 旧 XLS 兼容导入、首页概览与预测只读接口检查；运行过程使用临时目录保存中间文件和启动日志，最后自动清理后端进程与临时测试数据。

**Tech Stack:** PowerShell、Maven、npm、Spring Boot HTTP API

---

### Task 1: 补齐验收脚本主体

**Files:**
- Create: `scripts/run-acceptance-smoke.ps1`

- [ ] 增加静态验证步骤：`mvn -q -DskipTests compile` 与 `npm run build`
- [ ] 增加可选数据库重置步骤，默认调用 `scripts/reset-demo-db.ps1`
- [ ] 增加后端启动、ready 等待、日志采集与进程清理
- [ ] 增加用户 CRUD smoke、首页概览校验、预测任务只读校验
- [ ] 增加固定模板下载、固定模板导入、旧 CSV / 旧 XLS 导入与结果校验

### Task 2: 更新脚本文档入口

**Files:**
- Modify: `.devflow/grain-platform-bootstrap/plans/active-plan-links.md`
- Modify: `zzz-docs/验证/数据库优先MVP-回归验证清单.md`

- [ ] 将新脚本计划加入当前活跃计划索引
- [ ] 在回归验证清单中加入一键脚本入口与预期覆盖范围

### Task 3: 运行脚本并回写 devflow

**Files:**
- Modify: `.devflow/grain-platform-bootstrap/state.md`
- Modify: `.devflow/grain-platform-bootstrap/checkpoints.md`
- Modify: `.devflow/grain-platform-bootstrap/session-tasks.md`

- [ ] 实际运行 `scripts/run-acceptance-smoke.ps1`
- [ ] 记录脚本验证证据与后续主线
