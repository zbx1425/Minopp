package cn.zbx1425.minopp.item;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.platform.GroupedItem;
import cn.zbx1425.minopp.platform.multiver.PlayerShim;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
//? if >=26.1
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;
import java.util.function.Consumer;

public class ItemCoupon extends Item implements GroupedItem {

    public ItemCoupon() {
        super(GroupedItem.buildProperties(p -> p, Mino.id("coupon")));
    }

    @Override
    //? if <26.1
    //public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
    //? if >=26.1
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final TooltipDisplay display, final Consumer<Component> builder, final TooltipFlag tooltipFlag) {
        //~ if >=26.1 'tooltipComponents.add(' -> 'builder.accept(' {
        builder.accept(Component.translatable("item.minopp.coupon.description"));
        //~ }
        //? if <26.1
        //super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        //? if >=26.1
        super.appendHoverText(stack, context, display, builder, tooltipFlag);
    }

    @Override
    public ResourceKey<CreativeModeTab> getTab() {
        return null;
    }
}
