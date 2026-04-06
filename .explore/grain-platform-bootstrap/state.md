# 当前状态

## 当前阶段
- Handoff / Ready for resume

## 已确认的事实
- 用户要求使用 `context-budget-explore` 记录过程。
- 当前仓库已是 Git 仓库，当前分支为 `main`。
- 本地已检测到 `java`、`node`、`npm`，未检测到 `mvn`。
- 开题报告已转为 Markdown，核心课题是“基于 Spring Boot 的粮仓环境数据预测管理平台设计与实现”。
- 用户希望同时获得方案设计与项目骨架。
- 已将 `frontend-next/` 升级为高保真静态原型，补齐了仪表盘、用户管理、仓库管理、环境数据、温度预测和展示大屏。
- `frontend-next/README.md` 已补充，用于说明这套原型如何映射最终答辩成品预期。
- 已修复 `frontend-next/` 本地 `Next SWC` 依赖损坏问题，并重新通过 `npm run build` 验证。

## 工作假设
- 以毕业设计 MVP 为目标，先做可演示的软件平台，不接入真实硬件。
- 预测功能采用简单回归或移动平均，符合开题报告范围。
- 项目结构采用前后端分离：`backend/` + `frontend/`。

## 待解决的问题
- Maven 未安装时，一键启动脚本只能优先使用 `mvnw.cmd` 或提示用户使用 IDEA 内置 Maven。
- 正式实现路线已转向 Vue 技术栈，`frontend-next/` 现在只作为静态原型和答辩成品预期参考，而不是最终正式前端。
- 数据库 SQL 仍停留在初稿，后端接口、DTO/VO、Mapper 和 Vue 正式前端骨架仍未进入定稿实施。

## 下一步
- 如果进入正式开发，优先基于开发指导版 PRD 产出数据库 SQL 定稿。
- 然后拆后端接口清单、DTO/VO 和 MyBatis Mapper 设计。
- 再搭 Vue 3 + Vite + Element Plus + ECharts 的正式前端骨架。

## 最新 handoff
- `.explore/grain-platform-bootstrap/handoffs/2026-04-06-002-static-prototype-resume-ready.md`

## 最小活跃上下文摘要
- 已完成项目骨架、文档基础，以及一版更接近最终答辩成品效果的 `frontend-next/` 高保真静态原型；正式实现路线仍然是 Vue，下一步应围绕数据库定稿、接口设计和正式 Vue 前端骨架展开。
