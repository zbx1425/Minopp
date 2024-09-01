package cn.zbx1425.minopp.item;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.block.BlockMinoTable;
import cn.zbx1425.minopp.game.CardPlayer;
import cn.zbx1425.minopp.platform.GroupedItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.UUID;

public class ItemHandCards extends GroupedItem {
    
    public ItemHandCards() {
        super(() -> null, p -> p.stacksTo(1)
                .component(Mino.DATA_COMPONENT_TYPE_CARD_GAME_BINDING.get(), CardGameBindingComponent.EMPTY)
//                .component(DataComponents.CAN_PLACE_ON, new AdventureModePredicate(List.of(BlockPredicate.Builder.block().of(Mino.BLOCK_MINO_TABLE.get()).build()), false))
        );
    }

    public static CardPlayer getCardPlayer(Player player) {
        if (player.getMainHandItem().is(Mino.ITEM_HAND_CARDS.get())) {
            CardGameBindingComponent component = player.getMainHandItem().getOrDefault(Mino.DATA_COMPONENT_TYPE_CARD_GAME_BINDING.get(), CardGameBindingComponent.EMPTY);
            return new CardPlayer(component.player, component.player.toString().substring(0, 8));
        } else {
            return new CardPlayer(player);
        }
    }

    public static BlockPos getHandCardGamePos(Player player) {
        BlockPos tablePos = null;
        if (player.getMainHandItem().is(Mino.ITEM_HAND_CARDS.get())) {
            tablePos = player.getMainHandItem().getOrDefault(Mino.DATA_COMPONENT_TYPE_CARD_GAME_BINDING.get(), ItemHandCards.CardGameBindingComponent.EMPTY)
                    .tablePos().orElse(null);
        }
        if (tablePos == null) return null;
        BlockState blockState = player.level().getBlockState(tablePos);
        if (!blockState.is(Mino.BLOCK_MINO_TABLE.get())) return null;
        return BlockMinoTable.getCore(blockState, tablePos);
    }

    public static int getClientHandIndex(Player player) {
        if (player.getMainHandItem().is(Mino.ITEM_HAND_CARDS.get())) {
            return player.getMainHandItem().getOrDefault(Mino.DATA_COMPONENT_TYPE_CLIENT_HAND_INDEX.get(), 0);
        } else {
            return 0;
        }
    }

    @Override
    public float getAttackDamageBonus(Entity target, float damage, DamageSource damageSource) {
        return super.getAttackDamageBonus(target, damage, damageSource);
    }

    public record CardGameBindingComponent(UUID player, Optional<BlockPos> tablePos) {
        public static final CardGameBindingComponent EMPTY = new CardGameBindingComponent(UUID.randomUUID(), Optional.empty());
        public static final Codec<CardGameBindingComponent> CODEC = RecordCodecBuilder.create(it -> it.group(
                UUIDUtil.CODEC.fieldOf("player").forGetter(CardGameBindingComponent::player),
                BlockPos.CODEC.optionalFieldOf("tablePos").forGetter(CardGameBindingComponent::tablePos)
        ).apply(it, CardGameBindingComponent::new));
        public static final StreamCodec<ByteBuf, CardGameBindingComponent> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, CardGameBindingComponent::player,
                BlockPos.STREAM_CODEC.apply(ByteBufCodecs::optional), CardGameBindingComponent::tablePos,
                CardGameBindingComponent::new
        );
    }
}
