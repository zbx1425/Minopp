package cn.zbx1425.minopp.gui;

import cn.zbx1425.minopp.game.Card;
import cn.zbx1425.minopp.game.CardPlayer;
import cn.zbx1425.minopp.network.C2SPlayCardPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class WildSelectionScreen extends Screen {

    private final BlockPos gamePos;
    private final CardPlayer player;
    private final Card handCard;

    public WildSelectionScreen(BlockPos gamePos, CardPlayer player, Card handCard) {
        super(Component.translatable("gui.minopp.wild_selection.title"));
        this.gamePos = gamePos;
        this.player = player;
        this.handCard = handCard;
    }

    int BTN_WIDTH = 60;
    int BTN_HEIGHT = 20;
    int BTN_SPACING = 10;
    int MARGIN = 8;

    int PANEL_HEIGHT = MARGIN + 9 + MARGIN + BTN_HEIGHT + MARGIN + BTN_HEIGHT + MARGIN;
    int PANEL_WIDTH = MARGIN + BTN_WIDTH * 4 + BTN_SPACING * 3 + MARGIN;

    @Override
    protected void init() {
        super.init();
        int xOff = (width - PANEL_WIDTH) / 2;
        int yOff = (height - PANEL_HEIGHT) / 2;

        for (Card.Suit suit : Card.Suit.values()) {
            if (suit == Card.Suit.WILD) continue;
            addRenderableWidget(Button.builder(Component.translatable("game.minopp.card.suit." + suit.name().toLowerCase(), "").withColor(suit.color), e -> {
                C2SPlayCardPacket.Client.sendPlayCardC2S(gamePos, player, handCard, suit);
                onClose();
            }).pos(xOff + MARGIN + (BTN_WIDTH + BTN_SPACING) * suit.ordinal(), yOff + MARGIN + 9 + MARGIN).size(BTN_WIDTH, BTN_HEIGHT).build());
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), e -> {
            onClose();
        }).pos(xOff + MARGIN + (BTN_WIDTH + BTN_SPACING) * 2 + BTN_WIDTH / 2, yOff + MARGIN + 9 + MARGIN + BTN_HEIGHT + MARGIN).size(BTN_WIDTH, BTN_HEIGHT).build());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        int xOff = (width - PANEL_WIDTH) / 2;
        int yOff = (height - PANEL_HEIGHT) / 2;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, -100);
        guiGraphics.fill(xOff, yOff, xOff + PANEL_WIDTH, yOff + PANEL_HEIGHT, 0x70000000);
        guiGraphics.fill(xOff, yOff + PANEL_HEIGHT - BTN_HEIGHT, xOff + PANEL_WIDTH, yOff + PANEL_HEIGHT, 0x66546E7A);
        guiGraphics.drawCenteredString(font, title, width / 2, yOff + MARGIN, 0xFFFFFFFF);
        guiGraphics.pose().popPose();
    }
}
