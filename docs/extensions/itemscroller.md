# Item Scroller 扩展

文件：`clientbot-itemscroller-0.1.0.jar`

## 依赖

Core、Item Scroller 及其 MaLiLib 依赖。

## 功能

扫描 `fi.dy.masa.itemscroller.config.Configs` 的公开配置，提供 list/get/set/toggle/save，并把布尔项加入统一 `integrations` 注册表。

```text
!cb itemscroller status
!cb itemscroller list
!cb itemscroller list craft
!cb itemscroller get <配置名>
!cb itemscroller toggle <布尔配置名>
!cb itemscroller set <配置名> <JSON值>
!cb itemscroller save
```

权限：`extensions.itemscroller`。

## 当前边界

本扩展控制 Item Scroller 的配置，不模拟鼠标滚轮、拖拽或批量合成手势，也不实现远程容器点击序列。若需要无人值守容器操作，应新增独立任务扩展，处理同步、重试和关闭界面。

## 与 QuickShulker 配合

QuickShulker 扩展负责打开容器，Item Scroller 扩展负责其公开功能配置。打开容器后是否能使用 Item Scroller 特性取决于目标 Mod 的输入路径；ClientBot 当前不会伪造鼠标事件。
