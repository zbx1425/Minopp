package cn.zbx1425.minopp.gui;

import cn.zbx1425.minopp.game.Card;
import cn.zbx1425.minopp.game.CardPlayer;
import cn.zbx1425.minopp.network.C2SPlayCardPacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class SwapSelectionScreen extends Screen {

    private final BlockPos gamePos;
    private final CardPlayer self;
    private final Card handCard;
    private final boolean shout;
    private final List<CardPlayer> allPlayers;

    public SwapSelectionScreen(BlockPos gamePos, CardPlayer self, Card handCard, boolean shout, List<CardPlayer> allPlayers) {
        super(Component.translatable("gui.minopp.swap_selection.title"));
        this.gamePos = gamePos;
        this.self = self;
        this.handCard = handCard;
        this.shout = shout;
        this.allPlayers = new ArrayList<>(allPlayers);
    }

    static final int BTN_SIZE = 20;
    static final int ROW_HEIGHT = 24;
    static final int MARGIN = 8;
    static final int CARD_BLOCK_W = 10;
    static final int CARD_BLOCK_H = 15;
    static final int CARD_BLOCK_GAP = 1;
    static final int NAME_COL_WIDTH = 80;

    int PANEL_WIDTH = 280;
    int PANEL_HEIGHT;

    @Override
    protected void init() {
        super.init();
        clearWidgets();

        PANEL_HEIGHT = MARGIN + 9 + MARGIN + ROW_HEIGHT * allPlayers.size() + MARGIN + BTN_SIZE + MARGIN;

        int xOff = (width - PANEL_WIDTH) / 2;
        int yOff = (height - PANEL_HEIGHT) / 2;

        int rowY = yOff + MARGIN + 9 + MARGIN;
        for (CardPlayer target : allPlayers) {
            int finalRowY = rowY;
            Button btn = addRenderableWidget(Button.builder(Component.literal(" "), e -> {
                C2SPlayCardPacket.Client.sendPlayCardC2S(gamePos, self, handCard, null, shout, target.uuid);
                onClose();
            }).pos(xOff + MARGIN, finalRowY).size(BTN_SIZE, BTN_SIZE).build());
            if (target.equals(self)) {
                btn.active = false;
            }
            rowY += ROW_HEIGHT;
        }

        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), e -> {
            onClose();
        }).pos(xOff + PANEL_WIDTH - MARGIN - 60, yOff + PANEL_HEIGHT - MARGIN - BTN_SIZE).size(60, BTN_SIZE).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);

        int xOff = (width - PANEL_WIDTH) / 2;
        int yOff = (height - PANEL_HEIGHT) / 2;
        guiGraphics.fill(xOff + MARGIN, yOff + MARGIN, xOff + PANEL_WIDTH + MARGIN, yOff + PANEL_HEIGHT + MARGIN, 0x66000000);
        guiGraphics.fill(xOff, yOff, xOff + PANEL_WIDTH, yOff + PANEL_HEIGHT, 0xFF313031);
        guiGraphics.fill(xOff, yOff + PANEL_HEIGHT - MARGIN - BTN_SIZE / 2, xOff + PANEL_WIDTH, yOff + PANEL_HEIGHT, 0x66546E7A);
        guiGraphics.centeredText(font, title, width / 2, yOff + MARGIN, 0xFFFFFFFF);

        int cardAreaRight = xOff + PANEL_WIDTH - MARGIN;
        int cardAreaMaxWidth = cardAreaRight - (xOff + MARGIN + BTN_SIZE + MARGIN + NAME_COL_WIDTH);

        int rowY = yOff + MARGIN + 9 + MARGIN;
        for (CardPlayer target : allPlayers) {
            int nameX = xOff + MARGIN + BTN_SIZE + MARGIN;
            int nameColor = target.equals(self) ? 0xFF888888 : 0xFFDDDDDD;
            guiGraphics.text(font, Component.literal(target.name), nameX, rowY + (BTN_SIZE - 9) / 2, nameColor);

            int cardBlockX = nameX + NAME_COL_WIDTH;
            int handSize = target.hand.size();
            int maxVisibleWidth = cardAreaMaxWidth - 20;
            int maxVisible = Math.max(1, maxVisibleWidth / (CARD_BLOCK_W + CARD_BLOCK_GAP));

            int visibleCount = Math.min(handSize, maxVisible);
            boolean overflow = handSize > visibleCount;

            for (int i = 0; i < visibleCount; i++) {
                int bx = cardBlockX + i * (CARD_BLOCK_W + CARD_BLOCK_GAP);
                guiGraphics.fill(bx, rowY + (BTN_SIZE - CARD_BLOCK_H) / 2,
                    bx + CARD_BLOCK_W, rowY + (BTN_SIZE - CARD_BLOCK_H) / 2 + CARD_BLOCK_H, 0xFF546E7A);
            }

            if (overflow) {
                int remaining = handSize - visibleCount;
                String overflowText = "+" + remaining;
                int textX = cardBlockX + visibleCount * (CARD_BLOCK_W + CARD_BLOCK_GAP) + 2;
                guiGraphics.text(font, Component.literal(overflowText), textX, rowY + (BTN_SIZE - 9) / 2, 0xFFAAAA00);
            }

            rowY += ROW_HEIGHT;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
