package cn.zbx1425.minopp.item;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.platform.GroupedItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;
import java.util.UUID;

public class ItemHandCards extends GroupedItem {
    
    public ItemHandCards() {
        super(() -> null, p -> p.stacksTo(1)
                .component(Mino.DATA_COMPONENT_TYPE_CARD_GAME_BINDING.get(), COMPONENT_EMPTY));
    }

    public record CardGameBindingComponent(Optional<UUID> player, Optional<BlockPos> tablePos) {}
    public static final CardGameBindingComponent COMPONENT_EMPTY = new CardGameBindingComponent(Optional.empty(), Optional.empty());
    public static final Codec<CardGameBindingComponent> COMPONENT_CODEC = RecordCodecBuilder.create(it -> it.group(
        UUIDUtil.CODEC.optionalFieldOf("player").forGetter(CardGameBindingComponent::player),
        BlockPos.CODEC.optionalFieldOf("tablePos").forGetter(CardGameBindingComponent::tablePos)
    ).apply(it, CardGameBindingComponent::new));
    public static final StreamCodec<ByteBuf, CardGameBindingComponent> COMPONENT_STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional), CardGameBindingComponent::player,
        BlockPos.STREAM_CODEC.apply(ByteBufCodecs::optional), CardGameBindingComponent::tablePos,
        CardGameBindingComponent::new
    );
}
