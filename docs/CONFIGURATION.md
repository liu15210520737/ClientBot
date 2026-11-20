# 配置说明

## 核心：`config/clientbot/core.json`

首次启动自动生成。推荐通过本地命令完成授权，手工编辑后执行 `/clientbot reload`。

完整示例：

```json
{
  "enabled": false,
  "commandPrefix": "!cb",
  "replyMode": "whisper",
  "maxCommandsPerMinute": 12,
  "confirmationSeconds": 30,
  "allowSystemWhispers": false,
  "auditLog": true,
  "systemWhisperPatterns": [
    "^\\[(?<sender>[A-Za-z0-9_]{1,16}) whispers to you: (?<command>.*)]$",
    "^(?<sender>[A-Za-z0-9_]{1,16}) whispers to you: (?<command>.*)$"
  ],
  "blockedServerCommands": [
    "stop", "op", "deop", "ban", "ban-ip", "pardon", "kick", "whitelist"
  ],
  "operators": [
    {
      "uuid": "00000000-0000-0000-0000-000000000000",
      "name": "ControllerName",
      "permissions": ["*"]
    }
  ],
  "rendering": {
    "suppressWorld": false,
    "suppressHand": false,
    "limitFps": true,
    "backgroundFps": 5,
    "normalFps": 120
  }
}
```

上面的全零 UUID 只是格式示例，不能使用；用 `/clientbot allow` 获取真实 UUID。

### 字段

| 字段 | 默认 | 范围/说明 |
|---|---:|---|
| `enabled` | `false` | 总开关；本地启用 |
| `commandPrefix` | `!cb` | 指令前缀；空值会恢复默认 |
| `replyMode` | `whisper` | `whisper/local/off` |
| `maxCommandsPerMinute` | `12` | 每个 UUID 每分钟 1-120 条；超限时静默丢弃 |
| `confirmationSeconds` | `30` | 危险确认有效期，限制为 5-300 秒 |
| `allowSystemWhispers` | `false` | 是否信任可配置的系统消息格式 |
| `auditLog` | `true` | 记录成功、拒绝和异常 |
| `systemWhisperPatterns` | 英文 vanilla 两种形式 | 必须有 `sender`、`command` 命名分组 |
| `blockedServerCommands` | 见示例 | 仅约束 `cmd` 的根命令；Carpet 另有白名单 |
| `operators` | `[]` | UUID、备注名和权限列表 |
| `rendering.suppressWorld` | `false` | 取消世界渲染 |
| `rendering.suppressHand` | `false` | 取消手部渲染；`render` 命令会与世界一起设置 |
| `rendering.limitFps` | `true` | 是否应用后台/正常帧率 |
| `rendering.backgroundFps` | `5` | 限制为 1-60 |
| `rendering.normalFps` | `120` | 限制为 5-1000 |

### 权限匹配

- `*`：全部。
- `core.status`：精确节点。
- `extensions.*`：匹配该前缀下所有节点，包括管理节点，不应随意授予普通用户。
- 列表为 OR，不支持 `!permission` 否定语法。
- 本地 `/clientbot run` 不检查远程 ACL。

推荐普通控制者：

```json
{
  "permissions": [
    "core.status",
    "core.movement",
    "core.interact",
    "core.task",
    "extensions.litematica",
    "extensions.printer",
    "extensions.tweakeroo"
  ]
}
```

## Carpet：`config/clientbot/carpet.json`

```json
{
  "allowedRules": [
    "spawnChunkSize",
    "accurateBlockPlacement"
  ],
  "allowedFakePlayers": ["bot_*"],
  "allowTickCommands": false,
  "maxCommandLength": 200
}
```

- `allowedRules`：允许通过 `carpetset rule` 修改的规则；末尾 `*` 为前缀通配；空列表为不限制。
- `allowedFakePlayers`：允许控制的假人名；默认只允许 `bot_` 开头。
- `allowTickCommands`：是否允许 `/tick`；默认关闭。
- `maxCommandLength`：拒绝过长命令，并始终拒绝换行。

只读的 `carpet query` 不受 `allowedRules` 限制；修改仍需要 `extensions.carpet.admin` 和危险确认。

## Masa 配置

这些扩展不复制目标 Mod 的 JSON，而是直接修改运行中的公开配置对象。`save` 调用目标 Mod 自己的保存逻辑，因此文件位置和格式由目标 Mod 决定。

## QuickShulker 配置

`qshulker set` 修改目标 Mod 自身的布尔字段并调用其 `ConfigManager.save()`。只读 `list` 可以查看准确字段名。

## 扩展开关

当前“开关扩展”的单位是 JAR：在 `mods` 中添加/删除对应扩展，重启客户端。这样可以保证不同扩展的类和可选依赖完全隔离。运行时开关具体功能使用：

```text
!cb integrations list
!cb integrations set <ID> off
```

并非所有扩展功能都有运行时总开关；没有注册到 `integrations` 的命令仍受其权限节点控制。
