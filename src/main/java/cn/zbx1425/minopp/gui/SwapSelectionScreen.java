package cn.zbx1425.minopp.gui;

import cn.zbx1425.minopp.game.Card;
import cn.zbx1425.minopp.game.CardPlayer;
import cn.zbx1425.minopp.network.C2SPlayCardPacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

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
        this.allPlayers = allPlayers;
    }

    int BTN_SIZE = 20;
    int ROW_HEIGHT = 24;
    int MARGIN = 8;
    int NAME_WIDTH = 80;
    int CARD_BLOCK_WIDTH = 8;
    int CARD_BLOCK_HEIGHT = 12;
    int CARD_BLOCK_GAP = 1;

    int PANEL_WIDTH = 300;

    @Override
    protected void init() {
        super.init();
        clearWidgets();

        int panelHeight = MARGIN + 9 + MARGIN + ROW_HEIGHT * allPlayers.size() + MARGIN;
        int xOff = (width - PANEL_WIDTH) / 2;
        int yOff = (height - panelHeight) / 2;

        int y = yOff + MARGIN + 9 + MARGIN;
        for (CardPlayer player : allPlayers) {
            boolean isSelf = player.equals(self);
            int btnX = xOff + MARGIN;
            int btnY = y + (ROW_HEIGHT - BTN_SIZE) / 2;
            Button btn = Button.builder(Component.empty(), e -> {
                C2SPlayCardPacket.Client.sendPlayCardC2S(gamePos, self, handCard, null, shout, player.uuid);
                onClose();
            }).pos(btnX, btnY).size(BTN_SIZE, BTN_SIZE).build();
            btn.active = !isSelf;
            addRenderableWidget(btn);
            y += ROW_HEIGHT;
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);

        int panelHeight = MARGIN + 9 + MARGIN + ROW_HEIGHT * allPlayers.size() + MARGIN;
        int xOff = (width - PANEL_WIDTH) / 2;
        int yOff = (height - panelHeight) / 2;

        guiGraphics.fill(xOff + MARGIN, yOff + MARGIN, xOff + PANEL_WIDTH + MARGIN, yOff + panelHeight + MARGIN, 0x66000000);
        guiGraphics.fill(xOff, yOff, xOff + PANEL_WIDTH, yOff + panelHeight, 0xFF313031);
        guiGraphics.centeredText(font, title, width / 2, yOff + MARGIN, 0xFFFFFFFF);

        int y = yOff + MARGIN + 9 + MARGIN;
        int cardAreaX = xOff + MARGIN + BTN_SIZE + MARGIN + NAME_WIDTH + MARGIN;
        int cardAreaMaxWidth = PANEL_WIDTH - MARGIN - BTN_SIZE - MARGIN - NAME_WIDTH - MARGIN - MARGIN;

        for (CardPlayer player : allPlayers) {
            int nameY = y + (ROW_HEIGHT - 9) / 2;
            int nameX = xOff + MARGIN + BTN_SIZE + MARGIN;
            boolean isSelf = player.equals(self);
            int nameColor = isSelf ? 0xFFFFFF00 : 0xFFFFFFFF;
            guiGraphics.text(font, Component.literal(player.name), nameX, nameY, nameColor);

            int handSize = player.hand.size();
            int totalBlockWidth = handSize * (CARD_BLOCK_WIDTH + CARD_BLOCK_GAP) - CARD_BLOCK_GAP;
            int renderedCount;
            boolean overflow = false;
            if (totalBlockWidth > cardAreaMaxWidth - 20) {
                int maxWidth = cardAreaMaxWidth - 50;
                renderedCount = Math.max(1, (maxWidth + CARD_BLOCK_GAP) / (CARD_BLOCK_WIDTH + CARD_BLOCK_GAP));
                overflow = true;
            } else {
                renderedCount = handSize;
            }

            int blockY = y + (ROW_HEIGHT - CARD_BLOCK_HEIGHT) / 2;
            for (int i = 0; i < renderedCount; i++) {
                int blockX = cardAreaX + i * (CARD_BLOCK_WIDTH + CARD_BLOCK_GAP);
                guiGraphics.fill(blockX, blockY, blockX + CARD_BLOCK_WIDTH, blockY + CARD_BLOCK_HEIGHT, 0xFFAAAAAA);
            }
            if (overflow) {
                int remaining = handSize - renderedCount;
                int textX = cardAreaX + renderedCount * (CARD_BLOCK_WIDTH + CARD_BLOCK_GAP) + 4;
                guiGraphics.text(font, Component.literal("+" + remaining), textX, nameY, 0xFFCCCCCC);
            }

            y += ROW_HEIGHT;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
