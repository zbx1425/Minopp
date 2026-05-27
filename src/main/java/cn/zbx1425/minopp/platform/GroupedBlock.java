package cn.zbx1425.minopp.platform;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Function;
import java.util.function.Supplier;

public class GroupedBlock extends Block {

    public final Identifier id;
    public final Supplier<ResourceKey<CreativeModeTab>> tabSupplier;

    public GroupedBlock(
        Function<Properties, Properties> properties,
        Identifier id,
        Supplier<ResourceKey<CreativeModeTab>> tabSupplier
    ) {
        super(properties.apply(
            Properties.of()
                //? if >=1.21.2
                .setId(ResourceKey.create(Registries.BLOCK, id))
        ));
        this.id = id;
        this.tabSupplier = tabSupplier;
    }
}
