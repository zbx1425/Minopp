package cn.zbx1425.minopp.gui;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.MinoClient;
import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.block.BlockMinoTable;
import cn.zbx1425.minopp.game.*;
import cn.zbx1425.minopp.game.client.gui.BadgeGuiShard;
import cn.zbx1425.minopp.game.client.gui.MessageGuiShard;
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
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.NonNull;

//? if <26.1 {
/*import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.util.FastColor;
*///? } else {

//? }

//? if neoforge
//import net.neoforged.neoforge.client.gui.GuiLayer;

import java.util.*;
import java.util.function.Function;

//? if <26.1
//public class GameOverlayLayer implements LayeredDraw.Layer {
//? if >=26.1 && neoforge
//public class GameOverlayLayer implements GuiLayer {
//? if >=26.1 && !neoforge
public class GameOverlayLayer {

    private static final int BG_COLOR_SOLID = 0xFF888888;
    private static final int BG_COLOR_BACKDROP = 0x66000000;

    private static final int AVATAR_SIZE = 16;
    private static final int NAME_WIDTH = 80;
    private static final int NAME_PAD_X = 3;
    private static final int ROW_HEIGHT = 20;

    private double zoomAnimationProgress = 0;
    private double zoomAnimationTarget = 0;
    private final Long2FloatArrayMap handCardCurrentXOff = new Long2FloatArrayMap();

    private final Set<Long> newlyDrawnCardHashes = new HashSet<>();
    private final Map<UUID, Integer> lastSwapGeneration = new HashMap<>();
    private CardGame.PlayerActionPhase lastPhase = null;
    private int lastCurrentPlayerIndex = -1;
    private LongArrayList lastHandCardHashes = new LongArrayList();

    //? if <26.1
    //@Override
    public void render(@NonNull GuiGraphicsExtractor guiGraphics, @NonNull DeltaTracker deltaTracker) {
        LocalPlayer player = Minecraft.getInstance().player;
        BlockPos handCardGamePos = ItemHandCards.getHandCardGamePos(player);
        ClientLevel level = Minecraft.getInstance().level;
        BlockPos hitResultGamePos = BlockMinoTable.Client.getCursorPickedGame();
        BlockPos gamePos = (handCardGamePos != null) ? handCardGamePos : hitResultGamePos;
        if (gamePos == null) {
            TurnDeadMan.setOutsideGame();
            MinoClient.handCardOverlayActive = false;
            handCardCurrentXOff.clear();
            return;
        }
        BlockEntityMinoTable tableEntity = (BlockEntityMinoTable)level.getBlockEntity(gamePos);
        if (tableEntity == null) {
            TurnDeadMan.setOutsideGame();
            MinoClient.handCardOverlayActive = false;
            handCardCurrentXOff.clear();
            return;
        }

        if (tableEntity.game == null) {
            renderGameInactive(guiGraphics, deltaTracker, tableEntity);
            TurnDeadMan.setOutsideGame();
            zoomAnimationProgress = 0;
            zoomAnimationTarget = 0;
        } else {
            TurnDeadMan.tick(tableEntity.game, deltaTracker);
            if (handCardGamePos == null || hitResultGamePos == null || Objects.equals(handCardGamePos, hitResultGamePos)) {
                renderGameActive(guiGraphics, deltaTracker, tableEntity);
            } else {
                zoomAnimationTarget = 0;
            }
        }
        performZoomAnimation(deltaTracker, tableEntity);
        MinoClient.handCardOverlayActive = renderHandCards(guiGraphics, deltaTracker);
    }

    // ========== Game Inactive ==========

    private void renderGameInactive(GuiGraphicsExtractor g, DeltaTracker deltaTracker, BlockEntityMinoTable tableEntity) {
        if (Minecraft.getInstance().options.hideGui) return;
        int x = 20, y = 60;
        Font font = Minecraft.getInstance().font;

        Function<UUID, String> nameResolver = buildNameResolver(null, tableEntity);
        List<CardPlayer> playerList = tableEntity.getPlayersList();
        y = renderShardPanel(g, font, x, y, tableEntity, playerList, null, nameResolver);

        y += font.lineHeight;
        drawStringWithBackdrop(g, font, Component.translatable("gui.minopp.play.start_hint"), x, y, 0xFF00DD55);
    }

    // ========== Game Active ==========

    private void renderGameActive(GuiGraphicsExtractor g, DeltaTracker deltaTracker, BlockEntityMinoTable tableEntity) {
        LocalPlayer player = Minecraft.getInstance().player;
        CardPlayer cardPlayer = ItemHandCards.getCardPlayer(player);
        CardPlayer currentPlayer = tableEntity.game.players.get(tableEntity.game.currentPlayerIndex);
        if (currentPlayer.equals(cardPlayer)) {
            if (tableEntity.game.currentPlayerPhase == CardGame.PlayerActionPhase.DISCARD_HAND) {
                zoomAnimationTarget = 1;
            } else {
                if (zoomAnimationTarget < 1.01) {
                    zoomAnimationTarget = 1.5;
                } else if (zoomAnimationProgress >= 1.5) {
                    zoomAnimationTarget = 1.05;
                }
            }
        } else {
            zoomAnimationTarget = 0;
        }

        if (Minecraft.getInstance().options.hideGui) return;
        int x = 20, y = 60;
        Font font = Minecraft.getInstance().font;

        Function<UUID, String> nameResolver = buildNameResolver(tableEntity.game, tableEntity);

        // --- Status lines ---
        drawStringWithBackdrop(g, font, Component.translatable("gui.minopp.play.game_active").append(" \u00a9 Zbx1425"), x, y, 0xFF7090FF);
        y += font.lineHeight;
        if (currentPlayer.equals(cardPlayer)) {
            drawStringWithBackdrop(g, font, Component.translatable("gui.minopp.play." + tableEntity.game.currentPlayerPhase.name().toLowerCase()), x, y,
                    (System.currentTimeMillis() % 400 < 200) ? 0xFFFFFFFF : 0xFFFFFF00);
            if (tableEntity.game.currentPlayerPhase == CardGame.PlayerActionPhase.DISCARD_DRAWN
                    && tableEntity.rules.forcePlay() && tableEntity.game.lastDrawnCard != null) {
                y += font.lineHeight;
                drawStringWithBackdrop(g, font, Component.translatable("gui.minopp.play.force_play_hint",
                        tableEntity.game.lastDrawnCard.getDisplayName()), x, y, 0xFFFFAA00);
            }
        } else {
            drawStringWithBackdrop(g, font, Component.translatable("gui.minopp.play.turn_other", currentPlayer.name), x, y, 0xFFAAAAAA);
        }
        y += font.lineHeight;
        MutableComponent auxInfo = Component.translatable("gui.minopp.play.direction." + (tableEntity.game.isAntiClockwise ? "ccw" : "cw"));
        if (tableEntity.game.drawCount > 0) {
            auxInfo = auxInfo.append(", ").append(Component.translatable("gui.minopp.play.draw_accumulate", tableEntity.game.drawCount));
        }
        drawStringWithBackdrop(g, font, auxInfo, x, y, 0xFFAAAAAA);
        y += font.lineHeight * 2;

        MutableComponent topCardInfo = Component.translatable("gui.minopp.play.top_card", tableEntity.game.topCard.getDisplayName().getString());
        if (tableEntity.game.topCard.suit == Card.Suit.WILD) {
            topCardInfo.append(", ").append(Component.translatable("gui.minopp.play.top_card_wild_color",
                    Component.translatable("game.minopp.card.suit." + tableEntity.game.topCard.getEquivSuit().name().toLowerCase())));
        }
        drawStringWithBackdrop(g, font, topCardInfo, x, y, 0xFFFFFFDD);
        y += font.lineHeight * 2;

        // --- Shard panel (badges + messages) ---
        List<CardPlayer> orderedPlayers = getOrderedPlayers(tableEntity.game);
        y = renderShardPanel(g, font, x, y, tableEntity, orderedPlayers, currentPlayer, nameResolver);

        // --- Cursor hints ---
        renderCursorHints(g, font, tableEntity, cardPlayer, currentPlayer);
    }

    // ========== Shared Shard Panel: badges + messages ==========

    private int renderShardPanel(GuiGraphicsExtractor g, Font font, int x, int y,
                                  BlockEntityMinoTable tableEntity, List<CardPlayer> players,
                                  CardPlayer currentPlayer, Function<UUID, String> nameResolver) {

        boolean hasGameWon = tableEntity.stateShards.stream().anyMatch(s -> s instanceof GameWonShard);

        // Layer 1: Current state badges
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

        // Layer 2: Sticky badges (TOP_CARD_STICKY)
        Map<UUID, List<BadgeGuiShard>> stickyBadges = new LinkedHashMap<>();
        if (!hasGameWon) {
            for (ActionReportShard shard : tableEntity.clientStickyShards) {
                ShardExtractor<ActionReportShard> ext = ShardExtractors.getUnchecked(shard.shardType());
                if (ext != null) {
                    mergeBadges(stickyBadges, ext.extractBadges(shard));
                }
            }
        }

        // Layer 3: Ephemeral/Noteworthy badges + messages (newest = leftmost / topmost)
        long currentTime = System.currentTimeMillis();
        Map<UUID, List<Pair<BadgeGuiShard, Float>>> noteworthyBadges = new LinkedHashMap<>();
        List<Pair<MessageGuiShard, Float>> noteworthyMessages = new ArrayList<>();
        for (ListIterator<Pair<ActionReportShard, Long>> it =
                     tableEntity.clientEphemeralShards.listIterator(tableEntity.clientEphemeralShards.size()); it.hasPrevious(); ) {
            Pair<ActionReportShard, Long> entry = it.previous();
            if (entry.getSecond() < currentTime) {
                it.remove();
                continue;
            }
            float alpha = Mth.clamp((float)(entry.getSecond() - currentTime) / 4000f, 0f, 1f);
            ShardExtractor<ActionReportShard> ext = ShardExtractors.getUnchecked(entry.getFirst().shardType());
            if (ext != null) {
                Map<UUID, List<BadgeGuiShard>> badges = ext.extractBadges(entry.getFirst());
                for (Map.Entry<UUID, List<BadgeGuiShard>> e : badges.entrySet()) {
                    noteworthyBadges.computeIfAbsent(e.getKey(), k -> new ArrayList<>());
                    for (BadgeGuiShard badge : e.getValue()) {
                        noteworthyBadges.get(e.getKey()).add(new Pair<>(badge, alpha));
                    }
                }
                for (MessageGuiShard msg : ext.extractMessages(entry.getFirst(), nameResolver)) {
                    noteworthyMessages.add(new Pair<>(msg, alpha));
                }
            }
        }

        // --- Render player rows ---
        boolean hasBadges = !currentBadges.isEmpty() || !stickyBadges.isEmpty() || !noteworthyBadges.isEmpty();
        if (!players.isEmpty() && hasBadges) {
            for (CardPlayer p : players) {
                int rowX = x;
                boolean isCurrent = currentPlayer != null && p.equals(currentPlayer);

                if (isCurrent) {
                    g.fill(rowX - 1, y - 1, rowX + AVATAR_SIZE + 1, y + AVATAR_SIZE + 1, 0xFFFFFF00);
                }
                g.fill(rowX, y, rowX + AVATAR_SIZE, y + AVATAR_SIZE, 0xFF888888);
                rowX += AVATAR_SIZE;

                Component nameComp = Component.literal(p.name);
                g.fill(rowX, y, rowX + NAME_WIDTH, y + AVATAR_SIZE, BG_COLOR_BACKDROP);
                g.text(font, nameComp, rowX + NAME_PAD_X, y + (AVATAR_SIZE - font.lineHeight) / 2,
                        isCurrent ? 0xFFFFFFFF : 0xFFAAAAAA, true);
                rowX += NAME_WIDTH;

                // Order: Current → Sticky → Ephemeral (newest leftmost)
                List<BadgeGuiShard> playerCurrentBadges = currentBadges.getOrDefault(p.uuid, List.of());
                for (BadgeGuiShard badge : playerCurrentBadges) {
                    badge.render(g, font, rowX, y, BG_COLOR_SOLID);
                    rowX += BadgeGuiShard.WIDTH;
                }

                List<BadgeGuiShard> playerStickyBadges = stickyBadges.getOrDefault(p.uuid, List.of());
                for (BadgeGuiShard badge : playerStickyBadges) {
                    badge.render(g, font, rowX, y, BG_COLOR_SOLID);
                    rowX += BadgeGuiShard.WIDTH;
                }

                List<Pair<BadgeGuiShard, Float>> playerNoteworthyBadges = noteworthyBadges.getOrDefault(p.uuid, List.of());
                for (Pair<BadgeGuiShard, Float> entry : playerNoteworthyBadges) {
                    int alpha = (int)(entry.getSecond() * 0x66);
                    entry.getFirst().render(g, font, rowX, y, alpha << 24);
                    rowX += BadgeGuiShard.WIDTH;
                }

                y += ROW_HEIGHT;
            }
            y += font.lineHeight;
        }

        // --- Message list ---
        for (MessageGuiShard msg : currentMessages) {
            drawStringWithBackdrop(g, font, msg.message, x, y, msg.color);
            y += font.lineHeight;
        }

        for (Pair<MessageGuiShard, Float> entry : noteworthyMessages) {
            if (y > Minecraft.getInstance().getWindow().getGuiScaledHeight() - font.lineHeight - 40) break;
            int alpha = (int)(entry.getSecond() * 0xFF);
            MessageGuiShard msg = entry.getFirst();
            int color;
            if (msg.preserveColor) {
                color = (msg.color & 0x00FFFFFF) | (alpha << 24);
            } else {
                color = 0xAAAAAA | (alpha << 24);
            }
            drawStringWithBackdrop(g, font, msg.message, x, y, color);
            y += font.lineHeight;
        }

        return y;
    }

    // ========== Cursor Hints ==========

    private static void renderCursorHints(GuiGraphicsExtractor g, Font font,
                                           BlockEntityMinoTable tableEntity, CardPlayer cardPlayer, CardPlayer currentPlayer) {
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
                    int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
                    int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
                    boolean highlight = Minecraft.getInstance().level.getGameTime() % 3L < 2L && isPass;
                    int msgWidth = Math.max(font.width(cursorMessage), isShouting ? font.width(shoutMessage) : 0);
                    int msgHeight = isShouting ? font.lineHeight * 2 : font.lineHeight;
                    g.fill(width / 2 + 8, height / 2 - msgHeight / 2 - 2, width / 2 + msgWidth + 16, height / 2 + msgHeight / 2 + 3, highlight ? 0x80AAAA66 : 0x80000000);
                    g.text(font, cursorMessage, width / 2 + 12, height / 2 - msgHeight / 2, highlight ? 0xFF222222 : 0xFFFFFFDD);
                    if (isShouting) {
                        g.text(font, shoutMessage, width / 2 + 12, height / 2 - msgHeight / 2 + font.lineHeight, highlight ? 0xFF222222 : 0xFFFFFFDD);
                    }
                }
            }
        }

        if (TurnDeadMan.isAlarmActive()) {
            Component deadManMessage = Component.translatable("gui.minopp.play.cursor.dead_man");
            int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            boolean highlight = Minecraft.getInstance().level.getGameTime() % 3L < 2L;
            int msgWidth = font.width(deadManMessage);
            int msgHeight = font.lineHeight;
            GuiShim.pushMatrix(g);
            GuiShim.translate(g, (float)(width / 2), (float)(height / 2 + 12));
            GuiShim.scale(g, 1.5f, 1.5f);
            g.fill(-msgWidth / 2 - 4, 0, msgWidth / 2 + 4, msgHeight + 4, highlight ? 0x80AAAA66 : 0x80000000);
            g.text(font, deadManMessage, -msgWidth / 2, 2, highlight ? 0xFF222222 : 0xFFFFFFDD);
            GuiShim.popMatrix(g);
        }
    }

    // ========== Helpers ==========

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

    private static List<CardPlayer> getOrderedPlayers(CardGame game) {
        List<CardPlayer> result = new ArrayList<>();
        int n = game.players.size();
        for (int i = 0; i < n; i++) {
            int idx = game.isAntiClockwise ? (n - i) % n : i;
            result.add(game.players.get(idx));
        }
        return result;
    }

    private static void mergeBadges(Map<UUID, List<BadgeGuiShard>> target, Map<UUID, List<BadgeGuiShard>> source) {
        for (Map.Entry<UUID, List<BadgeGuiShard>> e : source.entrySet()) {
            target.computeIfAbsent(e.getKey(), k -> new ArrayList<>()).addAll(e.getValue());
        }
    }

    private static void drawStringWithBackdrop(GuiGraphicsExtractor guiGraphics, Font font, Component component, int x, int y, int color) {
        int i = (int)(0.4 * 255.0F) << 24 & -16777216;
        guiGraphics.fill(x - 2, y, x + font.width(component) + 2, y + font.lineHeight, i);
        guiGraphics.text(font, component, x, y, color, true);
    }

    // ========== Hand Cards ==========

    private static final Identifier ATLAS_LOCATION = Mino.id("textures/gui/deck.png");

    private boolean renderHandCards(GuiGraphicsExtractor g, DeltaTracker deltaTracker) {
        if (Minecraft.getInstance().options.hideGui) return false;

        Font font = Minecraft.getInstance().font;
        LocalPlayer player = Minecraft.getInstance().player;
        ClientLevel level = Minecraft.getInstance().level;
        BlockPos gamePos = ItemHandCards.getHandCardGamePos(player);
        if (gamePos == null) return false;
        BlockEntityMinoTable tableEntity = (BlockEntityMinoTable)level.getBlockEntity(gamePos);
        CardPlayer playerWithoutHand = ItemHandCards.getCardPlayer(player);

        final int CARD_V_SPACING = 20;
        final int CARD_WIDTH = (int)(100.0 * Mth.lerp(zoomAnimationProgress, 0.93, 1.0));
        final int CARD_HEIGHT = (int)(CARD_WIDTH * 8.9 / 5.6);

        if (tableEntity.game == null) return false;
        CardPlayer realPlayer = tableEntity.game.players.stream().filter(p -> p.equals(playerWithoutHand)).findFirst().orElse(null);
        if (realPlayer == null) return false;
        int clientHandIndex = Mth.clamp(ItemHandCards.getClientHandIndex(player), 0, realPlayer.hand.size() - 1);

        realPlayer.hand.sort(Card::compareTo);
        LongArrayList handCardHashes = new LongArrayList();
        for (Card card : realPlayer.hand) {
            if (!handCardHashes.isEmpty() && card.hashCode() == (handCardHashes.getLast() & 0xFFFFFFFFL)) {
                handCardHashes.add(handCardHashes.getLast() + 0x100000000L);
            } else {
                handCardHashes.add(card.hashCode());
            }
        }

        updateDrawnCardTracking(tableEntity, realPlayer, handCardHashes);
        handCardCurrentXOff.keySet().removeIf(hash -> !handCardHashes.contains(hash));

        //? if <26.1
        //RenderSystem.enableBlend();

        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int handSize = realPlayer.hand.size();
        int selectedCardYRaw = height - ((CARD_HEIGHT / 2) + CARD_V_SPACING * (handSize - clientHandIndex));
        int cardDrawOffset = selectedCardYRaw < 20 ? 20 - selectedCardYRaw : 0;
        Random cardRandom = new Random(handSize);
        for (int i = 0; i < handSize; i++) {
            int targetXOff = (i == clientHandIndex ? -30 : 0) + cardRandom.nextInt(-3, 4);
            float currentXOff = handCardCurrentXOff.computeIfAbsent(handCardHashes.getLong(i), ignored -> CARD_WIDTH + 10);
            int x = width - 10 - CARD_WIDTH + (int)currentXOff;
            handCardCurrentXOff.put(handCardHashes.getLong(i),
                    (float)Mth.lerp(8 * 0.05 * deltaTracker.getGameTimeDeltaPartialTick(false),
                            currentXOff, targetXOff));
            int y = height - ((CARD_HEIGHT / 2) + CARD_V_SPACING * (handSize - i)) + cardDrawOffset;
            if (i == clientHandIndex) {
                Card card = realPlayer.hand.get(i);
                Component cardName = card.getDisplayName();
                g.text(font, cardName, x - font.width(cardName) - 10, y + 10, 0xFFFFFFDD);
            }
            boolean isNewlyDrawn = newlyDrawnCardHashes.contains(handCardHashes.getLong(i));
            if (isNewlyDrawn) {
                float pulse = (float)(Math.sin(System.currentTimeMillis() * 0.006) + 1) / 2f;
                int r = (int)(0xFF * (1 - pulse) + 0xCC * pulse);
                int borderColor = 0xFF000000 | (r << 16);
                g.fill(x - 3, y - 3, x + CARD_WIDTH + 3, y + CARD_HEIGHT + 3, borderColor);
            }
            g.fill(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, 0xFF222222);
            g.fill(x + 1, y + 1, x + CARD_WIDTH - 1, y + CARD_HEIGHT - 1, 0xFFDDDDDD);

            Card card = realPlayer.hand.get(i);
            float cardU = switch (card.family) {
                case NUMBER -> Math.abs(card.number) * 16;
                case SKIP -> 160;
                case DRAW -> 176;
                case REVERSE -> 192;
            };
            float cardV = card.suit.ordinal() * 25;
            int cardUW = 16;
            int cardVH = 25;

            float shadowAlpha = (float) Math.max(Mth.lerp(zoomAnimationProgress, 0.5, 0), 0);

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
                        .withStyle(Style.EMPTY.withFont(GuiShim.getMiencraftyFontDesc()));
                int colorA = (int)(0x22 * shadowAlpha + 0xFF * (1 - shadowAlpha));
                g.text(font, cardName, 0, 0, 0xFF000000 + colorA * 0x10101);
            }

            GuiShim.popMatrix(g);
            GuiShim.pushMatrix(g);
            g.fill(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, 0x222222 | ((int)(0xFF * shadowAlpha) << 24));
            GuiShim.popMatrix(g);
        }

        //? if <26.1
        //RenderSystem.disableBlend();

        return true;
    }

    // ========== Drawn Card Tracking ==========

    private void updateDrawnCardTracking(BlockEntityMinoTable tableEntity, CardPlayer realPlayer, LongArrayList currentHashes) {
        CardGame game = tableEntity.game;
        if (game == null) {
            newlyDrawnCardHashes.clear();
            lastPhase = null;
            lastCurrentPlayerIndex = -1;
            lastHandCardHashes.clear();
            return;
        }

        Integer prevGen = lastSwapGeneration.get(realPlayer.uuid);
        if (prevGen != null && prevGen != realPlayer.swapGeneration) {
            newlyDrawnCardHashes.clear();
            lastHandCardHashes = new LongArrayList(currentHashes);
            lastSwapGeneration.put(realPlayer.uuid, realPlayer.swapGeneration);
            lastPhase = game.currentPlayerPhase;
            lastCurrentPlayerIndex = game.currentPlayerIndex;
            return;
        }
        lastSwapGeneration.put(realPlayer.uuid, realPlayer.swapGeneration);

        if (game.currentPlayerIndex != lastCurrentPlayerIndex) {
            newlyDrawnCardHashes.clear();
            lastHandCardHashes = new LongArrayList(currentHashes);
            lastPhase = game.currentPlayerPhase;
            lastCurrentPlayerIndex = game.currentPlayerIndex;
            return;
        }

        CardPlayer currentPlayer = game.players.get(game.currentPlayerIndex);
        boolean isOurTurn = currentPlayer.equals(realPlayer);

        if (isOurTurn && lastPhase == CardGame.PlayerActionPhase.DISCARD_HAND
                && game.currentPlayerPhase == CardGame.PlayerActionPhase.DISCARD_DRAWN) {
            newlyDrawnCardHashes.clear();
            int i = 0, j = 0;
            while (i < currentHashes.size() && j < lastHandCardHashes.size()) {
                long curr = currentHashes.getLong(i);
                long prev = lastHandCardHashes.getLong(j);
                if (curr == prev) {
                    i++; j++;
                } else if (curr < prev) {
                    newlyDrawnCardHashes.add(curr);
                    i++;
                } else {
                    j++;
                }
            }
            while (i < currentHashes.size()) {
                newlyDrawnCardHashes.add(currentHashes.getLong(i++));
            }
        }

        lastHandCardHashes = new LongArrayList(currentHashes);
        lastPhase = game.currentPlayerPhase;
        lastCurrentPlayerIndex = game.currentPlayerIndex;
    }

    private void performZoomAnimation(DeltaTracker deltaTracker, BlockEntityMinoTable tableEntity) {
        if (Math.abs(zoomAnimationTarget - zoomAnimationProgress) < 0.01) {
            zoomAnimationProgress = zoomAnimationTarget;
        } else {
            zoomAnimationProgress += (zoomAnimationTarget - zoomAnimationProgress) * 8 * 0.05 * deltaTracker.getGameTimeDeltaPartialTick(false);
        }
        MinoClient.globalFovModifier = Mth.lerp(Mth.clamp(zoomAnimationProgress, 0, 1), 1.0, 0.97);
    }

    public static final GameOverlayLayer INSTANCE = new GameOverlayLayer();
}
