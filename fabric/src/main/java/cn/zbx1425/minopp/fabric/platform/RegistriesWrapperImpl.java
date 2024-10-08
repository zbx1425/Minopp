package cn.zbx1425.minopp.fabric.platform;


import cn.zbx1425.minopp.platform.Main;
import cn.zbx1425.minopp.platform.item.GroupedItem;
import cn.zbx1425.minopp.platform.util.RegistriesWrapper;
import cn.zbx1425.minopp.platform.util.RegistryObject;
#if MC_VERSION >= "12000"
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
#else
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
#endif
import net.minecraft.client.KeyMapping;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class RegistriesWrapperImpl implements RegistriesWrapper {

    @Override
    public void registerBlock(String id, RegistryObject<Block> block) {
        Registry.register(RegistryUtilities.registryGetBlock(), Main.id(id), block.get());
    }

    @Override
    public void registerBlockAndItem(String id, RegistryObject<Block> block, #if MC_VERSION >= "12000" ResourceKey<CreativeModeTab> #else CreativeModeTab #endif tab) {
        Registry.register(RegistryUtilities.registryGetBlock(), Main.id(id), block.get());
#if MC_VERSION >= "12000"
        final BlockItem blockItem = new BlockItem(block.get(), new Item.Properties());
#else
        final BlockItem blockItem = new BlockItem(block.get(), new FabricItemSettings().group(tab));
#endif
        Registry.register(RegistryUtilities.registryGetItem(), Main.id(id), blockItem);
#if MC_VERSION >= "12000"
        ItemGroupEvents.modifyEntriesEvent(tab).register(consumer -> consumer.accept(blockItem));
#endif
    }

    @Override
    public void registerItem(String id, RegistryObject<GroupedItem> item) {
        Registry.register(RegistryUtilities.registryGetItem(), Main.id(id), item.get());
#if MC_VERSION >= "12000"
        ItemGroupEvents.modifyEntriesEvent(item.get().tabSupplier.get()).register(consumer -> consumer.accept(item.get()));
#endif
    }

    @Override
    public void registerBlockEntityType(String id, RegistryObject<? extends BlockEntityType<? extends BlockEntity>> blockEntityType) {
        Registry.register(RegistryUtilities.registryGetBlockEntityType(), Main.id(id), blockEntityType.get());
    }

    @Override
    public void registerEntityType(String id, RegistryObject<? extends EntityType<? extends Entity>> entityType) {
        Registry.register(RegistryUtilities.registryGetEntityType(), Main.id(id), entityType.get());
    }

    @Override
    public void registerSoundEvent(String id, SoundEvent soundEvent) {
        Registry.register(RegistryUtilities.registryGetSoundEvent(), Main.id(id), soundEvent);
    }
}