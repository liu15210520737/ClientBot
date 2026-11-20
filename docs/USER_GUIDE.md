# 用户指南

## 1. 运行环境

- Minecraft Java Edition **26.2**。
- Java **25**。
- Fabric Loader **0.19.5+**。
- Fabric API **0.161.0+26.2**。
- ClientBot Core。
- 按需安装扩展与对应的目标 Mod；所有目标 Mod 也必须支持 26.2。

核心与扩展均只运行在客户端。把 ClientBot 放进客户端 `mods`；Carpet/Servux 按它们自己的要求装在服务端。QuickShulker 的容器协议需要客户端与服务端配合。

## 2. 第一次启用

远程控制有两个默认保险：`enabled=false` 和操作者列表为空。必须进入世界后在 bot 客户端本地执行：

```text
/clientbot allow ControllerName admin
/clientbot enable
```

建议使用专用控制账号。普通账号使用：

```text
/clientbot allow ControllerName user
```

`user` 不包含 `core.command` 和 `extensions.carpet.admin`。需要精细配置时直接编辑 `config/clientbot/core.json`，然后 `/clientbot reload`。

控制者现在可以发送：

```text
/msg BotName !cb ping
/msg BotName !cb status
```

没有回复时依次检查：

1. bot 客户端日志是否有 `ClientBot 已初始化`。
2. `/clientbot status` 是否启用。
3. 控制者 UUID 是否在 `operators`。
4. 服务器是否把私聊作为玩家聊天包发送；自定义插件私聊见下文。
5. `replyMode` 是否为 `off`。
6. 是否超过 `maxCommandsPerMinute`。

## 3. 普通私聊与插件私聊

默认只处理能识别为 `commands.message.display.incoming` 的玩家私聊消息，发送者来自数据包中的 `GameProfile`，并使用其 UUID 做 ACL。

若服务器插件用系统消息模拟私聊，默认不会接受，因为普通文本没有可靠签名。确认该服务器可信后才设置：

```json
{
  "allowSystemWhispers": true,
  "systemWhisperPatterns": [
    "^\\[(?<sender>[A-Za-z0-9_]{1,16}) whispers to you: (?<command>.*)]$"
  ]
}
```

每条正则必须包含命名分组 `sender` 和 `command`，并匹配整行。ClientBot 会用在线玩家列表把 `sender` 解析成 UUID；查不到就拒绝。不要把公共聊天格式配置成私聊规则。

## 4. 回复方式

`replyMode`：

- `whisper`：默认，通过 `/msg <控制者> [CB] ...` 回复。
- `local`：只在 bot 客户端显示，远程操作者不会收到结果。
- `off`：不回复。

长回复会切分。`list` 等命令自身限制输出数量，避免向服务器刷屏。

## 5. 挂机与渲染节能

```text
!cb render off
!cb render fps 5
```

此模式取消 `GameRenderer.renderLevel` 和 `renderItemInHand`，并将帧率限制为后台值。它不会停止客户端 Tick、网络接收、自动任务或其他 Mod 的逻辑。

恢复：

```text
!cb render on
!cb render normalfps 120
```

若其他渲染 Mod 注入顺序特殊，节能效果可能不同；出现黑屏时先 `render on`，再排查渲染 Mod 兼容性。

## 6. 移动与任务

`walk` 和 `follow` 只控制朝向和移动键，不会计算绕障路线。卡住时：

```text
!cb stop walk
!cb move jump on
!cb move jump off
```

`stop all` 会停止全部任务。断线、退出世界、玩家实体更换时也会自动停止任务并释放移动覆盖。

`after` 示例：

```text
!cb after 100 say "已到 5 秒"
!cb after 200 mine crosshair
!cb tasks
```

每个 Tick 约为 50ms，但卡顿会改变真实耗时。任务在客户端 Tick 中执行，禁止扩展在任务里做阻塞 IO。

## 7. Masa / Sakura 系列

### 投影打印

```text
!cb litematica load build.litematic 100 64 100
!cb litematica list
!cb printer range 4.5
!cb printer interval 8
!cb printer start
!cb printer status
!cb printer stop
```

材料补货、特殊方块朝向、服务器校验仍由 Litematica Printer 负责。ClientBot 调用其公开配置，不重新实现打印算法。

### Tweakeroo 与其他配置

```text
!cb tweakeroo list free
!cb tweakeroo toggle tweakFreeCamera
!cb tweakeroo toggle tweakHandRestock
!cb minihud list structure
!cb itemscroller list
!cb litematicacfg list easyPlace
```

修改只通过目标配置的公开 JSON 读写接口完成。`save` 调用对应 Mod 的 `saveToFile`。少数配置可能要求重新进入世界或由目标 Mod 回调才会生效，这是目标 Mod 自身行为。

## 8. Carpet 与 Servux

Carpet 查询：

```text
!cb carpet query spawnChunkSize
!cb carpet log tps
```

修改规则和假人属于危险操作：

```text
!cb carpetset rule spawnChunkSize 8
!cb confirm <六位码>
!cb carpetset player bot_miner spawn
!cb confirm <六位码>
```

默认假人白名单为 `bot_*`。空数组代表不限制，建议生产环境填写明确白名单。

Servux：

```text
!cb servux sync hud on
!cb servux sync entity on
!cb servux refresh hud
!cb servux status
```

Servux 扩展读取 MiniHUD 的协议状态。服务端没装 Servux 时，状态会明确显示 false，不会伪造数据。

## 9. QuickShulker

```text
!cb qshulker open 0
!cb qshulker close
```

打开潜影盒后，可以配合其他物品栏扩展或未来任务操作容器。当前版本不承诺自动完成复杂容器点击序列。

## 10. 日志与排错

- 客户端日志：搜索 `ClientBot` 和扩展 ID。
- 审计：`config/clientbot/audit.log`，记录时间、UUID、名称、结果和命令。
- 扩展状态：`!cb extensions`。
- 配置错误：核心读取失败时会使用默认值，且不会自动覆盖原文件；修复 JSON 后 `/clientbot reload`。

提交问题时附 Minecraft/Fabric/ClientBot/目标 Mod 版本、复现命令、相关日志；移除服务器地址和其他隐私信息。
