# MiniHUD 扩展

文件：`clientbot-minihud-0.1.0.jar`

## 依赖

Core、MiniHUD 及其 MaLiLib 依赖。

## 覆盖范围

扫描 `Configs`、`InfoToggle`、`RendererToggle`、`StructureToggle`，覆盖公开 HUD 信息行、渲染器、结构显示和普通配置。

## 示例

```text
!cb minihud status
!cb minihud list tps
!cb minihud list structure
!cb minihud toggle infoServerTPS
!cb minihud get hudDataSync
!cb minihud toggle hudDataSync
!cb minihud save
```

权限：`extensions.minihud`。准确名称通过 `list` 获取，不同 MiniHUD 版本可能增加或调整条目。

布尔项同时注册为 `minihud.<配置名>` 统一开关。

## 与 Servux 的关系

MiniHUD 扩展负责 MiniHUD 自身配置；Servux 扩展负责协议状态、同步快捷命令和刷新请求。两者可以独立安装，但 Servux 扩展明确要求 MiniHUD。

## 限制

服务器没有 Carpet/Servux 或对应 provider 时，某些 HUD 数据不会存在。打开 HUD 选项不等于获得服务端权限。渲染抑制开启时，部分 HUD 仍有少量绘制开销。
