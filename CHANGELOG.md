# 更新日志

本项目采用语义化版本；Minecraft 兼容版本在发布标签和文档中单独标明。

## 0.1.0 - 2026-11-20

首个 26.2 开发版本。

### 核心

- 私聊命令、UUID ACL、精确/通配权限、危险命令一次性确认、频率限制和审计日志。
- 本地 `/clientbot` 启用、授权、撤销、重载和测试命令。
- 聊天、移动、跟随、定点挖掘、攻击、使用、物品栏与延迟任务。
- 世界/手部渲染抑制和后台 FPS 限制。
- `clientbot` 扩展入口点、任务 API、统一功能开关和 MaLiLib 配置适配器。
- 在实例化入口类前按 `clientbot:requires` 跳过缺少依赖的扩展。

### 扩展

- Carpet、Servux/MiniHUD、Litematica、Litematica Printer。
- Tweakeroo、MiniHUD、Item Scroller、QuickShulker。
- 最小第三方扩展示例。

### 文档与工程

- 用户、命令、配置、安全、扩展开发、移植和逐扩展文档。
- 核心权限、确认、参数、配置与任务单元测试。
- Gradle 多模块构建和 GitHub Actions CI。
