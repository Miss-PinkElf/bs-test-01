# 协作约束

## 工具约束

## 1. 输出与语言

1. 必须始终使用简体中文。
2. 产出的文档默认使用简体中文，禁止默认输出英文。
3. 包括但不限于 spark-workflow、handoff、openspec、skills 相关文档，默认都使用简体中文。

## 2. 提交流程

1. 每次完成代码修改后，必须先询问我是否需要提交代码。
2. 没有我的明确允许，不能执行 commit。
3. 如果需要 commit，提交信息必须使用中文。

## 3. 工作流要求

1. 默认使用 `context-budget-explore` 进行探索、记录和推进。
2. 在走 `context-budget-explore` 流程时，需要进入其中的 `spark-workflow` 做需求对齐。
3. 在进入实现前，先进行一次头脑风暴，和我讨论方案，不要跳过讨论直接改代码。
4. 需要顺手判断：这次需求是否需要补充或更新相关文档。

## 4. 文档与计划落盘要求

1. plan 必须创建在 `zzz-doc/zzz-prompt-debug/plan` 目录下。
2. plan 文件名必须与本次需求强相关，便于后续查找。
3. 除特殊说明外，过程文档、计划文档都放在 `zzz-doc` 目录下。
4. 更新问题清单时，必须写清楚：
   - 问题现象
   - 问题原因
   - 解决方案

## 5. 路径与环境要求

1. 本仓库内涉及文件路径时，必须使用相对路径。

## 6. 校验与改动边界

1. 不需要做全局 ESLint 校验。
2. 不影响运行的 TypeScript 报错可以先不处理。
3. 如果要顺手修改 TypeScript 错误，必须先征求我的确认。

---

# 开发规范

## 1. 代码风格

1. 可读性优先。
2. 修改 React / TSX 代码时，优先参考现有代码风格保持一致。
3. 需要使用 `react-tsx-readability-guard` 提升 React / TSX 代码可读性。

## 2. 组件库

1. 默认使用 Ant Design（Antd）。

## 3. 样式规范

1. 默认不要使用行内样式。
2. 默认使用 CSS Module，推荐 `less` 或 `sass`。
3. 样式结构默认采用“外层包裹 + 内层 className”的写法。

### CSS Module 示例

```css
.wrapper {
  padding: 20px;
  background: #f5f5f5;

  :global {
    .user-info {
      .user-name {
      }
    }
  }
}
```

```tsx
<div className={styles.wrapper}>
  <div className="user-info">
    <span className="user-name">张三</span>
  </div>
</div>
```

### CSS-in-JS 示例

```tsx
export const DetailDiv = styled.div`
  .user-info {
    .user-name {
    }
  }
`

<DetailDiv>
  <div className="user-info">
    <div className="user-name" />
  </div>
</DetailDiv>
```