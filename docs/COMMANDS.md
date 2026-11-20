# 命令参考

远程形式均为 `/msg <Bot> !cb <命令>`。为节省篇幅，下文省略 `/msg <Bot> !cb`。本地测试可用 `/clientbot run <命令>`。

参数 `<必填>`、`[可选]`；布尔值接受 `on/off`、`true/false`、`1/0`。带引号的参数会按一个参数处理，例如 `say "hello world"`。

## 核心

| 命令 | 权限 | 说明 |
|---|---|---|
| `help [过滤]` | `core.status` | 列出命令；一次最多显示 25 条 |
| `ping` | `core.status` | 检查控制链路 |
| `status` | `core.status` | 玩家、生命、位置、维度、渲染和任务状态 |
| `mods [过滤]` | `core.status` | 列出客户端 Mod，最多显示 30 个 |
| `extensions` | `core.status` | 查看扩展加载/跳过/失败原因 |
| `integrations list` | `core.render` | 查看所有扩展注册的统一开关 |
| `integrations set <ID> <on/off>` | `core.render` | 设置统一开关 |
| `render on/off/toggle` | `core.render` | 开关世界和手部渲染抑制 |
| `render fps <1-60>` | `core.render` | 设置渲染抑制时帧率 |
| `render normalfps <5-1000>` | `core.render` | 设置正常帧率 |
| `say <内容>` | `core.chat` | 发送普通聊天 |
| `whisper <玩家> <内容>` | `core.chat` | 发送 `/msg` |
| `cmd <服务器命令>` | `core.command` | **危险**；执行服务器命令，受黑名单与确认保护 |
| `look <yaw> <pitch>` | `core.movement` | 设置视角 |
| `move <键> <on/off>` | `core.movement` | 持续覆盖移动键；键为 forward/back/left/right/jump/sneak/sprint |
| `move clear` | `core.movement` | 清除所有移动覆盖 |
| `walk <x> <y> <z> [容差]` | `core.movement` | 直线行走；不是绕障寻路 |
| `follow <玩家> [距离]` | `core.movement` | 跟随当前可见实体列表中的同名玩家 |
| `slot <0-8>` | `core.inventory` | 切换快捷栏 |
| `inventory` | `core.inventory` | 返回非空快捷栏物品 |
| `drop [true/false]` | `core.inventory` | **危险**；丢弃整组/一个当前物品 |
| `use [ticks]` | `core.interact` | 发起一次主手使用动作，并保持任务若干 Tick |
| `attack once` | `core.interact` | 攻击准星实体一次 |
| `attack start [间隔Tick]` | `core.interact` | 持续攻击准星实体 |
| `attack stop` | `core.interact` | 停止攻击任务 |
| `mine crosshair` | `core.interact` | 挖掘准星方块 |
| `mine <x> <y> <z>` | `core.interact` | 挖掘指定方块，距离过远或变空气会结束 |
| `mine stop` | `core.interact` | 停止挖掘 |
| `tasks` | `core.task` | 列出任务 |
| `stop <任务ID>` | `core.task` | 停止指定任务 |
| `stop all` | `core.task` | 停止全部任务并清理移动 |
| `after <ticks> <命令>` | `core.task` | 延迟执行；再次检查权限和危险确认 |

## 本地管理命令

这些命令只在 bot 客户端聊天栏输入，不开放给远程操作者：

| 命令 | 说明 |
|---|---|
| `/clientbot enable` | 启用远程控制 |
| `/clientbot disable` | 禁用远程控制 |
| `/clientbot status` | 本地状态 |
| `/clientbot allow <在线玩家> <admin/user>` | 记录玩家 UUID 并授权；admin 为 `*`，user 为安全默认集合 |
| `/clientbot deny <UUID>` | 撤销授权 |
| `/clientbot reload` | 重载核心 JSON |
| `/clientbot render <true/false>` | 本地开关渲染抑制 |
| `/clientbot run <命令>` | 以本地身份执行，绕过远程 ACL；仅用于本机测试 |

## 危险命令确认

远程执行 `cmd`、`carpetset`、`drop` 时，会返回六位确认码：

```text
!cb cmd gamerule keepInventory true
!cb confirm 123456
```

确认码默认 30 秒过期，只能由发起命令的 UUID 使用，且只能使用一次。`after` 延迟到危险命令时会重新要求确认。

## Masa 通用命令

`tweakeroo`、`minihud`、`itemscroller`、`litematicacfg`、`printercfg` 共享以下子命令：

```text
<根命令> status
<根命令> list [名称过滤]
<根命令> get <配置名>
<根命令> set <配置名> <JSON值>
<根命令> toggle <布尔配置名>
<根命令> save
```

示例：

```text
!cb tweakeroo list camera
!cb tweakeroo get tweakFreeCamera
!cb tweakeroo toggle tweakFreeCamera
!cb tweakeroo set freeCameraMovementSpeed 0.5
!cb tweakeroo save
!cb minihud toggle structureBoundingBoxes
```

`set` 使用目标配置的 JSON 表达：布尔值写 `true`，数字直接写数字，字符串建议写 JSON 字符串（如 `"minecraft:stick"`）。某些 `ConfigBooleanHotkeyed` 的 JSON 还包含热键状态；优先使用 `toggle`，需要完整对象时先用 `get` 查看结构。

## 专用扩展命令

### Carpet

```text
!cb carpet status
!cb carpet query <规则>
!cb carpet log <logger> [参数]
!cb carpetset rule <规则> <值>
!cb carpetset player <假人名> <动作...>
!cb carpetset tick <参数...>
```

`carpetset` 权限为 `extensions.carpet.admin`，需要危险确认，并受 `carpet.json` 白名单限制。

### Servux

```text
!cb servux status
!cb servux sync hud on
!cb servux sync entity on
!cb servux refresh hud
!cb servux refresh structures
!cb servux refresh recipes
!cb servux save
```

### Litematica

```text
!cb litematica load <schematics内文件> [x y z]
!cb litematica list
!cb litematica unload [放置名称]
!cb litematica easyplace on
!cb litematica render off
```

文件路径被限制在 `.minecraft/schematics` 内；不接受 `../` 越界。没有坐标时以玩家当前方块位置为原点。

### Printer

```text
!cb printer start
!cb printer stop
!cb printer toggle
!cb printer status
!cb printer range <2.5-5>
!cb printer interval <1-40>
!cb printer rotate <on/off>
```

开始打印前先用 `litematica load` 建立投影放置。实际能否打印取决于材料、距离、游戏规则、反作弊和 Printer 本身支持。

### QuickShulker

```text
!cb qshulker open <0-35>
!cb qshulker open offhand
!cb qshulker close
!cb qshulker list
!cb qshulker get <布尔配置>
!cb qshulker set <布尔配置> <on/off>
```

背包槽 0-8 为快捷栏，9-35 为主背包；扩展会转换为 QuickShulker 协议所需菜单槽位。
