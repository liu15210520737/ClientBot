# 扩展 API 与开发指南

## 1. 架构

```text
Fabric Loader
  └─ clientbot (Core)
      ├─ CommandRegistry / CommandDispatcher
      ├─ TaskManager
      ├─ IntegrationRegistry
      ├─ ConfigManager / ResponseRouter
      └─ "clientbot" entry point
          ├─ clientbot-carpet
          ├─ clientbot-litematica
          ├─ clientbot-printer
          └─ 你的扩展
```

核心先注册内置命令，再读取所有 `clientbot` 入口点。扩展声明 `requiredMods()` 后，缺少目标 Mod 时不会初始化。初始化异常由核心记录，不阻止其他扩展。

## 2. 最小扩展

完整可构建示例位于 `examples/example-extension`。

### Java

```java
package com.example.bot;

import dev.clientbot.api.ClientBotExtension;
import dev.clientbot.api.ExtensionContext;
import dev.clientbot.command.BotCommand;
import java.util.Set;

public final class MyExtension implements ClientBotExtension {
    @Override public String id() { return "my-mod"; }

    @Override public Set<String> requiredMods() {
        return Set.of("mymod");
    }

    @Override public void initialize(ExtensionContext context) {
        context.commands().register(BotCommand.builder("mymod")
                .usage("start | stop | status")
                .description("控制 MyMod")
                .permission("extensions.mymod")
                .handler(ctx -> {
                    if (ctx.arg(0).equals("status")) {
                        ctx.reply("player=" + ctx.player().getGameProfile().name());
                    }
                })
                .build());
    }
}
```

### `fabric.mod.json`

```json
{
  "schemaVersion": 1,
  "id": "clientbot-mymod",
  "version": "1.0.0",
  "environment": "client",
  "entrypoints": {
    "clientbot": ["com.example.bot.MyExtension"]
  },
  "custom": {
    "clientbot:requires": ["mymod"]
  },
  "depends": {
    "fabricloader": ">=0.19.5",
    "minecraft": "~26.2",
    "java": ">=25",
    "clientbot": ">=0.1.0"
  },
  "suggests": {
    "mymod": "*"
  }
}
```

目标 Mod 放在 `suggests` 而不是强制 `depends`，这样核心能显示“缺少 mymod”，而不是 Fabric 在启动时直接终止。`clientbot:requires` 应与 Java 的 `requiredMods()` 保持一致：核心会在**实例化入口类之前**读取它并跳过缺失依赖，避免可选第三方类被过早解析。若你的扩展必须让目标 Mod 存在才能加载类，可改成 `depends`，但会失去友好降级。

### Gradle

```gradle
dependencies {
    implementation project(':core')
    compileOnly "com.example:mymod:1.0.0"
}
```

26.2 使用 Loom 1.17 的标准 Java 依赖配置和 Mojang 官方命名。第三方依赖优先 `compileOnly`，不要 `include` 或 shadow 第三方 Mod。移植到仍使用 `modImplementation`/`modCompileOnly` 的旧工具链时，应按该版本 Fabric 示例调整。

## 3. `ExtensionContext`

| 成员 | 用途 |
|---|---|
| `client()` | 当前 `Minecraft` 实例 |
| `commands()` | 注册远程/本地共用命令 |
| `tasks()` | 提交、停止、列出 Tick 任务 |
| `integrations()` | 注册统一布尔开关 |
| `config()` | 读取核心配置；不要直接远程修改 ACL |
| `configDirectory()` | `config/clientbot`，扩展自己的 JSON 放这里 |
| `isModLoaded(id)` | 运行时探测 |
| `execute(runnable)` | 切换到客户端主线程 |

初始化在客户端加载阶段执行。注册配置和命令可以在这里完成；需要世界/玩家的逻辑应延迟到命令或任务执行时。

## 4. 命令 API

`BotCommand.Builder`：

- `name`：唯一根命令；重复注册会抛异常。
- `usage`：参数说明。
- `description`：帮助文本。
- `permission`：ACL 节点，建议 `extensions.<name>`；管理操作使用 `extensions.<name>.admin`。
- `dangerous()`：远程执行前要求一次性确认。
- `handler`：实际逻辑，可抛异常；核心会返回简短错误并写日志。

`CommandContext`：

- `sender()`：已认证 UUID、名称、是否本地。
- `args()`：不含根命令的参数列表。
- `arg/intArg/doubleArg/booleanArg`：参数读取。
- `joined(start)`：保留为空格连接的剩余文本。
- `player()`：当前玩家；不在世界时抛出明确异常。
- `reply()`：通过核心回复路由，不要自己硬编码 `/msg`。
- `client()`：Minecraft 客户端。

查询和修改最好拆成两个根命令，避免给只读 `status` 也加危险确认。Carpet 扩展的 `carpet`/`carpetset` 是参考。

## 5. 任务 API

实现 `BotTask`：

```java
context.tasks().submit(new BotTask() {
    private int ticks;
    public String id() { return "mymod-job"; }
    public String description() { return "MyMod 自动任务"; }
    public String exclusiveGroup() { return "mymod"; }
    public void start() { /* 初始化 */ }
    public boolean tick() {
        if (context.client().player == null) return false;
        ticks++;
        return ticks < 100; // false 表示完成
    }
    public void stop() { /* 必须释放按键、关闭模式或还原状态 */ }
});
```

约定：

- `tick()` 每客户端 Tick 调用，必须快速、非阻塞。
- 返回 `false` 时核心移除任务并调用 `stop()`。
- 相同非空 `exclusiveGroup()` 的任务互斥。
- 相同 `id()` 会替换旧任务。
- 断线和玩家实体变化会 `stopAll()`。
- 不在 `tick()` 里下载文件、做大规模原理图 IO 或无限循环；重工作应分片。

## 6. 统一功能开关

```java
context.integrations().register(
    "mymod.enabled",             // 全局唯一 ID
    "mymod",                    // 所有者 Mod ID
    "MyMod 总开关",
    () -> MyConfig.enabled,       // getter
    value -> MyConfig.enabled = value
);
```

用户通过 `integrations list/set` 控制。setter 负责调用目标 Mod 的回调和保存配置；仅改一个未被读取的字段不够。

## 7. MaLiLib 配置适配

`ReflectedConfig.scan` 递归扫描公开静态字段和内部类，识别具有以下公开方法的对象：

```text
getName()
getAsJsonElement()
setValueFromJsonElement(JsonElement)
```

这覆盖 MaLiLib `IConfigBase` 及 Tweakeroo `FeatureToggle` 等兼容对象。`MasaCommandSupport.register` 自动提供 list/get/set/toggle/save。

如果目标 Mod 配置不是 MaLiLib 兼容接口，应写显式适配器，不要强行反射私有字段。

## 8. 扩展配置

可使用 `JsonFiles.load/save`：

```java
MyConfig config = JsonFiles.load(
    context.configDirectory().resolve("mymod.json"),
    MyConfig.class,
    new MyConfig()
);
```

配置类应有公开字段和无参构造器。启动时生成默认文件。安全相关默认值应为关闭或空权限。

## 9. 可选依赖与类加载

推荐扩展入口类只含少量直接引用，并保证 `requiredMods()` 在任何目标类初始化前返回。若目标 Mod 缺失，核心不会调用 `initialize()`。

复杂联动可采用：

```text
MyExtension.java          // 只做 requiredMods 和初始化分流
MyModIntegration.java     // 直接引用目标 API，仅确认存在后加载
```

不要在核心中添加目标 Mod 的 import、Mixin 或硬依赖。

## 10. 在本仓库新增模块

1. `settings.gradle` 添加 `include 'extensions:mymod'`。
2. 新建 `extensions/mymod/build.gradle`，`archivesName='clientbot-mymod'`。
3. 添加入口类与 `fabric.mod.json`。
4. 添加 `docs/extensions/mymod.md`：依赖、命令、权限、配置、限制、兼容版本。
5. 在 `docs/COMMANDS.md` 和用户指南补充入口。
6. 执行 `./gradlew :extensions:mymod:build` 和全量 `./gradlew build`。
7. 至少验证：无目标 Mod 时安全跳过、有目标 Mod 时初始化、getter/setter 生效、断线任务清理。

## 11. API 兼容约定

- `core/src/main/resources/fabric.mod.json` 的 `clientbot:api_version` 当前为 1。
- 扩展公开入口为 `dev.clientbot.api`、命令/任务/集成注册表及文档列出的工具类。
- 核心内部实现可能调整；扩展不应依赖 Mixin 内部字段或私有方法。
- 新增可选方法优先使用 interface `default` 方法，避免破坏旧扩展。
- 破坏性变更应提升 API 版本、写迁移说明，并在依赖声明中约束核心版本。
