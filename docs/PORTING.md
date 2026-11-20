# 移植指南

目标是将核心与每个扩展独立移植，避免一次版本升级牵一发动全身。

## 1. 版本分支策略

建议：

```text
main          # 当前稳定线，26.2
dev/26.3      # 下一版本开发
port/1.21.11  # 回填分支
```

发布标签：`v0.1.0+26.2`。不要把多个 Minecraft 版本的 JAR 混称为同一构建。

## 2. 升级顺序

1. 在 `gradle.properties` 更新 Minecraft、Loader、Loom、Fabric API。
2. 更新 Gradle wrapper 到目标 Fabric 示例所推荐版本。
3. 先只构建 `:core`，解决 Mojang 映射和 Fabric 事件变化。
4. 再构建无第三方编译依赖的 Carpet、Servux、Tweakeroo、MiniHUD、Item Scroller。
5. 更新 MaLiLib、Litematica、Printer、QuickShulker 坐标，逐个构建直接联动。
6. 完成运行时验证后再改 README 的兼容声明。

## 3. 核心检查点

- `ClientReceiveMessageEvents` 的签名、`ChatType.Bound` 和 `TranslatableContents`。
- `/clientbot` 的 Client Command API 注册签名。
- `LocalPlayer.input.keyPresses` 与 `Input` record 字段顺序。26.3 的输入路径可能和 26.2 不同，不能凭旧版本猜测。
- `Minecraft.gameMode` 的挖掘、攻击、使用方法。
- `GameRenderer.renderLevel/renderItemInHand` 的 Mixin 方法是否仍存在。
- `options.framerateLimit()` 是否仍是当前帧率入口。
- `Inventory` 槽位 API 和 QuickShulker 菜单槽映射。

## 4. 反射适配器检查点

`ReflectedConfig` 刻意只依赖 MaLiLib 兼容公开方法。目标版本若包名改变，只需调整各扩展的 root class 字符串；若配置接口改变，应新增版本适配器，不要在业务命令中散落的反射代码。

Servux 扩展检查：

- `DataStorage.getInstance/hasServuxServer/getServuxVersion`。
- TPS/MSPT getter。
- `HudDataManager` 的 metadata、recipe、spawn 方法。
- MiniHUD 的 `hudDataSync/entityDataSync` 配置名。

## 5. Litematica / Printer

- `LitematicaSchematic.createFromFile`。
- `SchematicPlacement.createFor`。
- `SchematicPlacementManager` 的 add/list/remove/selected 方法。
- Printer `Configs` 字段名和取值范围。
- 若 Printer 更换维护者或包名，新增独立扩展或清晰的适配层，不静默把不兼容实现当成同一版本。

## 6. QuickShulker

- 目标 Mod ID 是否仍为 `quickshulker`。
- `ClientUtil.CheckAndSend` 与协议包是否公开。
- 主手/副手/背包槽映射。
- `ConfigOptions` 字段和保存方法。
- QuickShulker Multi 与原版分支不能仅凭 Mod ID 假定 API 一致，应分别运行时测试。

## 7. 测试矩阵

每个目标版本至少覆盖：

| 场景 | 预期 |
|---|---|
| 仅核心启动 | 无扩展也正常进入世界 |
| 扩展存在、目标 Mod 缺失 | 扩展显示 skipped，核心不崩 |
| 目标 Mod 存在 | 扩展 loaded，命令可用 |
| 未授权私聊 | 不执行 |
| user 执行管理命令 | 拒绝 |
| 危险命令无确认 | 不执行 |
| 确认码过期/跨用户 | 拒绝 |
| 断线时有任务 | stop 被调用，移动清理 |
| render off/on | Tick 持续，画面可恢复 |
| 打印/开箱 | 与目标 Mod、服务端协议共同验证 |

## 8. 发布说明

每次移植记录：实际测试版本、目标 Mod 版本、未验证项和已知行为变化。不要把“编译成功”写成“所有功能运行时验证通过”。
