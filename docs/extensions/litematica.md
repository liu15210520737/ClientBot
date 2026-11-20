# Litematica 扩展

文件：`clientbot-litematica-0.1.0.jar`

## 依赖

Core、MaLiLib、Litematica。26.2 开发依赖基线为 MaLiLib 0.29.6、Litematica 0.28.8。

## 专用命令

- `litematica load <文件> [x y z]`：从 `.minecraft/schematics` 读取 `.litematic`，加入 SchematicHolder，创建并选中放置。省略坐标时使用玩家位置。
- `litematica list`：列出放置名称和原点。
- `litematica unload [名称]`：卸载指定或当前选中的放置。
- `litematica easyplace <on/off>`：设置并保存 `easyPlaceMode`。
- `litematica render <on/off>`：设置并保存 `enableRendering`。

权限：`extensions.litematica`。

## 全部公开配置

根命令 `litematicacfg`，权限 `extensions.litematicacfg`：

```text
!cb litematicacfg list easyPlace
!cb litematicacfg get toolItem
!cb litematicacfg set toolItem '"minecraft:stick"'
!cb litematicacfg toggle easyPlaceSwingHand
!cb litematicacfg save
```

扫描范围为 `fi.dy.masa.litematica.config.Configs` 及公开内部配置类。

## 与 Printer 配合

先 `litematica load` 建立放置，再使用 `printer start`。本扩展只提供投影和 EasyPlace；自动打印由独立的 Printer 扩展调用 Litematica Printer。

## 安全与限制

- 文件路径规范化后必须仍位于 `schematics` 内，阻止 `../` 越界。
- 当前专用加载器针对 `.litematic`，不是任意 Sponge/Schematica 转换器。
- 大文件读取在客户端命令线程执行，可能造成短暂卡顿；后续可改成分片/异步读取再回主线程创建放置。
- Servux paste、共享原理图和权限由对应服务端/provider 决定。
