# Carpet 扩展

文件：`clientbot-carpet-0.1.0.jar`

## 依赖与原理

客户端只需要 Core 和本扩展。Carpet 是服务端 Mod；本扩展通过当前玩家的连接发送 `/carpet`、`/player`、`/log`、`/tick`，不会在客户端模拟 Carpet。

`carpet status` 会尝试读取 MiniHUD 或 Tweakeroo 的 Carpet 握手状态；两者都不存在时返回“未知”，但查询命令仍可发送。

## 命令与权限

- `carpet status/query/log`：`extensions.carpet`。
- `carpetset rule/player/tick`：`extensions.carpet.admin`，危险确认。

详见 [命令参考](../COMMANDS.md#carpet)。

## 配置

`config/clientbot/carpet.json` 在首次加载时生成。`allowedRules` 和 `allowedFakePlayers` 支持末尾 `*` 前缀匹配；空数组代表不限制。默认假人白名单 `bot_*`，默认禁止 tick。

## 示例

```text
!cb carpet query accurateBlockPlacement
!cb carpet log tps
!cb carpetset rule accurateBlockPlacement true
!cb confirm 123456
!cb carpetset player bot_builder spawn at 100 64 100 facing 0 0 in minecraft:overworld
!cb confirm 654321
```

## 限制

当前玩家必须拥有服务端要求的权限。ClientBot 不绕过 OP/LuckPerms。规则名、假人动作和可用 logger 由服务端 Carpet/Carpet Extra 版本决定。修改世界性能相关规则前应先在测试服验证。
