package cn.zbx1425.minopp.item;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.platform.GroupedItem;
import net.minecraft.world.item.Item;

public class ItemHandCardsNoBewlr extends Item {

    public ItemHandCardsNoBewlr() {
        super(GroupedItem.buildProperties(p -> p.stacksTo(1), Mino.id("hand_cards_nobewlr")));
    }
}
