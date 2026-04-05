# 当前状态

## 当前阶段
Phase C：基础记忆与配置能力推进中，`Task 7`、`Task 8`、`Task 9` 已完成，当前转入 `Task 10` 的答辩演示包与说明材料收口阶段。

## 已确认的事实
- 前端桌宠展示层已经稳定，Live2D、hover 边框、右侧 toolbar、顶部拖拽把手、右下角缩放手柄和联动缩放都已具备。
- 前端 toolbar 已有明确的 `Q版` 切换按钮，可在正比模型与 Q版模型之间切换。
- 当常驻模型为正比模型时，拖拽中会临时切换到 `QQ小黑猫`，拖拽结束后恢复；当当前已是 Q版模型时，拖拽中不切换模型。
- OpenSpec 中前端展示直接相关的 `Task 2`、`Task 3`、`Task 4.1`、`Task 4.2`、`Task 4.3` 已完成。
- `Task 4` 整体仍未完成，因为“位置本地持久化”尚未补齐完成证据。
- `Task 5` 与 `Task 6` 已完成真实开发环境联调验证，前后端简单对话闭环已经跑通。
- `Task 6.1` 已完成，完整高层事件协议和最小 `Pet Controller` 已落地。
- `Task 7` 已完成，后端已具备低耦合的 MVP 记忆服务：
  - `ChatOrchestrator -> MemoryService -> MemoryPolicy + MemoryStore`
  - provider factory 已支持 `in_memory` / `sqlite`
  - 已新增 `SqliteMemoryStore`
  - 已新增 memory status / toggle / clear API
  - 已支持偏好、习惯、近期事件三类 MVP 记忆抽取
- `Task 8` 已完成，后端已具备低耦合的轻量上下文感知能力：
  - 新增 `ContextService -> ContextProvider` 边界
  - 当前 MVP provider 为 Windows 活动窗口感知
  - 已新增 context status / runtime toggle API
  - 聊天提示会显式注明来源为“当前活动窗口/应用上下文”
  - 感知失败时自动降级，不阻断聊天主链路
- `Task 9` 已完成，前端已具备最小设置与隐私控制能力：
  - 新增轻量设置面板
  - 已接入模型形态切换和本地持久化
  - 已接入 memory 开关、memory 写入开关、context 开关和状态回显
  - 已支持“保存当前设置”，下次启动会自动恢复模型形态和 memory/context 开关偏好
  - 已支持一键清空记忆与一键重置演示状态
- 后端已新增简单记忆查看能力：
  - `GET /dev/memory` 提供联调查看页
  - `GET /api/dev/memories` 提供最近记忆记录 JSON
- 后端仍保持模块化结构：`app/api`、`app/services`、`app/memory`、`app/schemas`、`app/core`。
- 本轮关键验证证据已补齐：`python -m compileall backend/app` 通过；虚拟环境内已完成 SQLite provider、记忆写入/召回、关闭写入、清空记忆和 memory 路由函数级验证；也已完成 context 默认关闭、运行时开启、来源提示注入和失败降级的函数级验证；新增的记忆查看接口已通过函数级验证；前端设置面板已通过 `npm.cmd --prefix frontend run lint`。

## 工作假设
- 毕设 MVP 当前优先顺序是：先站稳记忆与配置接口，再推进轻量上下文感知，最后收口设置页和答辩演示材料。
- 记忆模块的当前启发式抽取策略只服务 MVP，不作为长期最终方案。
- 后续若要更换记忆底层实现，应继续沿用当前低耦合边界，不直接破坏聊天主链路。
- 模型类型切换后续更适合接入设置页或本地配置层，而不是继续散落在前端局部状态逻辑中。

## 待解决的问题
- `Task 4` 的位置本地持久化仍未收口。
- 前端全量 TypeScript 校验仍受仓库既有 `TypeScript 4.5` 与较新 `@types/node` 不兼容问题影响，当前只做局部 lint 更可靠。
- 需要继续沉淀适合论文和答辩复用的“问题-原因-解决-验证”过程材料。

## 下一步
- 进入 `Phase 1 / Task 10`，组织 `5-8` 分钟答辩演示脚本、安装启动说明和演示检查清单。
- 视时间补齐 `Task 4` 剩余的位置本地持久化证据，避免阶段记录里留下单独未闭环项。

## 最小活跃上下文摘要
当前桌宠项目已经完成前端展示层、前后端简单对话闭环、完整高层事件协议、MVP 记忆服务、轻量上下文感知和最小设置面板。下一阶段重点是答辩演示脚本、说明材料和阶段收口。
