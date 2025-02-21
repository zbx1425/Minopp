package cn.zbx1425.minopp.forge.mixin;

import cn.zbx1425.minopp.item.ItemHandCards;
import cn.zbx1425.minopp.platform.GroupedItem;
import cn.zbx1425.minopp.render.HandCardsWithoutLevelRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(ItemHandCards.class)
public class ItemHandCardsMixin extends GroupedItem {

    public ItemHandCardsMixin(Supplier<ResourceKey<CreativeModeTab>> tabSupplier, Function<Properties, Properties> properties) {
        super(tabSupplier, properties);
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return HandCardsWithoutLevelRenderer.INSTANCE.get();
            }
        });
    }

}
