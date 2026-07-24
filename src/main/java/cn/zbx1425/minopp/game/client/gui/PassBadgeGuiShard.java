package cn.zbx1425.minopp.game.client.gui;

import cn.zbx1425.minopp.game.Card;
import cn.zbx1425.minopp.game.client.shard.ShardResources;
import cn.zbx1425.minopp.gui.GameOverlayLayer;
import cn.zbx1425.minopp.platform.multiver.GuiShim;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

public class PassBadgeGuiShard extends BadgeGuiShard {

    @Override
    public Component getLabel() {
        return Component.literal(". . .");
    }
}
