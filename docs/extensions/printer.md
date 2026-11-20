# Litematica Printer 扩展

文件：`clientbot-printer-0.1.0.jar`

## 依赖

Core、MaLiLib、Litematica、Sakura Ryoko 维护线的 Litematica Printer。26.2 基线为 Printer 3.2.2，目标 Mod ID 为 `litematica_printer`。

## 专用命令

```text
!cb printer start
!cb printer stop
!cb printer toggle
!cb printer status
!cb printer range <2.5-5>
!cb printer interval <1-40>
!cb printer rotate <on/off>
```

权限：`extensions.printer`。

`start/stop` 修改 Printer 的 `printingMode`，打印算法、材料选择、动作队列、碰撞与服务端校验继续由 Printer 自己执行。

## 全部配置

`printercfg list/get/set/toggle/save`，权限 `extensions.printercfg`。当前公开项包括打印模式、调试、范围、间隔、替换流体源、去皮、交互、空中打印和旋转；以运行时 `list` 为准。

统一开关：`integrations set printer.printing on/off`。

## 推荐流程

```text
!cb litematica load build.litematic 100 64 100
!cb printer interval 8
!cb printer range 4.0
!cb printer start
!cb printer status
!cb printer stop
```

## 限制

- 本扩展不是另一个打印实现，不支持 Printer 本身不支持的方块或操作。
- `range`/`interval` 的合法范围由 Printer 配置限制。
- 自动打印可能被服务器规则或反作弊禁止。
- 关闭 ClientBot 世界渲染不一定关闭 Printer 逻辑；要停止打印必须 `printer stop`。
- 材料耗尽、离开范围或投影缺失时，状态仍可能显示 printing=true，这表示模式开启而非正在成功放置。
