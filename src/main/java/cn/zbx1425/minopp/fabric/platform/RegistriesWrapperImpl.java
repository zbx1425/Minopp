package cn.zbx1425.minopp.fabric.platform;
//? if fabric {

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.platform.GroupedBlock;
import cn.zbx1425.minopp.platform.GroupedItem;
import cn.zbx1425.minopp.platform.RegistriesWrapper;
import cn.zbx1425.minopp.platform.RegistryObject;
//? if <26.1
//import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
//? if >=26.1
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
//? if >=1.20.5
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
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
        Registry.register(BuiltInRegistries.BLOCK, Mino.id(id), block.get());
    }

    @Override
    public void registerBlockAndItem(String id, RegistryObject<GroupedBlock> block) {
        Registry.register(BuiltInRegistries.BLOCK, Mino.id(id), block.get());
        final BlockItem blockItem = new BlockItem(block.get(),
            new Item.Properties()
                //? if >=1.21.2
                .setId(ResourceKey.create(Registries.ITEM, Mino.id(id)))
                //? if >=1.21.2
                .useBlockDescriptionPrefix()
        );
        Registry.register(BuiltInRegistries.ITEM, Mino.id(id), blockItem);
        //? if <26.1
        //ItemGroupEvents.modifyEntriesEvent(block.get().tabSupplier.get()).register(consumer -> consumer.accept(blockItem));
        //? if >=26.1
        CreativeModeTabEvents.modifyOutputEvent(block.get().tabSupplier.get()).register(consumer -> consumer.accept(blockItem));
    }

    @Override
    public void registerItem(String id, RegistryObject<Item> item) {
        Registry.register(BuiltInRegistries.ITEM, Mino.id(id), item.get());
        if (!(item.get() instanceof GroupedItem groupedItem)) return;
        //? if <26.1
        //ItemGroupEvents.modifyEntriesEvent(groupedItem.getTab()).register(consumer -> consumer.accept(item.get()));
        //? if >=26.1
        CreativeModeTabEvents.modifyOutputEvent(groupedItem.getTab()).register(consumer -> consumer.accept(item.get()));
    }

    @Override
    public void registerBlockEntityType(String id, RegistryObject<? extends BlockEntityType<? extends BlockEntity>> blockEntityType) {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Mino.id(id), blockEntityType.get());
    }

    @Override
    public void registerEntityType(String id, RegistryObject<? extends EntityType<? extends Entity>> entityType) {
        Registry.register(BuiltInRegistries.ENTITY_TYPE, Mino.id(id), entityType.get());
    }

    @Override
    public void registerSoundEvent(String id, SoundEvent soundEvent) {
        Registry.register(BuiltInRegistries.SOUND_EVENT, Mino.id(id), soundEvent);
    }

    //? if >=1.20.5 {
    @Override
    public <T> void registerDataComponentType(String id, RegistryObject<DataComponentType<T>> componentType) {
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Mino.id(id), componentType.get());
    }
    //? }
}

//? }
