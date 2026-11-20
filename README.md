# ClientBot

ClientBot 是一个面向 **Minecraft Java Edition 26.2 / Fabric / Java 25** 的客户端自动化框架。它把当前登录的真实玩家变成可远程控制的“bot”：被授权的玩家通过游戏内私聊发送指令，客户端在本地执行移动、交互、任务和其他 Mod 的公开功能。

项目采用 **核心 Mod + 独立扩展 Mod** 的结构。核心不包含对某一第三方 Mod 的强依赖；每个联动都能单独添加或删除。

> 请只在服务器规则允许、且你拥有相应权限时使用。自动打印、持续攻击、假人和 Carpet 规则修改可能触发反作弊或影响其他玩家。

## 功能

- 私聊指令：默认前缀 `!cb`，回复通过 `/msg` 返回。
- 默认安全：远程控制默认关闭；无操作者；只认 UUID；本地命令才能授权/启用。
- 权限节点：精确权限和 `foo.*` 通配符；危险指令需要一次性确认码。
- 防护：每分钟频率限制、服务器命令黑名单、审计日志、系统私聊解析默认关闭。
- 玩家动作：聊天、私聊、服务器命令、视角、移动键、直线行走、跟随、快捷栏、丢弃、使用、攻击、定点挖掘。
- 任务：合作式客户端 Tick 任务、互斥组、延迟执行、断线/换玩家时清理。
- 渲染节能：跳过世界与手部渲染并降低 FPS；网络、客户端 Tick 和任务继续运行。
- 其他 Mod 开关：统一 `integrations` 注册表。
- Masa 配置：枚举、查询、修改、切换和保存公开的 MaLiLib 配置项。
- 扩展故障隔离：缺少目标 Mod 或扩展初始化失败时，核心和其他扩展继续运行。

## 模块

| 模块 | JAR | 目标 Mod 是否必须安装 | 主要能力 |
|---|---|---:|---|
| Core | `clientbot-core` | 仅 Fabric API | 私聊控制、ACL、任务、动作、渲染、扩展 API |
| Carpet | `clientbot-carpet` | 否；Carpet 在服务端 | 规则查询/修改、假人、log、tick |
| Servux | `clientbot-servux` | 需要 MiniHUD；Servux 在服务端 | 握手状态、TPS/MSPT、HUD/实体同步、刷新 |
| Litematica | `clientbot-litematica` | MaLiLib + Litematica | 原理图加载/放置/卸载、EasyPlace、全部公开配置 |
| Printer | `clientbot-printer` | 上述依赖 + Litematica Printer | 自动打印、范围、速度、旋转、配置 |
| Tweakeroo | `clientbot-tweakeroo` | Tweakeroo | FeatureToggle、Configs 全部公开配置 |
| MiniHUD | `clientbot-minihud` | MiniHUD | Info/Renderer/Structure/Generic 全部公开配置 |
| Item Scroller | `clientbot-itemscroller` | Item Scroller | 全部公开配置与布尔开关 |
| QuickShulker | `clientbot-quickshulker` | QuickShulker | 远程开箱、关闭容器、配置与协议发送 |
| Example | `clientbot-example-extension` | 否 | 扩展开发示例 |

## 快速开始

1. 安装 Java 25、Fabric Loader 0.19.5+、Fabric API 0.161.0+26.2。
2. 把核心 JAR 放入 `mods`。只添加实际需要的扩展及其目标 Mod。
3. 进入服务器，在**被控制的 bot 客户端**执行：

   ```text
   /clientbot allow 控制者游戏名 admin
   /clientbot enable
   ```

   控制者必须在线，以便本地命令解析并记录真实 UUID。普通权限可使用 `user` 代替 `admin`。

4. 控制者发送：

   ```text
   /msg Bot游戏名 !cb status
   /msg Bot游戏名 !cb render off
   /msg Bot游戏名 !cb help
   ```

5. 本地测试不需要私聊：

   ```text
   /clientbot run status
   /clientbot run tweakeroo list freeCamera
   ```

详细安装与安全配置见 [用户指南](docs/USER_GUIDE.md)。

## 常用指令

```text
!cb status
!cb walk 100 64 -20
!cb follow PlayerName 3
!cb mine crosshair
!cb stop all
!cb litematica load house.litematic 100 64 100
!cb printer range 4.5
!cb printer start
!cb tweakeroo toggle tweakFreeCamera
!cb qshulker open 0
!cb carpet query spawnChunkSize
!cb servux status
!cb integrations list
!cb render off
```

完整说明和权限节点见 [命令参考](docs/COMMANDS.md)。

## 构建

```bash
gradle build
```

产物位于各模块的 `build/libs/`。需要 JDK 25 和 Gradle 9.5.1；项目使用 Loom 1.17 和 Mojang 官方映射。完整源码包也提供 Gradle wrapper，可使用 `./gradlew build`；如果检出内容没有 wrapper，可执行 `gradle wrapper --gradle-version 9.5.1` 生成。

单独构建：

```bash
./gradlew :core:build :extensions:printer:build
# 没有 wrapper 时：gradle :core:build :extensions:printer:build
```

## 文档索引

- [用户指南](docs/USER_GUIDE.md)
- [命令参考](docs/COMMANDS.md)
- [配置说明](docs/CONFIGURATION.md)
- [安全模型](docs/SECURITY.md)
- [扩展 API 与开发指南](docs/EXTENSION_API.md)
- [移植指南](docs/PORTING.md)
- [测试与验证](docs/TESTING.md)
- [各扩展文档](docs/extensions/)

## 当前边界

- `walk`/`follow` 是直线移动，不是 Baritone 式绕障寻路；复杂地形可能卡住。
- `mine` 是单目标持续挖掘，不是区域自动采矿。
- Masa 配置适配器依赖公开配置对象的 MaLiLib 兼容方法；目标 Mod 大改 API 时可能需要适配器更新。
- QuickShulker 打开容器要求服务端也支持对应协议，仅装客户端不能绕过服务端。
- Servux 和 Carpet 主要是服务端 Mod；扩展不会让不存在的服务端能力凭空出现。
- 渲染抑制不是窗口最小化、虚拟显示或完整无头客户端，HUD/界面仍可能消耗少量资源。

## 许可证

MIT。第三方 Mod、协议与商标归各自作者所有；本项目不捆绑其 JAR。
