package cn.zbx1425.minopp.game.client.shard;

import cn.zbx1425.minopp.game.Card;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

public class ShardResources {

    public static ShardResources INSTANCE = new ShardResources();

    public TextureAtlas ITEM_ATLAS;
    public TextureAtlasSprite SPRITE_REDSTONE;
    public TextureAtlasSprite SPRITE_EMERALD;
    public TextureAtlasSprite SPRITE_DIAMOND;
    public TextureAtlasSprite SPRITE_GOLD;

    public void onResourceReload() {
        ITEM_ATLAS = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS);
        SPRITE_REDSTONE = ITEM_ATLAS.getSprite(Identifier.withDefaultNamespace("item/redstone"));
        SPRITE_EMERALD = ITEM_ATLAS.getSprite(Identifier.withDefaultNamespace("item/emerald"));
        SPRITE_DIAMOND = ITEM_ATLAS.getSprite(Identifier.withDefaultNamespace("item/diamond"));
        SPRITE_GOLD = ITEM_ATLAS.getSprite(Identifier.withDefaultNamespace("item/gold_ingot"));
    }

    public TextureAtlasSprite getSpriteForSuit(Card.Suit suit) {
        return switch (suit) {
            case RED -> SPRITE_REDSTONE;
            case GREEN -> SPRITE_EMERALD;
            case BLUE -> SPRITE_DIAMOND;
            case YELLOW -> SPRITE_GOLD;
            default -> ITEM_ATLAS.missingSprite();
        };
    }
}
