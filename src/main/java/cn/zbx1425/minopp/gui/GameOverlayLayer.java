package cn.zbx1425.minopp.gui;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.MinoClient;
import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.block.BlockMinoTable;
import cn.zbx1425.minopp.block.MinoTableClientData;
import cn.zbx1425.minopp.entity.EntityAutoPlayer;
import cn.zbx1425.minopp.render.EntityAutoPlayerRenderer;
import cn.zbx1425.minopp.game.*;
import cn.zbx1425.minopp.game.client.gui.BadgeGuiShard;
import cn.zbx1425.minopp.game.client.gui.MessageGuiShard;
import cn.zbx1425.minopp.game.client.gui.PendingActionGuiShard;
import cn.zbx1425.minopp.game.client.gui.PlayBadgeGuiShard;
import cn.zbx1425.minopp.game.client.shard.ShardExtractor;
import cn.zbx1425.minopp.game.client.shard.ShardExtractors;
import cn.zbx1425.minopp.game.shard.ActionReportShard;
import cn.zbx1425.minopp.game.shard.ActionReportShards;
import cn.zbx1425.minopp.game.shard.GameWonShard;
import cn.zbx1425.minopp.item.ItemHandCards;
import cn.zbx1425.minopp.platform.multiver.GuiShim;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2FloatArrayMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
//? if >=1.21
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if <26.1
//import net.minecraft.client.gui.components.PlayerFaceRenderer;
//? if >=26.1
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

//? if <26.1 {
/*import com.mojang.blaze3d.systems.RenderSystem;
//? if >=1.21
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.util.FastColor;
*///? } else {

//? }

//? if >=26.1 && neoforge
//import net.neoforged.neoforge.client.gui.GuiLayer;

import java.util.*;
import java.util.function.Function;

//? if <1.21
//public class GameOverlayLayer {
//? if >=1.21 <26.1
//public class GameOverlayLayer implements LayeredDraw.Layer {
//? if >=26.1 && neoforge
//public class GameOverlayLayer implements GuiLayer {
//? if >=26.1 && !neoforge
public class GameOverlayLayer {

    private static final int BG_COLOR_SOLID = 0x888888;
    private static final int BG_COLOR_BACKDROP = 0x444444;
    private static final int BACKDROP_ALPHA = 0x66;

    private static final int AVATAR_SIZE = 16;
    private static final int NAME_PAD_X = 3;
    private static final int ROW_HEIGHT = 20;
    private static final String NAME_MEASURE = "WWW…WWW";


    //? if >=1.21 <26.1
    //@Override
    //? if <1.21 {
    /*public void render(@NotNull GuiGraphicsExtractor guiGraphics, float partialTick) {
    *///? } else {
    public void render(@NotNull GuiGraphicsExtractor guiGraphics, @NotNull DeltaTracker deltaTracker) {
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
    //? }
        LocalPlayer player = Minecraft.getInstance().player;
        BlockPos handCardGamePos = ItemHandCards.getHandCardGamePos(player);
        ClientLevel level = Minecraft.getInstance().level;
        BlockPos hitResultGamePos = BlockMinoTable.Client.getCursorPickedGame();
        BlockPos gamePos = (handCardGamePos != null) ? handCardGamePos : hitResultGamePos;
        if (gamePos == null) {
            TurnDeadMan.setOutsideGame();
            MinoClient.handCardOverlayActive = false;
            return;
        }
        BlockEntityMinoTable tableEntity = (BlockEntityMinoTable)level.getBlockEntity(gamePos);
        if (tableEntity == null) {
            TurnDeadMan.setOutsideGame();
            MinoClient.handCardOverlayActive = false;
            return;
        }

        if (tableEntity.game == null) {
            renderGameInactive(guiGraphics, tableEntity);
            TurnDeadMan.setOutsideGame();
            tableEntity.clientData.setZoomTarget(0);
        } else {
            TurnDeadMan.tick(tableEntity.game, partialTick);
            if (handCardGamePos == null || hitResultGamePos == null || Objects.equals(handCardGamePos, hitResultGamePos)) {
                renderGameActive(guiGraphics, tableEntity);
            } else {
                tableEntity.clientData.setZoomTarget(0);
            }
        }
        MinoClient.handCardOverlayActive = renderHandCards(guiGraphics, partialTick);
    }

    // ========== Game Inactive ==========

    private void renderGameInactive(GuiGraphicsExtractor g, BlockEntityMinoTable tableEntity) {
        if (Minecraft.getInstance().options.hideGui) return;
        int x = 20, y = 60;
        Font font = Minecraft.getInstance().font;

        Function<UUID, String> nameResolver = buildNameResolver(null, tableEntity);
        boolean hasGameWon = tableEntity.stateShards.stream().anyMatch(s -> s instanceof GameWonShard);
        if (hasGameWon) {
            List<CardPlayer> playerList = tableEntity.getPlayersList();
            y = renderShardPanel(g, font, x, y, tableEntity, tableEntity.game, playerList, null, null, nameResolver);
        } else {
            for (ActionReportShard shard : tableEntity.stateShards) {
                ShardExtractor<ActionReportShard> ext = ShardExtractors.getUnchecked(shard.shardType());
                if (ext != null) {
                    for (MessageGuiShard msg : ext.extractMessages(shard, nameResolver)) {
                        msg.render(g, font, x, y, msg.color, 0xFF);
                        y += msg.getAdvance(font);
                    }
                }
            }
        }

        y += font.lineHeight;
        drawStringWithBackdrop(g, font, Component.translatable("gui.minopp.play.start_hint"), x, y, 0xFF00DD55);
        y += font.lineHeight * 2;

        int rulesWidth = getGameRulesWidth(font);
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int rulesRightEdge = screenWidth - 20;
        int rulesLeftEdge = rulesRightEdge - rulesWidth;
        if (rulesLeftEdge > 200) {
            renderGameRules(g, font, tableEntity.rules, false, rulesRightEdge, 60);
        } else {
            renderGameRules(g, font, tableEntity.rules, true, x, y);
        }
    }

    // ========== Game Active ==========

    private void renderGameActive(GuiGraphicsExtractor g, BlockEntityMinoTable tableEntity) {
        LocalPlayer player = Minecraft.getInstance().player;
        CardPlayer cardPlayer = ItemHandCards.getCardPlayer(player);
        CardPlayer currentPlayer = tableEntity.game.players.get(tableEntity.game.currentPlayerIndex);
        MinoTableClientData clientData = tableEntity.clientData;
        if (currentPlayer.equals(cardPlayer)) {
            if (tableEntity.game.currentPlayerPhase == CardGame.PlayerActionPhase.DISCARD_HAND) {
                clientData.setZoomTarget(1);
            } else {
                if (clientData.getZoomTarget() < 1.01) {
                    clientData.setZoomTarget(1.5);
                } else if (clientData.getZoomProgress() >= 1.5) {
                    clientData.setZoomTarget(1.05);
                }
            }
        } else {
            clientData.setZoomTarget(0);
        }

        if (Minecraft.getInstance().options.hideGui) return;
        int x = 20, y = 60;
        Font font = Minecraft.getInstance().font;

        Function<UUID, String> nameResolver = buildNameResolver(tableEntity.game, tableEntity);

        // --- Status lines ---
        drawStringWithBackdrop(g, font, Component.translatable("gui.minopp.play.game_active").append(" © Zbx1425"), x, y, 0xFF7090FF);
        y += font.lineHeight;
        if (currentPlayer.equals(cardPlayer)) {
            if (tableEntity.game.currentPlayerPhase == CardGame.PlayerActionPhase.DISCARD_DRAWN
                    && tableEntity.game.forcePlayCard != null) {
                drawStringWithBackdrop(g, font, Component.translatable("gui.minopp.play.discard_drawn_force",
                        tableEntity.game.forcePlayCard.getDisplayName()), x, y, 0xFFAAAA00);
            } else if (tableEntity.game.currentPlayerPhase == CardGame.PlayerActionPhase.DISCARD_HAND
                    && tableEntity.game.drawCount > 0 && !tableEntity.rules.stackingEnabled()) {
                drawStringWithBackdrop(g, font, Component.translatable("gui.minopp.play.draw"),
                    x, y, 0xFFFFAA00);
            } else {
                drawStringWithBackdrop(g, font, Component.translatable("gui.minopp.play." + tableEntity.game.currentPlayerPhase.name().toLowerCase()),
                    x, y, 0xFFFFAA00);
            }
        } else {
            drawStringWithBackdrop(g, font, Component.translatable("gui.minopp.play.turn_other", currentPlayer.name), x, y, 0xFFAAAAAA);
        }
        y += font.lineHeight * 2;

        // --- Shard panel or Game rules ---
        if (player != null && player.isShiftKeyDown()) {
            renderGameRules(g, font, tableEntity.rules, true, x, y);
        } else {
            UUID selfUuid = player != null ? player.getUUID() : null;
            y = renderShardPanel(g, font, x, y, tableEntity, tableEntity.game, tableEntity.game.players, currentPlayer, selfUuid, nameResolver);
        }

        // --- Cursor hints ---
        renderCursorHints(g, font, tableEntity, cardPlayer, currentPlayer);
    }

    // ========== Shared Shard Panel: badges + messages ==========

    private static final long MAX_EPHEMERAL_DURATION_MS = 8000;

    private int renderShardPanel(GuiGraphicsExtractor g, Font font, int x, int y,
                                  @NotNull BlockEntityMinoTable tableEntity, @Nullable CardGame game,
                                  @NotNull List<CardPlayer> players, @Nullable CardPlayer currentPlayer,
                                  UUID selfUuid, Function<UUID, String> nameResolver) {

        int nameWidth = font.width(NAME_MEASURE) + NAME_PAD_X * 2;
        boolean hasGameWon = tableEntity.stateShards.stream().anyMatch(s -> s instanceof GameWonShard);

        // 1: Current state badges
        Map<UUID, List<BadgeGuiShard>> currentBadges = new LinkedHashMap<>();
        List<MessageGuiShard> currentMessages = new ArrayList<>();
        for (ActionReportShard shard : tableEntity.stateShards) {
            ShardExtractor<ActionReportShard> ext = ShardExtractors.getUnchecked(shard.shardType());
            if (ext != null) {
                if (!hasGameWon || shard.shardType() == ActionReportShards.GAME_WON) {
                    mergeBadges(currentBadges, ext.extractBadges(shard));
                }
                currentMessages.addAll(ext.extractMessages(shard, nameResolver));
            }
        }

        // 2: Top card badge
        Map<UUID, List<BadgeGuiShard>> topCardBadges = new LinkedHashMap<>();
        if (!hasGameWon && game != null) {
            boolean currentHasPlay = tableEntity.stateShards.stream()
                    .anyMatch(s -> s instanceof cn.zbx1425.minopp.game.shard.PlayShard);
            if (!currentHasPlay) {
                UUID badgeOwner = game.topCardPlayer != null
                        ? game.topCardPlayer
                        : game.players.get(game.currentPlayerIndex).uuid;
                topCardBadges.put(badgeOwner, List.of(new PlayBadgeGuiShard(game.topCard)));
            }
        }

        // 3: Ephemeral/Noteworthy badges/messages
        long currentTime = System.currentTimeMillis();
        Map<UUID, List<Pair<BadgeGuiShard, Integer>>> noteworthyBadges = new LinkedHashMap<>();
        List<Pair<MessageGuiShard, Integer>> noteworthyMessages = new ArrayList<>();
        List<Pair<ActionReportShard, Long>> ephemeralShards = tableEntity.clientData.getEphemeralShards();
        for (ListIterator<Pair<ActionReportShard, Long>> it =
                     ephemeralShards.listIterator(ephemeralShards.size()); it.hasPrevious(); ) {
            Pair<ActionReportShard, Long> entry = it.previous();
            long insertTime = entry.getSecond();
            if (currentTime - insertTime > MAX_EPHEMERAL_DURATION_MS) {
                it.remove();
                continue;
            }
            ShardExtractor<ActionReportShard> ext = ShardExtractors.getUnchecked(entry.getFirst().shardType());
            if (ext != null) {
                Map<UUID, List<BadgeGuiShard>> badges = ext.extractBadges(entry.getFirst());
                for (Map.Entry<UUID, List<BadgeGuiShard>> e : badges.entrySet()) {
                    noteworthyBadges.computeIfAbsent(e.getKey(), k -> new ArrayList<>());
                    for (BadgeGuiShard badge : e.getValue()) {
                        long remaining = insertTime + badge.getEphemeralDurationMs() - currentTime;
                        if (remaining <= 0) continue;
                        float alphaF = Mth.clamp((float) remaining / (badge.getEphemeralDurationMs() * 0.5f), 0, 1f);
                        if (alphaF < 0.1f) continue;
                        noteworthyBadges.get(e.getKey()).add(new Pair<>(badge, (int)(alphaF * 0xFF)));
                    }
                }
                for (MessageGuiShard msg : ext.extractMessages(entry.getFirst(), nameResolver)) {
                    long remaining = insertTime + msg.getEphemeralDurationMs() - currentTime;
                    if (remaining <= 0) continue;
                    float alphaF = Mth.clamp((float) remaining / (msg.getEphemeralDurationMs() * 0.5f), 0, 1f);
                    if (alphaF < 0.1f) continue;
                    noteworthyMessages.add(new Pair<>(msg, (int)(alphaF * 0xFF)));
                }
            }
        }

        // --- Render player rows ---
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        double zoomProg = tableEntity.clientData.getZoomProgress();
        int handCardWidth = (int)(100.0 * Mth.lerp(Mth.clamp(zoomProg, 0, 1.5), 0.93, 1.0));
        int badgeRightLimit = screenWidth - 10 - handCardWidth - 35;

        boolean isFirstDiscard = game != null && game.currentPlayerPhase == CardGame.PlayerActionPhase.DISCARD_HAND;
        boolean isSecondDiscard = game != null && game.currentPlayerPhase == CardGame.PlayerActionPhase.DISCARD_DRAWN;
        boolean hasBadges = !currentBadges.isEmpty() || !topCardBadges.isEmpty() || !noteworthyBadges.isEmpty()
                || (currentPlayer != null && isFirstDiscard);
        if (!players.isEmpty() && hasBadges) {
            int highlightColor = (currentPlayer != null && currentPlayer.uuid.equals(selfUuid))
                    ? 0xFFFFFF00 : 0xFF0094DA;
            for (CardPlayer p : players) {
                int rowX = x;
                boolean isCurrent = p.equals(currentPlayer);

                if (game != null) {
                    if (isCurrent) {
                        // Direction arrow
                        GuiShim.blit(g, ATLAS_LOCATION, x + (16 - 10) / 2, y + (game.isAntiClockwise ? 0 : (16 - 10)),
                            game.isAntiClockwise ? 208 : 218, 25, 10, 10, 256, 128);
                        // Draw count
                        if (game.drawCount > 1) {
                            GuiShim.drawCenteredString(g, font, "+" + game.drawCount, x + (16 / 2), y + (game.isAntiClockwise ? (12 - font.lineHeight / 2) : (4 - font.lineHeight / 2)), 0xFFFFAAAA);
                        }
                    }
                    rowX += 16;
                }

                if (isCurrent) {
                    g.fill(rowX - 1, y - 1, rowX + AVATAR_SIZE + 1, y + AVATAR_SIZE + 1, highlightColor);
                }
                Identifier skinTexture = resolveSkinTexture(p.uuid, tableEntity);
                //? if <26.1
                //PlayerFaceRenderer.draw(g, skinTexture, rowX, y, AVATAR_SIZE, true, false);
                //? if >=26.1
                PlayerFaceExtractor.extractRenderState(g, skinTexture, rowX, y, AVATAR_SIZE, true, false, -1);
                rowX += AVATAR_SIZE;

                Component nameComp = truncateName(p.name, font, nameWidth - NAME_PAD_X * 2);
                g.fill(rowX + (isCurrent ? 1 : 0), y, rowX + nameWidth, y + AVATAR_SIZE, BACKDROP_ALPHA << 24);
                GuiShim.drawString(g, font, nameComp, rowX + NAME_PAD_X, y + (AVATAR_SIZE - font.lineHeight) / 2,
                        isCurrent ? 0xFFFFFFFF : 0xFFAAAAAA, true);
                rowX += nameWidth;

                // Order: PendingAction → Current → Sticky → Ephemeral (newest leftmost)
                boolean isSecondDiscardAndPulsing = isSecondDiscard && currentPlayer.uuid.equals(selfUuid)
                    && ((Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0) % 40 >= 32);
                if (isCurrent && (isFirstDiscard || isSecondDiscardAndPulsing)) {
                    PendingActionGuiShard.INSTANCE.render(g, font, rowX, y, highlightColor, 0xFF);
                    rowX += PendingActionGuiShard.INSTANCE.getAdvance(font);
                }
                for (BadgeGuiShard badge : currentBadges.getOrDefault(p.uuid, List.of()).stream().skip(isSecondDiscardAndPulsing ? 1 : 0).toList()) {
                    if (rowX + badge.getAdvance(font) > badgeRightLimit) break;
                    int tint = badge instanceof PlayBadgeGuiShard ? PlayBadgeGuiShard.BG_PLAY : BG_COLOR_SOLID;
                    badge.render(g, font, rowX, y, tint, 0xFF);
                    rowX += badge.getAdvance(font);
                }
                for (BadgeGuiShard badge : topCardBadges.getOrDefault(p.uuid, List.of())) {
                    if (rowX + badge.getAdvance(font) > badgeRightLimit) break;
                    int tint = badge instanceof PlayBadgeGuiShard ? PlayBadgeGuiShard.BG_PLAY : BG_COLOR_SOLID;
                    badge.render(g, font, rowX, y, tint, 0xFF);
                    rowX += badge.getAdvance(font);
                }
                for (Pair<BadgeGuiShard, Integer> entry : noteworthyBadges.getOrDefault(p.uuid, List.of())) {
                    if (rowX + entry.getFirst().getAdvance(font) > badgeRightLimit) break;
                    entry.getFirst().render(g, font, rowX, y, BG_COLOR_BACKDROP, entry.getSecond());
                    rowX += entry.getFirst().getAdvance(font);
                }

                y += ROW_HEIGHT;
            }
            y += font.lineHeight;
        }

        // --- Message list ---
        for (MessageGuiShard msg : currentMessages) {
            msg.render(g, font, x, y, msg.color, 0xFF);
            y += msg.getAdvance(font);
        }

        for (Pair<MessageGuiShard, Integer> entry : noteworthyMessages) {
            if (y > Minecraft.getInstance().getWindow().getGuiScaledHeight() - font.lineHeight - 40) break;
            MessageGuiShard msg = entry.getFirst();
            int tintColor = msg.preserveColor ? msg.color : 0xAAAAAA;
            msg.render(g, font, x, y, tintColor, entry.getSecond());
            y += msg.getAdvance(font);
        }

        return y;
    }

    // ========== Cursor Hints ==========

    private static void renderCursorHints(GuiGraphicsExtractor g, Font font,
                                           BlockEntityMinoTable tableEntity, CardPlayer cardPlayer, CardPlayer currentPlayer) {
        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int x = width / 2 + 8;
        int y = height / 2;

        if (Minecraft.getInstance().hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = ((BlockHitResult) Minecraft.getInstance().hitResult).getBlockPos();
            BlockState hitState = Minecraft.getInstance().level.getBlockState(hitPos);
            if (hitState.is(Mino.BLOCK_MINO_TABLE.get())) {
                boolean isPass = BlockMinoTable.Client.isCursorHittingPile();
                if (currentPlayer.equals(cardPlayer)) {
                    Component cursorMessage = switch (tableEntity.game.currentPlayerPhase) {
                        case DISCARD_HAND -> isPass ? Component.translatable("gui.minopp.play.cursor.pass_draw")
                                : Component.translatable("gui.minopp.play.cursor.play");
                        case DISCARD_DRAWN -> isPass ? Component.translatable("gui.minopp.play.cursor.pass")
                                : Component.translatable("gui.minopp.play.cursor.play");
                    };
                    Component shoutMessage = Component.translatable("gui.minopp.play.cursor.shout");
                    boolean isShouting = !isPass && BlockMinoTable.Client.isShoutModifierHeld();
                    float pulseProgress = isPass ? (System.currentTimeMillis() % 1000) / 1000.0f : 1.0f;
                    int bgColor = lerpColor(0x80AAAA66, 0x80000000, pulseProgress);
                    int textColor = lerpColor(0xFF222222, 0xFFFFFFDD, pulseProgress);
                    int msgWidth = Math.max(font.width(cursorMessage), isShouting ? font.width(shoutMessage) : 0);
                    int msgHeight = isShouting ? font.lineHeight * 2 : font.lineHeight;
                    y -= msgHeight / 2;
                    g.fill(x, y - 2, x + msgWidth + 8, y + msgHeight + 3, bgColor);
                    GuiShim.drawString(g, font, cursorMessage, x + 4, y, textColor);
                    if (isShouting) {
                        GuiShim.drawString(g, font, shoutMessage, x + 4, y + font.lineHeight, textColor);
                    }
                    y += msgHeight + 6;
                }
            }
        }

        if (TurnDeadMan.isAlarmActive()) {
            g.fill(x + 1, y + 1, x + 48, y + 48, 0xFF000000);
            g.fill(x, y, x + 47, y + 47, cycleColor(20, 16, 0xFFEDEDED, 0xFFF5F47C));
            GuiShim.blit(g, ATLAS_LOCATION, x, y, 47, 47, 208, 50, 47, 47, 256, 128);
        }
    }

    // ========== Game Rules Panel ==========

    private static final int RULE_SQUARE_SIZE = 12;
    private static final int RULE_SQUARE_TEXT_GAP = 5;
    private static final int RULE_ROW_SPACING = 2;
    private static final int RULE_ROW_HEIGHT = RULE_SQUARE_SIZE + RULE_ROW_SPACING;

    private static final String[] RULE_KEYS = {
        "gui.minopp.table_rules.stacking",
        "gui.minopp.table_rules.jump_in",
        "gui.minopp.table_rules.seven_rule",
        "gui.minopp.table_rules.zero_rule",
        "gui.minopp.table_rules.draw_until_match",
        "gui.minopp.table_rules.force_play",
        "gui.minopp.table_rules.wd4_free"
    };

    private static boolean[] getRuleValues(TableRuleConfig rules) {
        return new boolean[] {
            rules.stackingEnabled(),
            rules.jumpInEnabled(),
            rules.sevenRuleEnabled(),
            rules.zeroRuleEnabled(),
            rules.drawUntilMatch(),
            rules.forcePlay(),
            rules.wildDrawFourFreeUse()
        };
    }

    private static int getGameRulesWidth(Font font) {
        int maxTextWidth = 0;
        for (String key : RULE_KEYS) {
            maxTextWidth = Math.max(maxTextWidth, font.width(Component.translatable(key)));
        }
        int titleWidth = font.width(Component.translatable("gui.minopp.table_rules.title"));
        int rowWidth = RULE_SQUARE_SIZE + RULE_SQUARE_TEXT_GAP + maxTextWidth;
        return Math.max(rowWidth, titleWidth);
    }

    private static void renderGameRules(GuiGraphicsExtractor g, Font font,
                                         TableRuleConfig rules, boolean leftSide, int x, int y) {
        Component title = Component.translatable("gui.minopp.table_rules.title");
        boolean[] values = getRuleValues(rules);
        int textYOffset = (RULE_SQUARE_SIZE - font.lineHeight) / 2;

        if (leftSide) {
            GuiShim.drawString(g, font, title, x, y, 0xFF7090FF, true);
            y += 12 + RULE_ROW_SPACING;
            for (int i = 0; i < RULE_KEYS.length; i++) {
                boolean enabled = values[i];
                int squareColor = enabled ? 0xFF55AA55 : 0xFF444444;
                g.fill(x + 1, y + 1, x + 1 + RULE_SQUARE_SIZE, y + 1 + RULE_SQUARE_SIZE, 0xFF000000);
                g.fill(x, y, x + RULE_SQUARE_SIZE, y + RULE_SQUARE_SIZE, squareColor);
                int textColor = enabled ? 0xFFFFFFFF : 0xFF888888;
                GuiShim.drawString(g, font, Component.translatable(RULE_KEYS[i]),
                    x + RULE_SQUARE_SIZE + RULE_SQUARE_TEXT_GAP, y + textYOffset, textColor, true);
                y += RULE_ROW_HEIGHT;
            }
        } else {
            GuiShim.drawString(g, font, title, x - font.width(title), y, 0xFF7090FF, true);
            y += 12 + RULE_ROW_SPACING;
            for (int i = 0; i < RULE_KEYS.length; i++) {
                boolean enabled = values[i];
                int squareColor = enabled ? 0xFF55AA55 : 0xFF444444;
                int squareX = x - RULE_SQUARE_SIZE;
                g.fill(squareX + 1, y + 1, squareX + 1 + RULE_SQUARE_SIZE, y + 1 + RULE_SQUARE_SIZE, 0xFF000000);
                g.fill(squareX, y, squareX + RULE_SQUARE_SIZE, y + RULE_SQUARE_SIZE, squareColor);
                int textColor = enabled ? 0xFFFFFFFF : 0xFF888888;
                Component text = Component.translatable(RULE_KEYS[i]);
                GuiShim.drawString(g, font, text, squareX - RULE_SQUARE_TEXT_GAP - font.width(text), y + textYOffset, textColor, true);
                y += RULE_ROW_HEIGHT;
            }
        }
    }

    // ========== Helpers ==========

    private static int lerpColor(int from, int to, float t) {
        int a = (int)Mth.lerp(t, (from >> 24) & 0xFF, (to >> 24) & 0xFF);
        int r = (int)Mth.lerp(t, (from >> 16) & 0xFF, (to >> 16) & 0xFF);
        int g = (int)Mth.lerp(t, (from >> 8) & 0xFF, (to >> 8) & 0xFF);
        int b = (int)Mth.lerp(t, from & 0xFF, to & 0xFF);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int cycleColor(int cycleTicks, int aTicks, int a, int b) {
        return ((Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0) % cycleTicks >= aTicks) ? b : a;
    }

    private static Function<UUID, String> buildNameResolver(CardGame game, BlockEntityMinoTable tableEntity) {
        return uuid -> {
            if (game != null) {
                for (CardPlayer p : game.players) {
                    if (p.uuid.equals(uuid)) return p.name;
                }
            }
            for (CardPlayer p : tableEntity.getPlayersList()) {
                if (p.uuid.equals(uuid)) return p.name;
            }
            return uuid.toString().substring(0, 8);
        };
    }

    private static Component truncateName(String name, Font font, int maxWidth) {
        if (font.width(name) <= maxWidth) {
            return Component.literal(name);
        }
        if (name.length() <= 6) {
            return Component.literal(name);
        }
        return Component.literal(name.substring(0, 3) + "…" + name.substring(name.length() - 3));
    }

    private static void mergeBadges(Map<UUID, List<BadgeGuiShard>> target, Map<UUID, List<BadgeGuiShard>> source) {
        for (Map.Entry<UUID, List<BadgeGuiShard>> e : source.entrySet()) {
            target.computeIfAbsent(e.getKey(), k -> new ArrayList<>()).addAll(e.getValue());
        }
    }

    private static Identifier resolveSkinTexture(UUID uuid, BlockEntityMinoTable tableEntity) {
        var connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            PlayerInfo info = connection.getPlayerInfo(uuid);
            if (info != null) {
                //? if <1.20.2
                //return info.getSkinLocation();
                //? if >=1.20.2 <26.1
                //return info.getSkin().texture();
                //? if >=26.1
                return info.getSkin().body().texturePath();
            }
        }

        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            AABB searchArea = AABB.ofSize(Vec3.atCenterOf(tableEntity.getBlockPos()), 20, 20, 20);
            for (EntityAutoPlayer autoPlayer : level.getEntitiesOfClass(EntityAutoPlayer.class, searchArea)) {
                if (autoPlayer.getUUID().equals(uuid)) {
                    //? if <26.1
                    //return EntityAutoPlayerRenderer.resolveClientSkinTexture(autoPlayer);
                    //? if >=26.1
                    return EntityAutoPlayerRenderer.resolveClientSkinTexture(autoPlayer);
                }
            }
        }

        //? if <1.20.2
        //return DefaultPlayerSkin.getDefaultSkin(uuid);
        //? if >=1.20.2 <26.1
        //return DefaultPlayerSkin.get(uuid).texture();
        //? if >=26.1
        return DefaultPlayerSkin.get(uuid).body().texturePath();
    }

    private static void drawStringWithBackdrop(GuiGraphicsExtractor guiGraphics, Font font, Component component, int x, int y, int color) {
        int i = (int)(0.4 * 255.0F) << 24 & -16777216;
        guiGraphics.fill(x - 2, y, x + font.width(component) + 2, y + font.lineHeight, i);
        guiGraphics.text(font, component, x, y, color, true);
    }

    // ========== Hand Cards ==========

    public static final Identifier ATLAS_LOCATION = Mino.id("textures/gui/deck.png");

    private boolean renderHandCards(GuiGraphicsExtractor g, float partialTick) {
        if (Minecraft.getInstance().options.hideGui) return false;

        Font font = Minecraft.getInstance().font;
        LocalPlayer player = Minecraft.getInstance().player;
        ClientLevel level = Minecraft.getInstance().level;
        BlockPos gamePos = ItemHandCards.getHandCardGamePos(player);
        if (gamePos == null) return false;
        BlockEntityMinoTable tableEntity = (BlockEntityMinoTable)level.getBlockEntity(gamePos);
        CardPlayer playerWithoutHand = ItemHandCards.getCardPlayer(player);

        if (tableEntity.game == null) return false;
        CardPlayer realPlayer = tableEntity.game.players.stream().filter(p -> p.equals(playerWithoutHand)).findFirst().orElse(null);
        if (realPlayer == null) return false;
        realPlayer.hand.sort(Card::compareTo);
        int clientHandIndex = Mth.clamp(ItemHandCards.getClientHandIndex(player), 0, Math.max(realPlayer.hand.size() - 1, 0));

        MinoTableClientData clientData = tableEntity.clientData;
        float delta = partialTick;
        clientData.tickAnimations(delta);

        MinoTableClientData.HandRenderState state = clientData.getHandRenderState(realPlayer, clientHandIndex);

        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        renderCardList(g, font, state, clientData, width, height, delta);

        MinoClient.globalFovModifier = Mth.lerp(Mth.clamp(clientData.getZoomProgress(), 0, 1), 1.0, 0.97);
        return true;
    }

    private void renderCardList(GuiGraphicsExtractor g, Font font,
                                MinoTableClientData.HandRenderState state,
                                MinoTableClientData clientData,
                                int screenWidth, int screenHeight, float deltaPartialTick) {
        List<Card> cards = state.cards();
        LongArrayList cardHashes = state.cardHashes();
        double zoomProg = state.zoomProgress();
        float yOffset = state.yOffset();
        float spacingMul = state.spacingMultiplier();
        int selectedIndex = state.selectedIndex();
        Set<Long> highlightHashes = state.highlightHashes();
        boolean useXOff = state.useXOffAnimation();

        final float cardSpacing = 20f * spacingMul;
        final int CARD_WIDTH = (int)(100.0 * Mth.lerp(zoomProg, 0.93, 1.0));
        final int CARD_HEIGHT = (int)(CARD_WIDTH * 8.9 / 5.6);

        Long2FloatArrayMap xOffMap = useXOff ? clientData.getHandCardXOff() : null;
        if (xOffMap != null) {
            xOffMap.keySet().removeIf(hash -> !cardHashes.contains(hash));
        }

        //? if <26.1
        //RenderSystem.enableBlend();

        int handSize = cards.size();
        if (handSize == 0) return;
        int selectedCardYRaw = screenHeight - (int)((CARD_HEIGHT / 2f) + cardSpacing * (handSize - Math.max(selectedIndex, 0)));
        int cardDrawOffset = selectedCardYRaw < 20 ? 20 - selectedCardYRaw : 0;
        Random cardRandom = new Random(handSize);
        for (int i = 0; i < handSize; i++) {
            int targetXOff = (i == selectedIndex ? -30 : 0) + cardRandom.nextInt(-3, 4);
            float currentXOff;
            if (xOffMap != null) {
                currentXOff = xOffMap.computeIfAbsent(cardHashes.getLong(i), ignored -> CARD_WIDTH + 10);
                xOffMap.put(cardHashes.getLong(i),
                        (float) Mth.lerp(8 * 0.05 * deltaPartialTick, currentXOff, targetXOff));
            } else {
                currentXOff = targetXOff;
            }
            int x = screenWidth - 10 - CARD_WIDTH + (int) currentXOff;
            int y = screenHeight - (int)((CARD_HEIGHT / 2f) + cardSpacing * (handSize - i)) + cardDrawOffset + (int) yOffset;
            if (i == selectedIndex) {
                Card card = cards.get(i);
                Component cardName = card.getDisplayName();
                GuiShim.drawString(g, font, cardName, x - font.width(cardName) - 10, y + 10, 0xFFFFFFDD);
            }
            boolean isHighlighted = highlightHashes.contains(cardHashes.getLong(i));
            if (isHighlighted) {
                float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.006) + 1) / 2f;
                int r = (int) (0xFF * (1 - pulse) + 0xCC * pulse);
                int borderColor = 0xFF000000 | (r << 16);
                g.fill(x - 3, y - 3, x + CARD_WIDTH + 3, y + CARD_HEIGHT + 3, borderColor);
            }
            g.fill(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, 0xFF222222);
            g.fill(x + 1, y + 1, x + CARD_WIDTH - 1, y + CARD_HEIGHT - 1, 0xFFDDDDDD);

            Card card = cards.get(i);
            float cardU = switch (card.family) {
                case NUMBER -> Math.abs(card.number) * 16;
                case SKIP -> 160;
                case DRAW -> 176;
                case REVERSE -> 192;
            };
            float cardV = card.suit.ordinal() * 25;
            int cardUW = 16;
            int cardVH = 25;

            float shadowAlpha = (float) Math.max(Mth.lerp(zoomProg, 0.5, 0), 0);

            GuiShim.blit(g,
                ATLAS_LOCATION,
                x + 5, y + 5, CARD_WIDTH - 10, CARD_HEIGHT - 10,
                    cardU + 1, cardV + 1, cardUW - 2, cardVH - 2,
                256, 128);
            GuiShim.pushMatrix(g);
            GuiShim.translate(g, x + 7, y + 7);
            GuiShim.scale(g, 1.5f, 1.5f);
            if (card.family == Card.Family.REVERSE) {
                GuiShim.blit(g, ATLAS_LOCATION, 0, 0, 208, 0, 10, 10, 256, 128);
            } else if (card.family == Card.Family.SKIP) {
                GuiShim.blit(g, ATLAS_LOCATION, 0, 0, 218, 0, 10, 10, 256, 128);
            } else if (card.suit == Card.Suit.WILD && card.family == Card.Family.NUMBER) {
                GuiShim.blit(g, ATLAS_LOCATION, 0, 0, 228, 0, 10, 10, 256, 128);
            } else {
                Component cardName = card.getCardFaceName().copy()
                        .withStyle(Style.EMPTY.withFont(GuiShim.getMinecraftyFontDesc()));
                int colorA = (int) (0x22 * shadowAlpha + 0xFF * (1 - shadowAlpha));
                GuiShim.drawString(g, font, cardName, 0, 0, 0xFF000000 + colorA * 0x10101);
            }

            GuiShim.popMatrix(g);
            GuiShim.pushMatrix(g);
            g.fill(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, 0x222222 | ((int) (0xFF * shadowAlpha) << 24));
            GuiShim.popMatrix(g);
        }

        //? if <26.1
        //RenderSystem.disableBlend();
    }

    public static final GameOverlayLayer INSTANCE = new GameOverlayLayer();
}
