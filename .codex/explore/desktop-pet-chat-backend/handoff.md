# 交接文档

## 当前目标
在保持当前后端低耦合结构不被破坏的前提下，继续推进桌宠后端从“可用 MVP”走向“可答辩 MVP”。当前优先项已从记忆模块切换到轻量上下文感知。

## 当前进度
- 后端模块化、聊天闭环和事件协议已完成。
- 最小 `Pet Controller` 链路已完成。
- `Task 7` 已完成：
  - 新增 `MemoryPolicy`
  - 新增 `SqliteMemoryStore`
  - 新增 provider factory
  - 新增 memory status / toggle / clear API
  - `ChatOrchestrator` 已只依赖 `MemoryService`
- 前端模型交互已补充：
  - toolbar 上新增明确的 `Q版` 切换按钮
  - 正比模型拖拽中会临时切换到 `QQ小黑猫`
  - 拖拽结束后恢复正比模型
  - 若当前已经是 Q版模型，则拖拽中不再切换模型
- memory MVP 已支持：
  - 偏好 / 习惯 / 近期事件三类抽取
  - 本地持久化
  - 召回
  - 清空记忆
  - 关闭记忆写入

## 验证情况
- `python -m compileall backend/app` 通过。
- 通过虚拟环境内脚本验证了：
  - SQLite provider 初始化
  - 记忆写入与召回
  - 关闭写入后的计数不再增长
  - 清空记忆
  - memory 路由函数级调用
- `TestClient` 方案未完成，因为当前虚拟环境缺 `httpx`。

## 当前判断
- `Task 7` 已达成 MVP 目标。
- 当前不应继续扩大记忆复杂度，除非 `Task 8` / `Task 9` 反向暴露设计问题。
- 更复杂的记忆结构、向量检索和外部记忆服务替换，应留到 `Task 12` 处理。

## 下一步
1. 进入 `Task 8`，实现轻量上下文感知。
2. 在 `Task 9` 中把 memory API 的开关能力接到设置页。
3. 在 `Task 9` 中决定模型类型是否需要持久化到本地配置。
4. 如果 `Task 8` / `Task 9` 需要额外 memory metadata，再回写 `design.md` 而不是直接临时加字段。

## 接手时先看
- `openspec/changes/desktop-pet-companion-roadmap/design.md`
- `openspec/changes/desktop-pet-companion-roadmap/tasks.md`
- `backend/app/services/chat_orchestrator.py`
- `backend/app/services/memory_service.py`
- `backend/app/services/memory_policy.py`
- `backend/app/memory/base.py`
- `backend/app/memory/sqlite_store.py`
- `backend/app/api/routes/memory.py`
