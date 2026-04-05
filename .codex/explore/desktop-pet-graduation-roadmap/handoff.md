# 交接文档

## 当前目标
继续按毕业设计节奏推进桌宠项目，当前主线已从“基础记忆与配置能力实现”切换到“Task 10 答辩演示包与说明材料收口”，同时保留可写论文、可答辩、可续接的过程记录。

## 当前进度
- 前端展示层已完成：Live2D、边框、toolbar、拖拽、缩放、联动缩放均已具备。
- toolbar 上已有明确的 `Q版` 切换按钮；当前支持“正比模型拖拽中临时切换到 `QQ小黑猫`，拖拽结束恢复；Q版模型拖拽中不换模”。
- 已建立全局 roadmap 目录作为毕设全过程主线记录区。
- OpenSpec 已完成 `Task 5`、`Task 6`、`Task 6.1`、`Task 7`、`Task 8`、`Task 9`。
- 后端已落地低耦合的 MVP 记忆结构、SQLite provider、memory API、context API 和开发态记忆查看接口。

## 关键文件/产物
- `openspec/changes/desktop-pet-companion-roadmap/design.md`
- `openspec/changes/desktop-pet-companion-roadmap/tasks.md`
- `.codex/explore/desktop-pet-chat-backend/state.md`
- `.codex/explore/desktop-pet-chat-backend/handoff.md`
- `backend/app/services/memory_service.py`
- `backend/app/services/memory_policy.py`
- `backend/app/memory/sqlite_store.py`
- `backend/app/api/routes/memory.py`
- `frontend/src/App.tsx`
- `frontend/src/petModels.ts`
- `zzz-doc/桌宠毕设过程记录指南.md`

## 已做的决策（摘要）
- 毕设记录继续采用“全局 roadmap + 专项 explore + OpenSpec 真相源”的三层结构。
- 后端记忆模块采用低耦合可替换架构，不把具体 provider 写死到聊天主链路。
- 前端模型交互采用“常驻模型切换 + 拖拽态临时换模”分离策略，只在正比模型拖拽时临时切 Q版。
- 当前主线不再继续扩展后端大功能，而是优先收口答辩材料；若继续写代码，先补 `Task 4` 证据。

## 立即要做的下一步
1. 进入 `Task 10`，整理 `5-8` 分钟答辩演示脚本。
2. 补齐安装与启动说明，明确根目录启动、前后端单独启动和环境要求。
3. 产出演示检查清单，覆盖模型展示、拖拽、对话、记忆召回、上下文提示五个演示点。
4. 视时间补 `Task 4` 的位置本地持久化验证证据，再同步回 OpenSpec。

## 恢复指引
1. 新对话优先读取本目录的 `handoff.md` 和 `state.md`。
2. 如果要继续后端与记忆主线，再读 `.codex/explore/desktop-pet-chat-backend/state.md` 与 `handoff.md`。
3. 进入具体实现时，以 OpenSpec 的 `proposal.md / design.md / tasks.md` 为真相源。
