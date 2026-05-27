package cn.zbx1425.minopp.item;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.platform.GroupedItem;

public class ItemHandCardsNoBewlr extends GroupedItem {

    public ItemHandCardsNoBewlr() {
        super(p -> p.stacksTo(1), Mino.id("hand_cards_nobewlr"), () -> null);
    }
}
