package cn.zbx1425.minopp.item;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.platform.GroupedItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.SpawnEggItem;

public class ItemAutoPlayer extends SpawnEggItem implements GroupedItem {

    private static final ResourceKey<CreativeModeTab> SPAWN_EGGS = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
        Identifier.withDefaultNamespace("spawn_eggs"));

    public ItemAutoPlayer() {
        super(GroupedItem.buildProperties(
            p -> p
                .stacksTo(1)
                .spawnEgg(Mino.ENTITY_AUTO_PLAYER.get())
                .overrideDescription("entity.minopp.mino_auto_player")
            ,
            Mino.id("auto_player")
        ));
    }

    @Override
    public ResourceKey<CreativeModeTab> getTab() {
        return SPAWN_EGGS;
    }
}
