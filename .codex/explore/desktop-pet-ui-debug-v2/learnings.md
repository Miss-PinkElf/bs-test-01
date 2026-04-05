# 经验沉淀

## 2026-03-24 第 1 轮迭代

### 有效的做法
- 先从报错栈定位到 `Live2DCanvas.tsx` 和 `useLive2DModel.ts`，能快速区分“生命周期问题”和“视觉比例问题”。
- 在 Electron 窗口 resize 场景里，让 `PIXI.Application` 保持单例，只做 `renderer.resize`，可以显著降低 Live2D 相关异常概率。

### 无效的做法
- 只在 `App.tsx` 里继续叠加 `modelScaleRatio` 这样的外部缩放系数，会让问题更难定位。
- 用模型当前已经缩放后的 `width/height` 再参与 fit 计算，会导致视觉越来越失真。

### 可复用的策略
- Electron + PIXI + Live2D 的窗口缩放场景下，优先保持 `PIXI.Application` 单例，然后只做 renderer resize 和模型重布局。
- Live2D 适配算法需要先提取模型原始尺寸，再基于 viewport 做单次缩放计算。

### 要避免的坑
- 不要让初始化 effect 依赖 `width/height`，否则窗口 resize 会触发 destroy/recreate。
- 不要在多个层次同时维护缩放比例，否则很容易出现“模型越来越大/越来越小”的错觉。

## 2026-03-24 第 2 轮迭代经验

### 有效的做法
- 让 React 订阅真实窗口 `resize` 并重新读取主进程 bounds，能修掉“边框变了但模型和 toolbar 没跟着动”的错位感。
- 先在容器层切出统一模型可视区，再微调 Live2D fit 参数，比单纯继续调 `MODEL_FILL_RATIO` 更稳定。
- 将 toolbar、拖拽把手、缩放手柄和模型可视区都挂到同一套布局 token 上，更容易形成整体缩放感。

### 无效的做法
- 只在右下角自定义缩放手柄回写 bounds，而不监听真实窗口 resize，会留下边框缩放和渲染缩放脱节的问题。
- 只把模型 scale 调大，不重新定义模型绘制区域，通常只能治一点留白，治不了整体联动割裂。

### 可复用的策略
- Electron 桌面宠物这类悬浮窗 UI，应该把“窗口 bounds”“UI token”“模型 viewport”拆成三层，但它们必须共用同一个尺寸源。
- 在不改 PIXI 初始化依赖的前提下，优先通过 renderer resize + 统一 viewport 重算解决视觉联动问题。

### 下次要避免的坑
- 不要把“窗口尺寸变化”和“右下角缩放手柄交互”当成同一个事件来源，它们在渲染层是两条链路。
- 不要把 toolbar 缩放和模型可视区调参分开维护，否则很容易再次出现三套系统的观感。
