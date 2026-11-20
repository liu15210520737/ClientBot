# Servux 扩展

文件：`clientbot-servux-0.1.0.jar`

## 依赖与原理

客户端需要 Core、MiniHUD 和本扩展；Servux 安装在服务端。扩展通过 MiniHUD 的公开客户端状态读取协议结果，不要求把 Servux 放进客户端。

## 功能

- 查看 Servux 是否存在及版本。
- 查看 MiniHUD 已接收的 TPS、MSPT、世界出生点、出生区块半径和种子是否已知。
- 开关 `hudDataSync`、`entityDataSync` 并保存 MiniHUD 配置。
- 请求 HUD metadata、配方数据，或标记结构边界缓存需要更新。

## 命令

```text
!cb servux status
!cb servux sync hud on
!cb servux sync entity on
!cb servux refresh hud
!cb servux refresh structures
!cb servux refresh recipes
!cb servux save
```

权限：`extensions.servux`。

## 与 Carpet 配合

MiniHUD 可以使用 Carpet 或 Servux 提供的数据。`servux status` 只报告 Servux，不会把 Carpet 状态误报为 Servux。需要 Carpet 状态时使用 `carpet status`。

## 限制

服务端可以按权限关闭 seed、weather、entity NBT、structure、paste 等 provider。客户端请求不会提升权限。刷新只是请求/缓存失效，不保证服务端立即返回；网络延迟和服务端配置会影响结果。
