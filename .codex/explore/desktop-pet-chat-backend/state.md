# 当前状态

## 当前阶段
后端最小闭环、高层事件协议、MVP 记忆服务和轻量上下文感知都已落地，当前主线已切换到 `Task 9` 的设置页和隐私开关准备阶段。

## 已确认的事实
- 后端已重构为模块化目录，包含配置层、API 路由层、服务层、记忆抽象层和 schema 层。
- 后端已提供 `GET /health`、`POST /api/chat`、`GET /api/events`、`GET /dev/control`、`POST /api/dev/messages`。
- 后端新增了 memory 控制接口：`GET /api/memory/status`、`PATCH /api/memory/status`、`DELETE /api/memory`。
- `ChatOrchestrator` 现在只通过 `MemoryService` 使用记忆能力，不直接依赖具体 provider。
- 记忆模块已按低耦合方向分层为 `ChatOrchestrator -> MemoryService -> MemoryPolicy + MemoryStore`。
- 服务装配层已支持按配置选择 `in_memory` 或 `sqlite` provider，不再写死 `InMemoryStore()`。
- 已新增 `SqliteMemoryStore`，实现本地持久化、基础召回、清空记忆和状态查询。
- `MemoryPolicy` 已实现 MVP 级别的记忆分类规则，至少能抽取偏好、习惯、近期事件三类记忆。
- 当前后端已经具备向外部记忆、上下文感知和更强 agent 形态演进的基础边界，但目前仍是“可扩展对话底座”，不是完整 agent runtime。
- 已新增 `ContextService` 与 `ContextProvider` 边界，当前 MVP provider 为 Windows 活动窗口感知。
- 已新增 context 控制接口：`GET /api/context/status`、`PATCH /api/context/status`。
- 已新增记忆联调查看入口：`GET /dev/memory`、`GET /api/dev/memories`。
- 当上下文可用时，聊天系统提示会明确要求模型说明来源是“当前活动窗口/应用上下文”；当上下文采集失败时，系统会自动降级，不影响聊天主链路。
- 根目录 `npm start` 仍可同时拉起前后端，后端也保留了独立启动脚本。
- 前端 toolbar 已接入明确的 `Q版` 切换按钮；当常驻模型为正比时，拖拽中会临时切换到 `QQ小黑猫`，拖拽结束后恢复；当常驻模型本身已是 Q版时，拖拽中不切换模型。

## 本轮验证证据
- `python -m compileall backend/app` 通过。
- 虚拟环境内的最小行为验证通过：
  - SQLite provider 正常初始化。
  - 记忆写入、召回、关闭写入和清空记忆都正常。
  - memory 路由函数级调用验证通过。
- context 行为函数级验证通过：
  - 默认关闭状态正常返回。
  - 运行时开启后可读到当前活动应用与窗口名。
  - 聊天 prompt 中会注入“当前活动窗口/应用上下文”来源说明。
  - provider 抛错时聊天仍能正常返回。
- 记忆查看入口函数级验证通过：
  - `MemoryService.list_recent()` 可返回最近记忆记录。
  - debug 入口所需 schema 和 store 边界均已打通。
- API 级 `TestClient` 验证未执行完成，因为当前虚拟环境缺少 `httpx`，但路由本身已通过函数级验证。

## 待解决的问题
- 记忆抽取目前仍是启发式规则，属于 MVP 方案，不是长期稳定方案。
- 还没有前端设置页去直接消费新的 memory API。
- 模型类型切换目前还是前端本地状态，尚未接入设置页或本地持久化配置。
- 还没有独立的 `ContextService`、`AgentService` 或工具注册层；如果后续要接 LangChain、外部记忆或更强 agent 能力，应该沿现有服务边界继续补，而不是反向耦回聊天主链路。
- 隐私开关前端承接、模型类型持久化和答辩演示模式尚未进入实现阶段。
- OpenSpec 里的 formal review 任务还未单独关闭。

## 下一步
- 优先进入 `Phase 1 / Task 9`，把记忆开关和上下文开关接到设置页。
- 等 `Task 8`、`Task 9` 稳定后，再进入 `Task 10` 组织答辩演示包。
