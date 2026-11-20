# 参与开发

## 环境

- JDK 25
- Minecraft Java Edition 26.2
- Fabric Loader 0.19.5+
- Gradle wrapper（9.5.1）

执行全量构建和测试：

```bash
gradle clean build
```

完整源码包提供 wrapper，也可使用 `./gradlew clean build`。如果当前平台没有保留脚本执行权限，可使用 `bash ./gradlew clean build`。

## 修改原则

1. 核心只保留通用命令、任务、权限、渲染和扩展 API，不加入某个可选 Mod 的强依赖。
2. 新联动放入独立 `extensions/<name>` 模块，并同时实现 Java `requiredMods()` 与 `fabric.mod.json` 的 `clientbot:requires`。
3. 第三方 JAR 使用 `compileOnly`，不要捆绑、重打包或复制其代码。
4. 远程控制的新增修改操作应使用独立权限节点；高风险操作增加 `dangerous()`，不要绕过 ACL 或确认流程。
5. 任务必须非阻塞，并在 `stop()` 中释放按键、关闭模式或还原状态。
6. 对公开 API 做破坏性调整时，更新 `clientbot:api_version`、扩展示例和迁移文档。

## 提交前检查

- `./gradlew clean build` 成功。
- 新增权限、参数解析、配置或任务逻辑有对应单元测试。
- 新扩展具有 `docs/extensions/<name>.md`，并更新命令参考和用户指南。
- 区分编译验证与真实客户端/服务器运行时验证，不把未测试功能标为已验证。
- 不提交 `.gradle/`、`build/`、`run/`、本地配置、审计日志或账号信息。

## 移植或新增扩展

分别阅读 [移植指南](docs/PORTING.md) 和 [扩展 API 与开发指南](docs/EXTENSION_API.md)。优先从 `examples/example-extension` 复制最小结构，再按目标版本的真实源码和签名调整，不凭旧版本经验猜测 API。
