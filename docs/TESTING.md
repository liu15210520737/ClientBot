# 测试与验证

## 已完成的自动验证

验证日期：2026-11-20。

环境：Minecraft 26.2、Java 25.0.5.1、Gradle 9.5.1、Fabric Loom 1.17.21、Fabric Loader 0.19.5、Fabric API 0.161.0+26.2。

执行：

```bash
./gradlew clean build
```

结果：**BUILD SUCCESSFUL**。核心、八个功能扩展和示例扩展均生成主 JAR 与 sources JAR。

### 单元测试

共 **23 个测试，0 失败、0 错误、0 跳过**，分属 6 个测试类：

- `CommandDispatcherTest`：引号参数、远程总开关、危险确认、跨 UUID 确认、一次性确认、确认时重新检查权限。
- `CommandContextTest`：整数/浮点/布尔参数、剩余文本、回复路由、缺少玩家时的错误。
- `CommandRegistryTest`：大小写无关查询、重名拒绝、缺失 handler 拒绝。
- `BotConfigTest`：默认安全状态、精确权限、作用域通配符、普通用户不自动获得管理权限。
- `ConfigManagerTest`：保存/读取、数值和空集合修复、配置对象身份保持、损坏文件不覆盖。
- `TaskManagerTest`：互斥组、同 ID 替换、任务完成、异常停止、全部停止。

### 产物静态检查

- 10 个主 JAR 的 `fabric.mod.json` 均可解析，版本替换为 `0.1.0`。
- 所有声明的入口点类都存在于对应 JAR。
- 核心包含 Mixin 类，所有扩展包含各自声明的依赖元数据。
- 扩展 JAR 未捆绑 MaLiLib、Litematica、Printer 或 QuickShulker 的类。
- 26.2 `GameRenderer` 中已核对 `renderLevel(DeltaTracker)` 与 `renderItemInHand(CameraRenderState, float, Matrix4fc)` 目标签名。

## 尚未完成的运行时验证

当前环境没有完成真实 Minecraft 客户端启动、账号登录和服务器连接。因此以下项目**不能仅凭编译成功视为已验证**：

- 私聊包在 vanilla 与各类服务端插件下的识别和回复。
- `/clientbot` 在真实游戏内的注册与授权流程。
- 渲染抑制与目标渲染管线的兼容性，以及恢复渲染后的画面状态。
- Carpet/Servux 的服务端握手和权限组合。
- Litematica 原理图加载、Printer 实际放置方块。
- QuickShulker 的真实协议开箱和容器行为。
- 各 Masa 配置项修改后是否全部被目标 Mod 的运行逻辑读取。
- 断线、换世界、玩家实体重建时所有第三方任务的清理。
- GitHub Actions 的远端运行结果。

## 建议的人工测试矩阵

先在测试客户端和测试服进行，每台客户端只启用本组所需扩展。

| 场景 | 步骤 | 预期 |
|---|---|---|
| 仅核心启动 | 只安装 Fabric API 与 Core，进入单人世界 | 无扩展依赖错误，`/clientbot status` 可用 |
| 可选依赖缺失 | 安装全部 ClientBot 扩展，不安装目标 Mod | 目标扩展显示 skipped，核心和其他扩展继续工作 |
| 默认拒绝 | 不执行本地启用，发送 `!cb status` | 无动作、无回复 |
| UUID 授权 | 本地 `allow` 后由控制者私聊 | 授权 UUID 可执行，未授权 UUID 不执行 |
| 普通权限 | 使用 `user`，尝试 `carpetset` | 管理节点被拒绝 |
| 危险确认 | 远程执行 `drop` 或 `cmd` | 首次只生成确认码；正确码执行一次；他人或过期码失败 |
| 配置重载 | 修改前缀、回复模式和权限后 `reload` | 已初始化的服务立即使用新值 |
| 移动清理 | `walk` 后断线、换世界、`stop all` | 任务停止，不遗留移动键 |
| 渲染开关 | 执行 `render off`，等待后 `render on` | 世界/手部渲染关闭时 Tick 继续，恢复后画面正常 |
| Litematica/Printer | 加载小型测试投影并打印 | 投影位置正确，打印启停和配置保存生效 |
| QuickShulker | 客户端与服务端安装兼容版本后 `qshulker open` | 打开正确槽位容器，无权限时由服务端拒绝 |
| Carpet/Servux | 连接安装对应服务端 Mod 的测试服 | 握手状态、查询和同步与真实服务端能力一致 |

人工验证后应在此文件记录实际客户端版本、目标 Mod 版本、服务端类型、测试日期和结果。发现问题时优先按模块隔离，不要一次引入全部扩展。
