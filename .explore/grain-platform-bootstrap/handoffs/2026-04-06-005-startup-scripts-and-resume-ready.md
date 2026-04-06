# Handoff

## 基础信息

- 创建时间：2026-04-06
- mission：grain-platform-bootstrap
- 当前阶段：Handoff / Ready for resume
- handoff 编号：005
- 是否 superseded：否

## 当前目标

- 保存“前后端启动脚本已收口到 8081 联调口径，并补齐 mac 启动脚本”的可恢复状态。
- 让下一次会话可以直接从“启动验证 + 前后端联调”继续，而不需要重新梳理启动方式。

## 当前进度

- 已确认正式前端为 `frontend/`，`frontend-next/` 仅作为静态原型参考。
- 已确认前端依赖已安装，后端 Maven 在当前环境可用。
- 已将 PowerShell 启动脚本统一到 `8081` 联调端口。
- 已补充 mac 版本启动脚本：`start-backend.sh`、`start-frontend.sh`、`start-all.sh`。
- 已确认当前仓库实际文档目录为 `zzz-docs/`，不存在 `zzz-doc/`。
- 已更新下一轮恢复提示词，便于直接继续联调与验证。

## 本轮完成内容

- [x] 检查前端当前完成度与真实 API 接入范围
- [x] 检查前后端依赖安装状态
- [x] 确认仓库中已有 PowerShell 一键启动脚本
- [x] 将 `scripts/start-backend.ps1` 固定为 `8081` 端口启动
- [x] 将 `scripts/start-frontend.ps1` 固定注入 `VITE_API_BASE=http://localhost:8081`
- [x] 将 `scripts/start-all.ps1` 的联调提示更新为 `8081`
- [x] 新增 mac 启动脚本 `scripts/start-backend.sh`
- [x] 新增 mac 启动脚本 `scripts/start-frontend.sh`
- [x] 新增 mac 一键启动脚本 `scripts/start-all.sh`
- [x] 更新 `NEXT-SESSION-PROMPT.md`
- [x] 生成本轮 handoff

## 关键验证与判断

1. 当前正式前端实现目录为 `frontend/`
   - 登录、仓库、环境数据、预测四块已具备真实 API 接入基础
   - 用户管理、仪表盘部分区域、预测历史仍保留 mock 数据
2. 当前后端启动不再受“缺少 Maven”阻塞
   - 实测当前环境 `mvn -v` 可用
3. 当前联调仍应优先使用 `8081`
   - 原因不是代码故障，而是本地旧 Java 进程可能占用 `8080`
4. `zzz-doc/桌宠前端开发问题修复清单.md` 未更新
   - 当前仓库不存在 `zzz-doc/` 目录，也未发现该问题清单文件
   - 本轮主要是脚本收口和交接补全，不属于“桌宠前端问题修复”范畴

## 关键决策与原因

| 决策 | 备选方案 | 原因 |
| --- | --- | --- |
| PowerShell 启动脚本统一固定到 `8081` | 继续维持 `8080` 默认值 | 已知 `8080` 可能被旧进程占用，固定 `8081` 更符合当前联调真相源 |
| 前端启动脚本直接注入 `VITE_API_BASE` | 继续依赖人工手动传环境变量 | 减少重复操作，降低恢复成本 |
| 补 `mac` 版本 `.sh` 启动脚本 | 仅保留 `.ps1` | 当前会话环境是 macOS，补齐本地可直接启动方式更稳 |
| `start-all.sh` 采用 `osascript` 打开两个 Terminal 窗口 | 在同一 shell 串行启动 | 更接近 Windows `start-all.ps1` 的双窗口体验 |
| 问题清单本轮不新增记录 | 强行创建 `zzz-doc/` 或新建无关问题清单 | 仓库真实结构中无该目录，且本轮无对应问题修复事项 |

## 关键文件 / 产物

| 文件 | 作用 | 相关性 |
| --- | --- | --- |
| `scripts/start-backend.ps1` | Windows 后端启动脚本，固定 `8081` | 最高 |
| `scripts/start-frontend.ps1` | Windows 前端启动脚本，固定 `VITE_API_BASE` | 最高 |
| `scripts/start-all.ps1` | Windows 一键启动脚本 | 高 |
| `scripts/start-backend.sh` | mac 后端启动脚本 | 最高 |
| `scripts/start-frontend.sh` | mac 前端启动脚本 | 最高 |
| `scripts/start-all.sh` | mac 一键启动脚本 | 高 |
| `NEXT-SESSION-PROMPT.md` | 下次会话直接复制的恢复提示词 | 高 |
| `.explore/grain-platform-bootstrap/state.md` | 当前 mission 状态真相源 | 高 |
| `.explore/grain-platform-bootstrap/handoffs/2026-04-06-005-startup-scripts-and-resume-ready.md` | 本轮可恢复交接 | 最高 |

## 风险 / 阻塞项 / 开放问题

- [ ] 还未实际跑一遍新的 `start-all.ps1` / `start-all.sh` 做端到端验证
- [ ] 前端默认源码中的 `frontend/src/api/http.js` 仍默认回落到 `http://localhost:8080`，当前是靠启动脚本注入 `VITE_API_BASE` 收口
- [ ] 用户管理、角色选项、指标选项、预测历史仍未联调到真实持久层
- [ ] 仪表盘健康度、最近采样、更多聚合仍待真实接口替换

## 立即下一步

1. 先分别验证：
   - Windows：`scripts/start-backend.ps1`、`scripts/start-frontend.ps1`、`scripts/start-all.ps1`
   - mac：`scripts/start-backend.sh`、`scripts/start-frontend.sh`、`scripts/start-all.sh`
2. 验证前端是否成功请求到 `http://localhost:8081`
3. 进入 `frontend/` 联调主线：
   - 登录页
   - 仓库页
   - 环境数据页
   - 预测页
4. 再补用户管理、角色选项、指标选项、预测历史等正式接口
5. 最后收口仪表盘增强与演示流程

## 环境注意事项

- 本地数据库：`grain_env_predict`
- 数据库用户：`root`
- 数据库密码：空
- 当前联调端口：`8081`
- 正式前端目录：`frontend/`
- 静态原型目录：`frontend-next/`

## 恢复指引

1. 先读取 `NEXT-SESSION-PROMPT.md`
2. 再读取 `.explore/grain-platform-bootstrap/handoffs/index.md`
3. 再读取本 handoff
4. 再读取 `.explore/grain-platform-bootstrap/state.md`
5. 必要时补读：
   - `zzz-docs/开发指导版PRD-粮仓环境数据预测管理平台.md`
   - `.explore/grain-platform-bootstrap/handoffs/2026-04-06-004-backend-real-persistence-resume-ready.md`
6. 从“立即下一步”的第 1 条开始继续

## 可从活跃上下文移除的内容

- 关于 Maven 是否已安装的短期确认过程
- 旧脚本仍指向 `8080` 的排查细节
- mac 一键启动脚本第一次写错命令的中间过程
