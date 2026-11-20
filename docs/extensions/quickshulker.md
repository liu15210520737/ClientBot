# QuickShulker 扩展

文件：`clientbot-quickshulker-0.1.0.jar`

## 依赖与兼容目标

Core 和 QuickShulker。开发基线为 MoRanpcy/kyrptonaught API 的 QuickShulker 3.1.0-26.2，Mod ID `quickshulker`。

QuickShulker Multi 可能共享 Mod ID 或协议，但不能仅凭名称保证类 API 相同；使用其他分支时应按 `!cb extensions` 和实际开箱结果单独验证。

## 命令

- `qshulker open <0-35>`：打开主背包/快捷栏中的可快速开启物品。
- `qshulker open offhand`：打开副手物品。
- `qshulker close`：关闭当前容器。
- `qshulker list`：列出目标 Mod 的公开布尔配置。
- `qshulker get/set <配置>`：读取/修改并保存配置。

权限：`extensions.quickshulker`。

统一开关：`quickshulker.rightClickToOpen`、`quickshulker.keybind`。

## 槽位转换

用户输入背包索引：0-8 为快捷栏，9-35 为主背包，offhand 为副手。扩展转换为 QuickShulker 客户端协议使用的菜单槽 36-44、9-35、45，再调用 `ClientUtil.CheckAndSend`。

## 限制

- 物品必须通过 QuickShulker 自己的 `Util.isOpenableItem` 检查。
- 服务端必须安装并允许相应 QuickShulker 功能；仅客户端有扩展无法打开服务端容器。
- 当前只负责打开/关闭，不自动执行潜影盒内取放。
- 配置中的快捷容器类型可能需要重进世界才能改变注册行为，这是 QuickShulker 自身生命周期决定。
