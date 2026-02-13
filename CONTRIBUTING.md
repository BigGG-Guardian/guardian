# 贡献指南

感谢你对 Guardian 项目的关注！欢迎任何形式的贡献：Bug 报告、功能建议、代码提交、文档完善。

## 如何贡献

### 1. 提交 Issue

- **Bug 报告**：请使用 Bug Report 模板，提供复现步骤、预期行为、实际行为
- **功能建议**：请使用 Feature Request 模板，描述使用场景和期望效果

### 2. 提交代码（Pull Request）

1. **Fork** 本仓库到你的 GitHub 账号
2. 基于 `master` 分支创建你的功能分支：`git checkout -b feature/xxx`
3. 完成开发并确保本地编译通过：`mvn clean compile`
4. 提交代码，commit message 请遵循下方规范
5. 推送到你的 Fork 仓库：`git push origin feature/xxx`
6. 创建 Pull Request 到本仓库的 `master` 分支

### 3. Commit Message 规范

格式：`<type>: <subject>`

| type | 说明 |
|------|------|
| `feat` | 新功能 |
| `fix` | Bug 修复 |
| `docs` | 文档变更 |
| `refactor` | 重构（不新增功能、不修复 Bug） |
| `test` | 测试相关 |
| `chore` | 构建/工具/依赖变更 |

示例：

```
feat: 新增接口限流模块
fix: 修复 context-path 场景下 URL 匹配失败
docs: 完善 README 响应模式说明
refactor: 提取 stripContextPath 到工具类
```

## 开发环境

- JDK 1.8+
- Maven 3.6+
- Spring Boot 2.7.x
- Redis 5.0+（运行示例项目时需要）

## 代码规范

- 所有 public 类和方法必须有 Javadoc 注释
- 新增功能请同步更新 `guardian-example` 示例工程
- 配置项统一使用 `guardian.repeat-submit.*` 前缀（后续模块使用对应前缀）

## 分支说明

| 分支          | 说明 |
|-------------|------|
| `master`    | 主分支，Spring Boot 2.7.x，稳定代码 |
| `feature/*` | 功能开发分支 |
| `fix/*`     | Bug 修复分支 |

## 许可证

提交代码即表示你同意将代码以 [Apache License 2.0](LICENSE) 许可证授权。
