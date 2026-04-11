# Phase 9: 管理端列表去 mock 并收口服务端分页查询 - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-04-11
**Phase:** 09-管理端列表去 mock 并收口服务端分页查询
**Areas discussed:** 覆盖范围, 查询形态, PredictionView 列表接口, mock 文件处理

---

## 覆盖范围

| Option | Description | Selected |
|--------|-------------|----------|
| 只收口 CRUD 列表 | 先做 `UsersView`、`WarehouseView`、`PredictionView`，删除残留 mock，`DashboardView` 留待后续阶段 | |
| Dashboard 也纳入 | 把 `DashboardView` 也改成后端分页/独立接口，首页不再做本地分页 | ✓ |

**User's choice:** Dashboard 也纳入。
**Notes:** 用户明确要求首页也遵守“后端分页/后端查询”的主线，不把 Dashboard 留成例外。

---

## 查询形态

| Option | Description | Selected |
|--------|-------------|----------|
| 轻量 keyword + 分页 | 保持一个关键词搜索框，后端支持 `keyword + pageNum/pageSize`，不扩成高级筛选面板 | ✓ |
| 高级筛选 | 顺手给列表页补结构化筛选条件，扩大本阶段范围 | |

**User's choice:** 按推荐执行轻量 keyword + 分页。
**Notes:** 用户接受“其它的按照推荐的来”，因此本项按推荐锁定。

---

## PredictionView 列表接口

| Option | Description | Selected |
|--------|-------------|----------|
| 轻量列表 + 详情接口 | 分页列表只返回轻量 task 行，切换任务时再单独查详情 | |
| 完整任务详情 + 分页参数 | 列表接口继续返回完整任务详情和 `resultList`，只新增分页参数与后端分页能力 | ✓ |

**User's choice:** 继续让列表接口返回完整任务详情，只是加分页参数。
**Notes:** 用户优先保持当前 `PredictionView` 交互结构稳定，不在本阶段引入新的详情拉取链路。

---

## mock 文件处理

| Option | Description | Selected |
|--------|-------------|----------|
| 直接删除 | 确认无运行时引用后直接删除 `frontend/src/mock/platform.js` | ✓ |
| 保留归档 | 搬到 fixtures / archive 等目录但不参与运行时 | |

**User's choice:** 按推荐直接删除。
**Notes:** 用户接受“其它的按照推荐的来”，因此本项按推荐锁定。

---

## the agent's Discretion

- 各分页接口的命名、DTO 字段和前后端并行请求组织方式
- 本轮改造后 `useClientPagination.js` / `useIncrementalList.js` 是否仍保留给其他页面使用

## Deferred Ideas

- 用户、仓库、预测任务页面更细粒度的结构化筛选
- PredictionView 未来改为“轻量列表 DTO + 详情接口”
- Dashboard 更深一层的缓存化、专用统计接口拆分
