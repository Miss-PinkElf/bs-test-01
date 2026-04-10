<!-- GSD:project-start source:PROJECT.md -->
## Project

Project not yet initialized. Run /gsd-new-project to set up.
<!-- GSD:project-end -->

<!-- GSD:stack-start source:STACK.md -->
## Technology Stack

Technology stack not yet documented. Will populate after codebase mapping or first phase.
<!-- GSD:stack-end -->

<!-- GSD:conventions-start source:CONVENTIONS.md -->
## Conventions

Conventions not yet established. Will populate as patterns emerge during development.
<!-- GSD:conventions-end -->

<!-- GSD:architecture-start source:ARCHITECTURE.md -->
## Architecture

Architecture not yet mapped. Follow existing patterns found in the codebase.
<!-- GSD:architecture-end -->

<!-- GSD:skills-start source:skills/ -->
## Project Skills

| Skill | Description | Path |
|-------|-------------|------|
| agent-browser | Browser automation CLI for AI agents. Use when the user needs to interact with websites, including navigating pages, filling forms, clicking buttons, taking screenshots, extracting data, testing web apps, or automating any browser task. Triggers include requests to "open a website", "fill out a form", "click a button", "take a screenshot", "scrape data from a page", "test this web app", "login to a site", "automate browser actions", or any task requiring programmatic web interaction. | `.claude/skills/agent-browser/SKILL.md` |
| brainstorming | "You MUST use this before any creative work - creating features, building components, adding functionality, or modifying behavior. Explores user intent, requirements and design before implementation." | `.claude/skills/brainstorming/SKILL.md` |
| context-budget-explore | \| 探索、需求、实施一体化的上下文预算工作流管理器。适用于长期探索、需求澄清、方案对比、迭代开发、质检闭环、跨对话续接、上下文过长、需要记录过程与决策的任务。优先在 Claude Code 中使用，但也可作为通用 skill：把阶段、任务、决策、经验、checkpoint、handoff 全部沉淀到仓库根目录 `.explore/`，让 AI 在长任务中始终有记录、有恢复点、有清晰过程。 只要用户提到“探索一下”“先梳理需求”“边做边记录”“跨对话继续”“保存进度”“上下文太长”“这个任务会做很久”“想把过程沉淀下来”，都应该主动触发本 skill。 | `.claude/skills/context-budget-explore/SKILL.md` |
| docx | "Use this skill whenever the user wants to create, read, edit, or manipulate Word documents (.docx files). Triggers include: any mention of 'Word doc', 'word document', '.docx', or requests to produce professional documents with formatting like tables of contents, headings, page numbers, or letterheads. Also use when extracting or reorganizing content from .docx files, inserting or replacing images in documents, performing find-and-replace in Word files, working with tracked changes or comments, or converting content into a polished Word document. If the user asks for a 'report', 'memo', 'letter', 'template', or similar deliverable as a Word or .docx file, use this skill. Do NOT use for PDFs, spreadsheets, Google Docs, or general coding tasks unrelated to document generation." | `.claude/skills/docx/SKILL.md` |
| electron-local-cache-install | Handle Electron binary download failures during `npm install`, `pnpm install`, or other package-manager installs when `node_modules/electron` fails in `install.js` or `@electron/get`. Use when the exact Electron zip is already available locally and Codex needs a project-scoped cache-based fix on Windows, macOS, or Linux/WSL without changing global npm config. | `.claude/skills/electron-local-cache-install/SKILL.md` |
| executing-plans | Use when you have a written implementation plan to execute in a separate session with review checkpoints | `.claude/skills/executing-plans/SKILL.md` |
| find-skills | Helps users discover and install agent skills when they ask questions like "how do I do X", "find a skill for X", "is there a skill that can...", or express interest in extending capabilities. This skill should be used when the user is looking for functionality that might exist as an installable skill. | `.claude/skills/find-skills/SKILL.md` |
| prd | 'Generate high-quality Product Requirements Documents (PRDs) for software systems and AI-powered features. Includes executive summaries, user stories, technical specifications, and risk analysis.' | `.claude/skills/prd/SKILL.md` |
| react-tsx-readability-guard | \| 让 React 和 TSX 代码更整洁、更好读。只要 Claude 需要生成、修改、重构或审查 React 组件、TSX 页面、JSX 较多的 UI 逻辑，尤其是 Ant Design 的表单、列表、弹窗、Drawer、Modal 等场景，都应优先使用这个 skill。即使用户没有明确提到“可读性”“clean code”“提常量”“加注释”，只要是在写 React/TSX 代码，也应该优先按本 skill 输出。优先保证代码结构清晰、命名语义化、减少魔法数字、拆分复杂条件、保持 JSX 轻量、避免过度抽象，并补充简短但有意义的注释。 | `.claude/skills/react-tsx-readability-guard/SKILL.md` |
| session-handoff | "Creates comprehensive handoff documents for seamless AI agent session transfers. Triggered when: (1) user requests handoff/memory/context save, (2) context window approaches capacity, (3) major task milestone completed, (4) work session ending, (5) user says 'save state', 'create handoff', 'I need to pause', 'context is getting full', (6) resuming work with 'load handoff', 'resume from', 'continue where we left off'. Proactively suggests handoffs after substantial work (multiple file edits, complex debugging, architecture decisions). Solves long-running agent context exhaustion by enabling fresh agents to continue with zero ambiguity." | `.claude/skills/session-handoff/SKILL.md` |
| skill-creator | Create new skills, modify and improve existing skills, and measure skill performance. Use when users want to create a skill from scratch, edit, or optimize an existing skill, run evals to test a skill, benchmark skill performance with variance analysis, or optimize a skill's description for better triggering accuracy. | `.claude/skills/skill-creator/SKILL.md` |
| web-fetch-mcp-preferred | \| 优先使用 MCP `fetch` 读取网页正文，并在需要 JS 渲染、登录态、点击交互、截图、表单填写或多步网页操作时切换到 `agent-browser`。当用户提到网页抓取、网页摘要、读取 URL、提取文章正文、替代 Claude Code 内置 WebFetch、禁用 WebFetch、抓公开页面文本、网页调研、读取博客文档或从链接整理信息时，应主动使用本 skill。不要把动态网页、登录页或需要点击后内容才出现的页面硬塞给 `fetch`。 | `.claude/skills/web-fetch-mcp-preferred/SKILL.md` |
| writing-plans | Use when you have a spec or requirements for a multi-step task, before touching code | `.claude/skills/writing-plans/SKILL.md` |
| devflow | \| 长期开发任务主入口。适用于中大型开发、长任务推进、跨对话续接、过程记录、方案讨论、计划沉淀、bug 调试、handoff 交接。显式调用时直接使用；当任务涉及多阶段推进、需要记录决策与过程、或预计会跨多轮继续时，也应主动触发本 skill。 一旦进入本 skill，默认把任务过程沉淀到仓库根目录 `.devflow/<mission-slug>/`，并根据任务形态选择轻量路径、重型路径、bug 路径或 resume 路径。不要把记录当成附属说明，记录本身就是主流程的一部分。 | `.cursor/skills/devflow/SKILL.md` |
| skill-creator-cc | Create new skills, modify and improve existing skills, and measure skill performance. Use when users want to create a skill from scratch, edit, or optimize an existing skill, run evals to test a skill, benchmark skill performance with variance analysis, or optimize a skill's description for better triggering accuracy. | `.cursor/skills/skill-creator-cc/SKILL.md` |
<!-- GSD:skills-end -->

<!-- GSD:workflow-start source:GSD defaults -->
## GSD Workflow Enforcement

Before using Edit, Write, or other file-changing tools, start work through a GSD command so planning artifacts and execution context stay in sync.

Use these entry points:
- `/gsd-quick` for small fixes, doc updates, and ad-hoc tasks
- `/gsd-debug` for investigation and bug fixing
- `/gsd-execute-phase` for planned phase work

Do not make direct repo edits outside a GSD workflow unless the user explicitly asks to bypass it.
<!-- GSD:workflow-end -->



<!-- GSD:profile-start -->
## Developer Profile

> Profile not yet configured. Run `/gsd-profile-user` to generate your developer profile.
> This section is managed by `generate-claude-profile` -- do not edit manually.
<!-- GSD:profile-end -->
