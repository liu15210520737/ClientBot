# Tweakeroo 扩展

文件：`clientbot-tweakeroo-0.1.0.jar`

## 依赖

Core、Tweakeroo 及其 MaLiLib 依赖。扩展使用运行时反射适配公开配置，不把 Tweakeroo 类链接进核心。

## 覆盖范围

扫描：

- `fi.dy.masa.tweakeroo.config.Configs`，包括 Generic、Disable 等公开内部配置。
- `fi.dy.masa.tweakeroo.config.FeatureToggle` 枚举。

因此可通过同一命令控制公开的普通配置、禁用项和功能开关，而不是只写死 Free Camera 等少数功能。

## 示例

```text
!cb tweakeroo status
!cb tweakeroo list camera
!cb tweakeroo toggle tweakFreeCamera
!cb tweakeroo toggle tweakHandRestock
!cb tweakeroo toggle tweakHoldUse
!cb tweakeroo get freeCameraMovementSpeed
!cb tweakeroo set freeCameraMovementSpeed 0.5
!cb tweakeroo save
```

权限：`extensions.tweakeroo`。

所有布尔项也注册为 `tweakeroo.<配置名>` 统一开关，可用 `integrations list` 查看。

## 限制

配置名以目标 Mod 运行时公开名称为准。某些选项修改后依赖 Tweakeroo 回调、玩家状态或目标 Mixin 才能生效。带热键的配置可能使用数组/对象 JSON，优先用 `toggle` 而不是覆盖整个 JSON。
