package cn.zbx1425.minopp;

import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.block.BlockMinoTable;
import cn.zbx1425.minopp.item.ItemHandCards;
import cn.zbx1425.minopp.network.C2SPlayCardPacket;
import cn.zbx1425.minopp.network.S2CActionEphemeralPacket;
import cn.zbx1425.minopp.platform.GroupedItem;
import cn.zbx1425.minopp.platform.RegistriesWrapper;
import cn.zbx1425.minopp.platform.RegistryObject;
import cn.zbx1425.minopp.platform.ServerPlatform;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public final class Mino {
    public static final String MOD_ID = "minopp";
    public static final Logger LOGGER = LoggerFactory.getLogger("Mino++");

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static final RegistryObject<Block> BLOCK_MINO_TABLE = new RegistryObject<>(BlockMinoTable::new);
    public static final RegistryObject<BlockEntityType<BlockEntityMinoTable>> BLOCK_ENTITY_TYPE_MINO_TABLE = new RegistryObject<>(() ->
            ServerPlatform.createBlockEntityType(BlockEntityMinoTable::new, BLOCK_MINO_TABLE.get()));

    public static final RegistryObject<GroupedItem> ITEM_HAND_CARDS = new RegistryObject<>(ItemHandCards::new);
    public static final RegistryObject<DataComponentType<ItemHandCards.CardGameBindingComponent>> DATA_COMPONENT_TYPE_CARD_GAME_BINDING = new RegistryObject<>(() ->
            ServerPlatform.createDataComponentType(ItemHandCards.CardGameBindingComponent.CODEC, ItemHandCards.CardGameBindingComponent.STREAM_CODEC));
    public static final RegistryObject<DataComponentType<Integer>> DATA_COMPONENT_TYPE_CLIENT_HAND_INDEX = new RegistryObject<>(() ->
            ServerPlatform.createDataComponentType(Codec.INT, ByteBufCodecs.INT));

    public static void init(RegistriesWrapper registries) {
        final ResourceKey<CreativeModeTab> FUNCTIONAL_BLOCKS = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
                ResourceLocation.withDefaultNamespace("functional_blocks"));
        registries.registerBlockAndItem("mino_table", BLOCK_MINO_TABLE, FUNCTIONAL_BLOCKS);
        registries.registerBlockEntityType("mino_table", BLOCK_ENTITY_TYPE_MINO_TABLE);
        registries.registerItem("hand_cards", ITEM_HAND_CARDS);
        registries.registerDataComponentType("card_game_binding", DATA_COMPONENT_TYPE_CARD_GAME_BINDING);
        registries.registerDataComponentType("client_hand_index", DATA_COMPONENT_TYPE_CLIENT_HAND_INDEX);

        ServerPlatform.registerPacket(S2CActionEphemeralPacket.ID);
        ServerPlatform.registerNetworkReceiver(C2SPlayCardPacket.ID, C2SPlayCardPacket::handleC2S);
    }
}
