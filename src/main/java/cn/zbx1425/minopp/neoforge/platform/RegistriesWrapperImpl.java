package cn.zbx1425.minopp.neoforge.platform;
//? if forgelike {

/*import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.platform.GroupedBlock;
import cn.zbx1425.minopp.platform.GroupedItem;
import cn.zbx1425.minopp.platform.RegistriesWrapper;
import cn.zbx1425.minopp.platform.RegistryObject;
import net.minecraft.client.KeyMapping;
//? if >=1.20.5
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
//? if forge
import net.neoforged.neoforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistriesWrapperImpl implements RegistriesWrapper {

    //? if neoforge {
    /^private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Mino.MOD_ID);
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, Mino.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Mino.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Mino.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Mino.MOD_ID);
    ^///? } else {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Mino.MOD_ID);
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Mino.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Mino.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Mino.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Mino.MOD_ID);
    //? }
    //? if >=1.20.5
    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, Mino.MOD_ID);

    @Override
    public void registerBlock(String id, RegistryObject<Block> block) {
        BLOCKS.register(id, block::get);
    }

    @Override
    public void registerBlockAndItem(String id, RegistryObject<GroupedBlock> block) {
        BLOCKS.register(id, block::get);
        ITEMS.register(id, () -> {
            final BlockItem blockItem = new BlockItem(block.get(),
                new Item.Properties()
                //? if >=1.21.2
                .setId(ResourceKey.create(Registries.ITEM, Mino.id(id)))
                //? if >=1.21.2
                .useBlockDescriptionPrefix()
            );
            registerCreativeModeTab(block.get().tabSupplier.get(), blockItem);
            return blockItem;
        });
    }

    @Override
    public void registerItem(String id, RegistryObject<Item> item) {
        ITEMS.register(id, () -> {
            final Item itemObject = item.get();
            if (itemObject instanceof GroupedItem groupedItem) {
                registerCreativeModeTab(groupedItem.getTab(), itemObject);
            }
            return itemObject;
        });
    }

    @Override
    public void registerBlockEntityType(String id, RegistryObject<? extends BlockEntityType<? extends BlockEntity>> blockEntityType) {
        BLOCK_ENTITY_TYPES.register(id, blockEntityType::get);
    }

    @Override
    public void registerEntityType(String id, RegistryObject<? extends EntityType<? extends Entity>> entityType) {
        ENTITY_TYPES.register(id, entityType::get);
    }

    @Override
    public void registerSoundEvent(String id, SoundEvent soundEvent) {
        SOUND_EVENTS.register(id, () -> soundEvent);
    }

    //? if >=1.20.5 {
    @Override
    public <T> void registerDataComponentType(String id, RegistryObject<DataComponentType<T>> componentType) {
        DATA_COMPONENT_TYPES.register(id, componentType::get);
    }
    //? }

    public final List<KeyMapping> keyMappings = new ArrayList<>();

    public void registerAllDeferred(IEventBus eventBus) {
        ITEMS.register(eventBus);
        BLOCKS.register(eventBus);
        BLOCK_ENTITY_TYPES.register(eventBus);
        ENTITY_TYPES.register(eventBus);
        SOUND_EVENTS.register(eventBus);
        //? if >=1.20.5
        DATA_COMPONENT_TYPES.register(eventBus);
    }


    private static final Map<ResourceKey<CreativeModeTab>, ArrayList<Item>> CREATIVE_TABS = new HashMap<>();

    public static void registerCreativeModeTab(ResourceKey<CreativeModeTab> resourceLocation, Item item) {
        CREATIVE_TABS.computeIfAbsent(resourceLocation, ignored -> new ArrayList<>()).add(item);
    }

    public static class RegisterCreativeTabs {

        @SubscribeEvent
        public static void onRegisterCreativeModeTabsEvent(BuildCreativeModeTabContentsEvent event) {
            CREATIVE_TABS.forEach((key, items) -> {
                if (event.getTabKey().equals(key)) {
                    items.forEach(item -> event.accept(new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS));
                }
            });
        }

    }
}

*///?}
