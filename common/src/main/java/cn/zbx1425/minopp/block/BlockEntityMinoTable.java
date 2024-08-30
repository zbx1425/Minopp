package cn.zbx1425.minopp.block;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.game.ActionMessage;
import cn.zbx1425.minopp.game.CardGame;
import cn.zbx1425.minopp.game.CardPlayer;
import cn.zbx1425.minopp.item.ItemHandCards;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BlockEntityMinoTable extends BlockEntity {

    public Map<Direction, CardPlayer> players = new HashMap<>();
    public CardGame game = null;
    public ActionMessage state = ActionMessage.NO_GAME;

    public List<Pair<ActionMessage, Long>> clientMessageList = new ArrayList<>();

    public static final List<Direction> PLAYER_ORDER = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    public BlockEntityMinoTable(BlockPos blockPos, BlockState blockState) {
        super(Mino.BLOCK_ENTITY_TYPE_MINO_TABLE.get(), blockPos, blockState);
        for (Direction direction : PLAYER_ORDER) {
            players.put(direction, null);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        CompoundTag playersTag = new CompoundTag();
        for (Map.Entry<Direction, CardPlayer> entry : players.entrySet()) {
            if (entry.getValue() != null) {
                playersTag.put(entry.getKey().getSerializedName(), entry.getValue().toTag());
            }
        }
        compoundTag.put("players", playersTag);
        if (game != null) {
            compoundTag.put("game", game.toTag());
        }
        compoundTag.put("state", state.toTag());
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        CompoundTag playersTag = compoundTag.getCompound("players");
        for (Direction direction : PLAYER_ORDER) {
            if (playersTag.contains(direction.getSerializedName())) {
                players.put(direction, new CardPlayer(playersTag.getCompound(direction.getSerializedName())));
            } else {
                players.put(direction, null);
            }
        }
        CardGame previousGame = game;
        if (compoundTag.contains("game")) {
            game = new CardGame(compoundTag.getCompound("game"));
        } else {
            game = null;
        }
        ActionMessage newState = new ActionMessage(compoundTag.getCompound("state"));
        if (!newState.equals(state)) {
            if (previousGame == null && game != null) {
                clientMessageList.clear();
            } else {
                clientMessageList.add(new Pair<>(state, System.currentTimeMillis() + 12000));
            }
            state = newState;
            clientMessageList.removeIf(entry -> entry.getFirst().isEphemeral);
        }
    }

    public List<CardPlayer> getPlayersList() {
        // Return a list of players in the order of NORTH, EAST, SOUTH, WEST, without null elements
        List<CardPlayer> playersList = new ArrayList<>();
        for (Direction direction : PLAYER_ORDER) {
            if (players.get(direction) != null) {
                playersList.add(players.get(direction));
            }
        }
        return playersList;
    }

    private static final int PLAYER_RANGE = 20;

    public void startGame(CardPlayer player) {
        List<CardPlayer> players = getPlayersList();
        if (players.size() < 2) return;

        // Give hand card items to players
        for (CardPlayer cardPlayer : players) {
            boolean playerFound = false;
            for (Player mcPlayer : level.players()) {
                if (mcPlayer.position().distanceToSqr(Vec3.atCenterOf(getBlockPos())) > PLAYER_RANGE * PLAYER_RANGE) continue;
                for (ItemStack invItem : mcPlayer.getInventory().items) {
                    if (!invItem.is(Mino.ITEM_HAND_CARDS.get())) continue;
                    ItemHandCards.CardGameBindingComponent gameBinding = invItem.getOrDefault(Mino.DATA_COMPONENT_TYPE_CARD_GAME_BINDING.get(),
                            ItemHandCards.CardGameBindingComponent.EMPTY);
                    if (cardPlayer.uuid.equals(gameBinding.player())) {
                        // We've found an applicable hand card item
                        if (gameBinding.tablePos().isEmpty()) {
                            // It's not bound, bind it
                            ItemHandCards.CardGameBindingComponent newBinding = new ItemHandCards.CardGameBindingComponent(
                                    gameBinding.player(), Optional.of(getBlockPos()));
                            invItem.set(Mino.DATA_COMPONENT_TYPE_CARD_GAME_BINDING.get(), newBinding);
                            playerFound = true;
                        }
                    }
                }
                if (!playerFound) {
                    if (cardPlayer.uuid.equals(mcPlayer.getGameProfile().getId())) {
                        // We've found a player, but no applicable hand card item, give them one
                        ItemStack handCard = new ItemStack(Mino.ITEM_HAND_CARDS.get());
                        ItemHandCards.CardGameBindingComponent newBinding = new ItemHandCards.CardGameBindingComponent(
                                mcPlayer.getGameProfile().getId(), Optional.of(getBlockPos()));
                        handCard.set(Mino.DATA_COMPONENT_TYPE_CARD_GAME_BINDING.get(), newBinding);
                        playerFound = mcPlayer.getInventory().add(handCard);
                    }
                }
            }
            if (!playerFound) {
                // No player found or no hand card item given, destroy the game
                destroyGame(player);
                state = new ActionMessage(null, player).panic(Component.translatable("game.minopp.play.player_unavailable", cardPlayer.name));
                return;
            }
        }

        game = new CardGame(getPlayersList());
        state = game.initiate(player, 7);
        setChanged();
    }

    public void destroyGame(CardPlayer player) {
        game = null;

        // Remove hand card items from players
        for (Player mcPlayer : level.players()) {
            for (ItemStack invItem : mcPlayer.getInventory().items) {
                if (!invItem.is(Mino.ITEM_HAND_CARDS.get())) continue;
                ItemHandCards.CardGameBindingComponent gameBinding = invItem.getOrDefault(Mino.DATA_COMPONENT_TYPE_CARD_GAME_BINDING.get(),
                        ItemHandCards.CardGameBindingComponent.EMPTY);
                if (gameBinding.tablePos().isPresent() && gameBinding.tablePos().get().equals(getBlockPos())) {
                    if (gameBinding.player().equals(mcPlayer.getGameProfile().getId())) {
                        // Default item, just remove
                        mcPlayer.getInventory().removeItem(invItem);
                    } else {
                        // Unbind
                        ItemHandCards.CardGameBindingComponent newBinding = new ItemHandCards.CardGameBindingComponent(
                                gameBinding.player(), Optional.empty());
                        invItem.set(Mino.DATA_COMPONENT_TYPE_CARD_GAME_BINDING.get(), newBinding);
                    }
                }
            }
        }

        players.values().forEach(p -> { if (p != null) p.hand.clear(); });
        state = new ActionMessage(null, player).gameDestroyed();
        setChanged();
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, provider);
        return tag;
    }

    @Nullable @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
