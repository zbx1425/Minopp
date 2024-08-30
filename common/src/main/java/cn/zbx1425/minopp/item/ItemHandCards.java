package cn.zbx1425.minopp.item;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.game.CardPlayer;
import cn.zbx1425.minopp.platform.GroupedItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ItemHandCards extends GroupedItem {
    
    public ItemHandCards() {
        super(() -> null, p -> p
                .component(Mino.DATA_COMPONENT_TYPE_CARD_GAME_BINDING.get(), CardGameBindingComponent.EMPTY)
                .component(DataComponents.MAX_STACK_SIZE, 1)
                .component(DataComponents.CAN_PLACE_ON, new AdventureModePredicate(List.of(BlockPredicate.Builder.block().of(Mino.BLOCK_MINO_TABLE.get()).build()), false))
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
