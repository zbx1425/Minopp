package cn.zbx1425.minopp.block;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.game.effect.EffectEvent;
import cn.zbx1425.minopp.game.effect.EffectEvents;
import cn.zbx1425.minopp.game.effect.SeatActionTakenEffectEvent;
import cn.zbx1425.minopp.game.ActionReport;
import cn.zbx1425.minopp.game.CardGame;
import cn.zbx1425.minopp.game.CardPlayer;
import cn.zbx1425.minopp.game.TableRuleConfig;
import cn.zbx1425.minopp.game.shard.ActionReportShard;
import cn.zbx1425.minopp.game.shard.ActionReportShards;
import cn.zbx1425.minopp.game.shard.SystemShard;
import cn.zbx1425.minopp.item.ItemHandCards;
import cn.zbx1425.minopp.network.S2CActionEphemeralPacket;
import cn.zbx1425.minopp.network.S2CEffectListPacket;
import cn.zbx1425.minopp.platform.multiver.PlayerShim;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//? if >=26.1 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//? }

import java.util.*;
import java.util.function.Function;

public class BlockEntityMinoTable extends BlockEntity {

    public Map<Direction, CardPlayer> players = new HashMap<>();
    public CardGame game = null;
    public List<ActionReportShard> stateShards = new ArrayList<>(List.of(
            new SystemShard(Component.translatable("game.minopp.play.no_game"))));

    public List<Pair<ActionReportShard, Long>> clientEphemeralShards = new ArrayList<>();
    public List<ActionReportShard> clientStickyShards = new ArrayList<>();

    public ItemStack award = ItemStack.EMPTY;
    public boolean demo = false;
    public TableRuleConfig rules = TableRuleConfig.DEFAULT;

    public static final List<Direction> PLAYER_ORDER = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    public BlockEntityMinoTable(BlockPos blockPos, BlockState blockState) {
        super(Mino.BLOCK_ENTITY_TYPE_MINO_TABLE.get(), blockPos, blockState);
        for (Direction direction : PLAYER_ORDER) {
            players.put(direction, null);
        }
    }

    private MinoTableState getSerializableState() {
        Map<String, CardPlayer> map = new HashMap<>();
        for (Map.Entry<Direction, CardPlayer> e : players.entrySet()) {
            if (e.getValue() != null) map.put(e.getKey().getSerializedName(), e.getValue());
        }
        return new MinoTableState(map, game, stateShards, award, demo, rules);
    }

    private void applyLoadedState(MinoTableState loaded) {
        for (Direction d : PLAYER_ORDER) {
            players.put(d, loaded.players().get(d.getSerializedName()));
        }
        CardGame previousGame = game;
        game = loaded.game();
        List<ActionReportShard> newStateShards = loaded.stateShards();
        if (!newStateShards.equals(stateShards)) {
            if (previousGame == null && game != null) {
                clientEphemeralShards.clear();
                clientStickyShards.clear();
            } else {
                boolean newStateHasPlay = newStateShards.stream()
                        .anyMatch(s -> s instanceof cn.zbx1425.minopp.game.shard.PlayShard);
                if (newStateHasPlay) {
                    clientStickyShards.clear();
                }
                long expiry = System.currentTimeMillis() + 6000;
                for (ActionReportShard oldShard : stateShards) {
                    if (oldShard.isNoteworthy()) {
                        clientEphemeralShards.add(new Pair<>(oldShard, expiry));
                    } else if (oldShard.shardType().transitionBehavior() == ActionReportShard.TransitionBehavior.TOP_CARD_STICKY
                               && !newStateHasPlay) {
                        clientStickyShards.add(oldShard);
                    }
                }
            }
            stateShards = new ArrayList<>(newStateShards);
            clientEphemeralShards.removeIf(entry ->
                    entry.getFirst().shardType().lifecycle() == ActionReportShard.Lifecycle.REJECTION);
        }
        award = loaded.award();
        demo = loaded.demo();
        rules = loaded.rules();
    }

    @Override
    //? if <26.1 {
    /*protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.merge(NbtIOShim.encode(MinoTableState.CODEC, getSerializableState(), provider));
    *///? } else if >=26.1 {
    @SuppressWarnings("deprecation")
    protected void saveAdditional(final ValueOutput output) {
        super.saveAdditional(output);
        output.store(MinoTableState.MAP_CODEC, getSerializableState());
    //? }
    }

    @Override
    //? if <26.1 {
    /*protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        applyLoadedState(NbtIOShim.decode(MinoTableState.CODEC, tag, provider));
    *///? } else if >= 26.1 {
    @SuppressWarnings("deprecation")
    protected void loadAdditional(final ValueInput input) {
        super.loadAdditional(input);
        input.read(MinoTableState.MAP_CODEC).ifPresent(this::applyLoadedState);
    //? }
    }

    public ArrayList<CardPlayer> getPlayersList() {
        ArrayList<CardPlayer> playersList = new ArrayList<>();
        for (Direction direction : PLAYER_ORDER) {
            if (players.get(direction) != null) {
                playersList.add(players.get(direction));
            }
        }
        return playersList;
    }

    public List<Direction> getEmptyDirections() {
        List<Direction> emptyDirections = new ArrayList<>();
        for (Direction direction : PLAYER_ORDER) {
            if (players.get(direction) == null) {
                emptyDirections.add(direction);
            }
        }
        return emptyDirections;
    }

    private static final int PLAYER_RANGE = 20;

    public void joinPlayerToTable(CardPlayer cardPlayer, Vec3 playerPos) {
        if (game != null) return;
        BlockPos centerPos = getBlockPos().offset(1, 0, 1);
        Vec3 playerOffset = playerPos.subtract(centerPos.getX(), centerPos.getY(), centerPos.getZ());
        Direction playerDirection = Direction.fromYRot(Mth.atan2(playerOffset.z, playerOffset.x) * 180 / Math.PI - 90);
        for (Direction checkDir : players.keySet()) {
            if (cardPlayer.equals(players.get(checkDir))) {
                players.put(checkDir, null);
            }
        }
        players.put(playerDirection, cardPlayer);
        sync();
    }

    @SuppressWarnings("unchecked, rawtypes")
    public void startGame(CardPlayer initiator) {
        if (game != null) return;
        List<CardPlayer> playerList = getPlayersList();
        if (playerList.size() < 2) return;

        AABB searchArea = AABB.ofSize(Vec3.atLowerCornerWithOffset(getBlockPos(), 1, 1, 1), PLAYER_RANGE, PLAYER_RANGE, PLAYER_RANGE);
        for (CardPlayer cardPlayer : playerList) {
            boolean playerFound = false;
            for (Entity entity : level.getEntities(null, searchArea)) {
                if (entity instanceof Player mcPlayer) {
                    if (cardPlayer.uuid.equals(PlayerShim.getGameProfileId(mcPlayer))) {
                        ItemStack handCard = new ItemStack(Mino.ITEM_HAND_CARDS.get());
                        ItemHandCards.CardGameBindingComponent newBinding =
                                new ItemHandCards.CardGameBindingComponent(getBlockPos(), cardPlayer.uuid);
                        handCard.set(Mino.DATA_COMPONENT_TYPE_CARD_GAME_BINDING.get(), newBinding);
                        //~ if >=26.1 '.selected' -> '.getSelectedSlot()'
                        if (Inventory.isHotbarSlot(mcPlayer.getInventory().getSelectedSlot())
                            //~ if >=26.1 '.getSelected()' -> '.getSelectedItem()'
                            && mcPlayer.getInventory().getSelectedItem().isEmpty()) {
                            //~ if >=26.1 '.selected' -> '.getSelectedSlot()'
                            mcPlayer.getInventory().setItem(mcPlayer.getInventory().getSelectedSlot(), handCard);
                            playerFound = true;
                        } else {
                            boolean addSuccessful = mcPlayer.getInventory().add(handCard);
                            if (!addSuccessful) {
                                ItemEntity itemEntity = mcPlayer.drop(handCard, false);
                                if (itemEntity != null) {
                                    itemEntity.setNoPickUpDelay();
                                    itemEntity.setTarget(mcPlayer.getUUID());
                                }
                            }
                            PlayerShim.sendSystemMessage(mcPlayer, Component.translatable("game.minopp.play.hand_card_in_inventory"));
                            playerFound = true;
                        }
                    }
                } else {
                    if (cardPlayer.uuid.equals(entity.getUUID())) {
                        playerFound = true;
                    }
                }
                if (playerFound) break;
            }
            if (!playerFound) {
                destroyGame(initiator);
                stateShards = new ArrayList<>(List.of(new SystemShard(
                        Component.translatable("game.minopp.play.player_unavailable", cardPlayer.name))));
                return;
            }
        }

        players.values().forEach(p -> { if (p != null) {
            p.hand.clear();
            p.hasShoutedMino = false;
        } });
        game = new CardGame(getPlayersList());
        ActionReport initReport = game.initiate(initiator, 7);
        stateShards = new ArrayList<>();
        for (ActionReportShard shard : initReport.shards) {
            if (shard.shardType().lifecycle() == ActionReportShard.Lifecycle.STATE) {
                stateShards.add(shard);
            }
        }
        sendSeatActionTakenToAll();
        sync();
    }

    public void destroyGame(CardPlayer initiator) {
        if (game != null) sendSeatActionTakenToAll();
        game = null;

        for (Player mcPlayer : level.players()) {
            for (int i = 0; i < mcPlayer.getInventory().getContainerSize(); i++) {
                ItemStack invItem =  mcPlayer.getInventory().getItem(i);
                if (!invItem.is(Mino.ITEM_HAND_CARDS.get())) continue;
                ItemHandCards.CardGameBindingComponent gameBinding = invItem.get(Mino.DATA_COMPONENT_TYPE_CARD_GAME_BINDING.get());
                if (gameBinding != null && gameBinding.tablePos().equals(getBlockPos())) {
                    mcPlayer.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
        }

        for (CardPlayer cardPlayer : players.values()) {
            if (cardPlayer == null) continue;
            Entity entity = ((ServerLevel)level).getEntity(cardPlayer.uuid);
            if (entity instanceof LivingEntity livingEntity) {
                for (InteractionHand hand : InteractionHand.values()) {
                    ItemStack stack = livingEntity.getItemInHand(hand);
                    if (stack.is(Mino.ITEM_HAND_CARDS.get())) {
                        livingEntity.setItemInHand(hand, ItemStack.EMPTY);
                    }
                }
            }
        }

        players.values().forEach(p -> { if (p != null) {
            p.hand.clear();
            p.hasShoutedMino = false;
        } });
        stateShards = new ArrayList<>(List.of(new SystemShard(
                Component.translatable("game.minopp.play.game_destroyed", initiator.name))));
        sync();
    }

    public void resetSeats(CardPlayer initiator) {
        sendSeatActionTakenToAll();
        players.replaceAll((d, v) -> null);
        stateShards = new ArrayList<>(List.of(new SystemShard(
                Component.translatable("game.minopp.play.seats_reset", initiator.name))));
        sync();
    }

    public void handleActionResult(ActionReport result, CardPlayer cardPlayer, ServerPlayer player) {
        if (result == null) return;

        List<ActionReportShard> newState = new ArrayList<>();
        List<ActionReportShard> rejections = new ArrayList<>();
        List<ActionReportShard> oob = new ArrayList<>();
        for (ActionReportShard shard : result.shards) {
            switch (shard.shardType().lifecycle()) {
                case STATE -> newState.add(shard);
                case REJECTION -> rejections.add(shard);
                case OUT_OF_BAND -> oob.add(shard);
            }
        }

        if (!rejections.isEmpty() && player != null) {
            S2CActionEphemeralPacket.sendS2C(player, getBlockPos(), rejections);
        }

        if (!oob.isEmpty()) {
            sendShardsToAll(oob);
        }

        if (result.shouldDestroyGame) {
            destroyGame(cardPlayer);
        }

        if (!newState.isEmpty()) {
            stateShards = newState;
        }

        if (!result.effects.isEmpty()) {
            MinecraftServer server = ((ServerLevel)level).getServer();
            BlockPos tableCenterPos = getBlockPos().offset(1, 0, 1);
            for (EffectEvent effect : result.effects) {
                effect.summonServer((ServerLevel) level, tableCenterPos, this);
            }
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
                if (serverPlayer.level().dimension() == level.dimension()) {
                    if (serverPlayer.position().distanceToSqr(Vec3.atCenterOf(tableCenterPos)) <= EffectEvents.EFFECT_RADIUS * EffectEvents.EFFECT_RADIUS) {
                        boolean playerPartOfGame = getPlayersList().stream().anyMatch(p -> p.uuid.equals(PlayerShim.getGameProfileId(serverPlayer)));
                        S2CEffectListPacket.sendS2C(serverPlayer, result.effects, tableCenterPos, playerPartOfGame);
                    }
                }
            }
        }
        sync();
    }

    private void sendShardsToAll(List<ActionReportShard> shards) {
        for (CardPlayer cardPlayer : getPlayersList()) {
            Player mcPlayer = level.getPlayerByUUID(cardPlayer.uuid);
            if (mcPlayer != null) {
                S2CActionEphemeralPacket.sendS2C((ServerPlayer) mcPlayer, getBlockPos(), shards);
            }
        }
    }

    private void sendSeatActionTakenToAll() {
        for (CardPlayer player : getPlayersList()) {
            Player mcPlayer = level.getPlayerByUUID(player.uuid);
            BlockPos tableCenterPos = getBlockPos().offset(1, 0, 1);
            List<EffectEvent> events = List.of(new SeatActionTakenEffectEvent());
            if (mcPlayer != null) {
                S2CEffectListPacket.sendS2C((ServerPlayer) mcPlayer, events, tableCenterPos, true);
            }
        }
    }

    public void sync() {
        setChanged();
        BlockState blockState = level.getBlockState(getBlockPos());
         level.sendBlockUpdated(getBlockPos(), blockState, blockState, 2);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveWithoutMetadata(provider);
    }

    @Nullable @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private static final List<ActionReportShard> DEFAULT_STATE_SHARDS = List.of(
            new SystemShard(Component.translatable("game.minopp.play.no_game")));

    public record MinoTableState(
        Map<String, CardPlayer> players,
        @Nullable CardGame game,
        List<ActionReportShard> stateShards,
        ItemStack award,
        boolean demo,
        TableRuleConfig rules
    ) {
        public static final MapCodec<MinoTableState> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, CardPlayer.CODEC)
                .optionalFieldOf("players", Map.of()).forGetter(MinoTableState::players),
            CardGame.CODEC.optionalFieldOf("game").forGetter(s -> Optional.ofNullable(s.game)),
            ActionReportShards.DISPATCH_CODEC.listOf()
                .optionalFieldOf("stateShards", DEFAULT_STATE_SHARDS).forGetter(MinoTableState::stateShards),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("award", ItemStack.EMPTY).forGetter(MinoTableState::award),
            Codec.BOOL.optionalFieldOf("demo", false).forGetter(MinoTableState::demo),
            TableRuleConfig.CODEC.optionalFieldOf("rules", TableRuleConfig.DEFAULT).forGetter(MinoTableState::rules)
        ).apply(instance, (players, game, stateShards, award, demo, rules) ->
            new MinoTableState(players, game.orElse(null), stateShards, award, demo, rules)));

        public static final Codec<MinoTableState> CODEC = MAP_CODEC.codec();
    }
}
