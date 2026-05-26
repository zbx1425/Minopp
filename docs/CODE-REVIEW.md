# Minopp 代码审查报告

> 所有发现均基于实际代码，引用具体文件路径和符号名。

## 1. 安全问题

### 1.1 C2SPlayCardPacket UUID 欺骗 [严重]

`C2SPlayCardPacket` 中客户端自行发送 `playerUuid`，服务端仅通过 `CardGame.deAmputate(playerUuid)` 查找玩家但从未验证该 UUID 是否等于已认证的 `ServerPlayer.getUUID()`。恶意客户端可以代替其他玩家出牌。

**修复方向**：服务端处理时忽略客户端发送的 UUID，改用 `player.getUUID()` 获取。

### 1.2 C2SSeatControlPacket 缺少权限检查 [严重]

`C2SSeatControlPacket` 中对玩家是否在座位上的检查代码已被注释掉（原文件第 32-35 行）。任何玩家（甚至未入座的远程玩家）都可以对任意牌桌发送开始/停止/重置命令，且没有距离检查。

**修复方向**：恢复被注释的座位成员检查，并增加距离校验。

### 1.3 万能牌花色索引越界风险 [中等]

`C2SPlayCardPacket` 中 `wildSelectionOrdinal` 直接用作 `Card.Suit.values()` 数组索引，未做边界检查。恶意客户端发送越界值会导致 `ArrayIndexOutOfBoundsException`。

### 1.4 C2SAutoPlayerConfigPacket 原始 NBT 注入 [低]

虽然该包检查了 op 权限 (`hasPermissions(2)`)，但 `readConfigFromTag` 直接接受客户端发送的原始 `CompoundTag`。如果反序列化逻辑对异常字段不够健壮，可能产生意外行为。

## 2. Bug

### 2.1 ItemHandCards tooltip UUID 检查逻辑反转 [确认 Bug]

`ItemHandCards.appendHoverText` 中条件为 `binding.bearerId().equals(Minecraft.getInstance().player.getGameProfile().getId())`，但该条件为 true 时显示"NOT YOUR CARD!"——逻辑完全相反，应为 `!equals`。

此外该方法直接引用 `Minecraft.getInstance()`，如果在专用服务端环境触发 tooltip 生成则会崩溃（虽然当前不太可能触发）。

### 2.2 GameOverlayLayer alpha clamp 参数顺序错误 [确认 Bug]

`GameOverlayLayer` 中使用 `Mth.clamp(0, 0xFF, ...)` 计算 alpha 值，但 `Mth.clamp(value, min, max)` 的第一个参数应为待 clamp 的值。当前写法始终将 `0` 夹在 `0xFF` 和实际值之间，在某些情况下不会产生预期结果。

### 2.3 NeoForge 生命周期事件未注册 [平台 Bug]

`neoforge/.../platform/neoforge/ServerPlatformImpl.java` 中玩家加入/退出、服务端生命周期、tick 等事件注册全部被注释掉。这意味着 NeoForge 平台上这些事件回调无效。

## 3. 已知 Hacks 与 Workarounds

### 3.1 HandCardsWithoutLevelRenderer Pose Stack 操作

`HandCardsWithoutLevelRenderer` 第三人称渲染中先 `popPose()` 再 `pushPose()` 来"逃出"调用方的变换矩阵。作者注释明确承认：*"Transform must be somehow messed up but it works so I'm not going to fix it"*。

### 3.2 CompatPacket retainedDuplicate() 技巧

Fabric 和 NeoForge 的 `CompatPacket` 实现中，`STREAM_CODEC.decode` 使用 `retainedDuplicate()` + 手动推进 `readerIndex` 到末尾，将整个剩余 buffer 作为不透明 blob 消费。这绕过了平台对结构化 codec 的预期，允许 mod 使用自定义 `FriendlyByteBuf` 序列化。NeoForge 侧还额外需要 `packet.readerIndex(0)` 重置。

### 3.3 CardGame 罚抽重置技巧

`CardGame` 中当罚抽被接受后，执行 `topCard = topCard.withEquivFamily(Card.Family.NUMBER)`——将顶牌的等效 family 覆盖为 NUMBER，阻止下一位玩家继续叠加 DRAW 牌。非显而易见的隐式状态突变。

### 3.4 VoxelShape 高度 14.9

`BlockMinoTable` 的碰撞/轮廓形状高度为 `14.9` 而非 `15`，可能是为了避免站在桌边时与纸牌交互判定产生冲突。

### 3.5 S2CAutoPlayerScreenPacket YACL 反射检测

通过 `Class.forName("dev.isxander.yacl3....")` 运行时检测 YACL3 是否可用，结果缓存在 `static Boolean`。不可用时回退到聊天消息而非崩溃。这是经典的可选依赖软加载模式。

### 3.6 Mino 喊叫的聊天拦截

`Mino.onServerChatMessage` 拦截所有匹配 "mino"/"uno"/"minopp"（忽略大小写、空格、感叹号）的聊天消息并触发游戏内喊叫逻辑。会抑制原始聊天消息，可能意外吞掉包含这些词的正常对话。

### 3.7 质疑操作通过攻击实体触发

`Mino.onPlayerAttackEntity` 在客户端检测到持有手牌攻击其他实体时发送 `doubtMino` 包。这是非常规的交互设计——攻击 = 质疑。

## 4. 代码质量问题

### 4.1 封装缺失

`Card`、`CardGame`、`CardPlayer` 的所有字段均为 `public` 且可变。`Card` 作为值对象实现了 `equals`/`hashCode` 但字段可被外部任意修改，破坏集合中的不变性约束。`CardGame` 的核心状态（`currentPlayerIndex`、`deck`、`players`）完全暴露。

**建议**：至少对关键类使用 `private` 字段 + getter；考虑将 `Card` 设计为不可变类或 `record`。

### 4.2 Card 魔数

`Card` 中使用字面量 `-101`(SKIP)、`-102`(REVERSE)、`-2`(DRAW 2)、`-4`(DRAW 4)、`-1`(WILD) 作为数字标识，散布在多处条件判断中。无命名常量。

**建议**：定义 `public static final int` 常量。

### 4.3 Card.compareTo 使用 hashCode

`Card.compareTo` 委托给 `hashCode()` 比较。hashCode 虽然在此实现中是确定性的，但语义上不保证有意义的全序关系。

### 4.4 Random 实例频繁创建

`AutoPlayer.playAtGame` 和 `EntityAutoPlayer.tick` 中每次调用都 `new Random()`，而非复用实例。不影响正确性但浪费资源，快速连续调用时可能产生相关种子。

**建议**：使用持久 `RandomSource` 或 `ThreadLocalRandom`。

### 4.5 ActionReport.NO_GAME 可变单例

`ActionReport.NO_GAME` 是 `static final` 实例，但 `ActionReport` 本身是可变类。如果代码路径意外修改该共享实例，会产生全局状态污染。当前看起来安全，但属于潜在隐患。

**建议**：使该实例不可变，或每次创建新实例。

### 4.6 CardGame.shoutMino 返回 null

`CardGame.shoutMino()` 在某些路径返回 `null`，`combineWith` 中有 null 守卫。使用 `Optional` 或空 `ActionReport` 会更安全。

### 4.7 死代码

- `MinoCommand.withPlayerAndGame` 是一个定义了但未被任何子命令使用的工具方法。
- `BlockEntityMinoTableRenderer` 中存在注释掉的附魔光效渲染和摄像机朝向 billboard 代码。

### 4.8 命令中硬编码英文字符串

`MinoCommand` 的 `set_table_award` 和 `set_table_demo` 子命令中使用硬编码英文字符串（如 "Requirement: Hold an item"、"Table award set"），而非翻译键。

**建议**：使用 `Component.translatable()`。

### 4.9 RegistryObject 线程安全

`RegistryObject.get()` 的懒加载不是线程安全的——并发首次调用可能重复创建对象。在 Minecraft 的单线程初始化环境下通常不是问题，但设计上不健壮。

### 4.10 Fabric ServerPlatformImpl 使用 ENTITY_LOAD 代替 JOIN

Fabric 的 `ServerPlatformImpl` 使用 `ENTITY_LOAD` 事件而非连接事件作为玩家加入钩子，这在维度切换时也会触发，可能产生重复回调。

### 4.11 CardGame.deAmputate 命名

`deAmputate` 方法名含义晦涩（作者幽默地将"无手牌的玩家存根"称为"截肢"的玩家）。方法功能是通过 UUID 或存根查找完整 `CardPlayer`，命名可改进为如 `resolvePlayer`。

## 5. 设计改进方向

以下为非紧急但有价值的改进方向，供后续版本参考：

1. **引入命令模式或事件溯源**：当前 `CardGame` 是事务脚本式的 God Object，所有规则内联。提取 `GameAction` 接口可以支持回放、撤销和规则变体。
2. **DataComponent 序列化统一**：当前手工 NBT 序列化(`toTag`/构造函数对)分散在每个类中，考虑统一使用 `Codec` 系统。
3. **增量同步代替全量同步**：`BlockEntityMinoTable.getUpdateTag` 发送完整状态，高频操作时可改为只同步变化部分。
4. **单元测试**：`CardGame`、`AutoPlayer`、`Card.canPlayOn` 等纯逻辑类可以脱离 Minecraft 环境进行单元测试，当前无任何测试。
5. **Forge 模块清理**：`forge/` 目录仍存在于仓库中但已从构建中移除，应考虑删除或归档。
