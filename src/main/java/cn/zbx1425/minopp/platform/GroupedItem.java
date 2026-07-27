package cn.zbx1425.minopp.platform;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import java.util.function.Function;
import java.util.function.Supplier;

public interface GroupedItem {

    ResourceKey<CreativeModeTab> getTab();

    static Item.Properties buildProperties(Function<Item.Properties, Item.Properties> properties, Identifier id) {
        return properties.apply(
            new Item.Properties()
                //? if >=1.21.2
                .setId(ResourceKey.create(Registries.ITEM, id))
        );
    }
}

