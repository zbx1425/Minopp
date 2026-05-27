package cn.zbx1425.minopp.item;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.platform.GroupedItem;
import cn.zbx1425.minopp.platform.multiver.PlayerShim;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;
import java.util.function.Consumer;

public class ItemCoupon extends GroupedItem {

    public ItemCoupon() {
        super(p -> p, Mino.id("coupon"), () -> null);
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
}
