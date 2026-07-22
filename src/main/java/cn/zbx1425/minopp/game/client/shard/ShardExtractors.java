package cn.zbx1425.minopp.game.client.shard;

import cn.zbx1425.minopp.game.client.gui.*;
import cn.zbx1425.minopp.game.shard.*;
import cn.zbx1425.minopp.game.shard.ActionReportShard.ShardType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.*;
import java.util.function.Function;

public class ShardExtractors {

    private static final Map<ShardType<?>, ShardExtractor<?>> REGISTRY = new HashMap<>();

    static {
        register(ActionReportShards.PLAY, new ShardExtractor<PlayShard>() {
            @Override
            public Map<UUID, List<BadgeGuiShard>> extractBadges(PlayShard shard) {
                List<BadgeGuiShard> badges = new ArrayList<>();
                badges.add(new PlayBadgeGuiShard(shard.card()));
                return Map.of(shard.subject(), badges);
            }

            @Override
            public List<MessageGuiShard> extractMessages(PlayShard shard, Function<UUID, String> nameResolver) {
                if (shard.isCut()) {
                    return List.of(new MessageGuiShard(
                        Component.translatable("game.minopp.play.cut", nameResolver.apply(shard.subject()), shard.card().getDisplayName()),
                        0xFFFFFFFF
                    ));
                } else {
                    return List.of();
                }
            }
        });

        register(ActionReportShards.DRAW, new ShardExtractor<DrawShard>() {
            @Override
            public Map<UUID, List<BadgeGuiShard>> extractBadges(DrawShard shard) {
                return Map.of(shard.subject(), List.of(new DrawBadgeGuiShard(shard.drawCount())));
            }

            @Override
            public List<MessageGuiShard> extractMessages(DrawShard shard, Function<UUID, String> nameResolver) {
                return List.of();
            }
        });

        register(ActionReportShards.PASS, new ShardExtractor<PassShard>() {
            @Override
            public Map<UUID, List<BadgeGuiShard>> extractBadges(PassShard shard) {
                return Map.of(shard.subject(), List.of(new PassBadgeGuiShard()));
            }

            @Override
            public List<MessageGuiShard> extractMessages(PassShard shard, Function<UUID, String> nameResolver) {
                return List.of();
            }
        });

        register(ActionReportShards.SKIP, new ShardExtractor<SkipShard>() {
            @Override
            public Map<UUID, List<BadgeGuiShard>> extractBadges(SkipShard shard) {
                return Map.of(shard.skippedPlayer(), List.of(new SkipBadgeGuiShard()));
            }

            @Override
            public List<MessageGuiShard> extractMessages(SkipShard shard, Function<UUID, String> nameResolver) {
                return List.of();
            }
        });

        register(ActionReportShards.REVERSE, new ShardExtractor<ReverseShard>() {
            @Override
            public Map<UUID, List<BadgeGuiShard>> extractBadges(ReverseShard shard) {
                return Map.of();
            }

            @Override
            public List<MessageGuiShard> extractMessages(ReverseShard shard, Function<UUID, String> nameResolver) {
                return List.of(new MessageGuiShard(
                        Component.translatable("gui.minopp.play.direction." + (shard.isAntiClockwise() ? "ccw" : "cw")),
                        0xFFFFFFFF
                ));
            }
        });

        register(ActionReportShards.HAND_SWAP, new ShardExtractor<HandSwapShard>() {
            @Override
            public Map<UUID, List<BadgeGuiShard>> extractBadges(HandSwapShard shard) {
                Map<UUID, List<BadgeGuiShard>> result = new HashMap<>();
                result.put(shard.source(), List.of(new SwapBadgeGuiShard()));
                result.put(shard.target(), List.of(new SwapBadgeGuiShard()));
                return result;
            }

            @Override
            public List<MessageGuiShard> extractMessages(HandSwapShard shard, Function<UUID, String> nameResolver) {
                return List.of(new MessageGuiShard(
                        Component.translatable("game.minopp.play.swap_hands",
                                nameResolver.apply(shard.source()), nameResolver.apply(shard.target()))
                                .withStyle(Style.EMPTY.withColor(0xFFAA00)),
                        0xFFFFAA00
                ));
            }
        });

        register(ActionReportShards.HAND_ROTATE, new ShardExtractor<HandRotateShard>() {
            @Override
            public Map<UUID, List<BadgeGuiShard>> extractBadges(HandRotateShard shard) {
                return Map.of();
            }

            @Override
            public List<MessageGuiShard> extractMessages(HandRotateShard shard, Function<UUID, String> nameResolver) {
                return List.of(new MessageGuiShard(
                        Component.translatable("game.minopp.play.rotate_hands",
                                Component.translatable("gui.minopp.play.direction." + (shard.isAntiClockwise() ? "ccw" : "cw")))
                                .withStyle(Style.EMPTY.withColor(0xFFAA00)),
                        0xFFFFAA00
                ));
            }
        });

        register(ActionReportShards.GAME_WON, new ShardExtractor<GameWonShard>() {
            @Override
            public Map<UUID, List<BadgeGuiShard>> extractBadges(GameWonShard shard) {
                Map<UUID, List<BadgeGuiShard>> result = new HashMap<>();
                result.put(shard.winner(), List.of(new GameWonBadgeGuiShard(0)));
                for (Map.Entry<UUID, Integer> entry : shard.otherPlayersHandSizes().entrySet()) {
                    result.put(entry.getKey(), List.of(new GameWonBadgeGuiShard(entry.getValue())));
                }
                return result;
            }

            @Override
            public List<MessageGuiShard> extractMessages(GameWonShard shard, Function<UUID, String> nameResolver) {
                List<MessageGuiShard> messages = new ArrayList<>();
                messages.add(new MessageGuiShard(
                        Component.translatable("game.minopp.play.game_won", nameResolver.apply(shard.winner()))
                                .withStyle(Style.EMPTY.withColor(0xFFAA00)),
                        0xFFFFAA00
                ));
                for (Map.Entry<UUID, Integer> entry : shard.otherPlayersHandSizes().entrySet()) {
                    messages.add(new MessageGuiShard(
                            Component.translatable("game.minopp.play.game_nearly_won",
                                    nameResolver.apply(entry.getKey()), entry.getValue()),
                            0xFFAAAAAA
                    ));
                }
                return messages;
            }
        });

        register(ActionReportShards.SYSTEM, new ShardExtractor<SystemShard>() {
            @Override
            public Map<UUID, List<BadgeGuiShard>> extractBadges(SystemShard shard) {
                return Map.of();
            }

            @Override
            public List<MessageGuiShard> extractMessages(SystemShard shard, Function<UUID, String> nameResolver) {
                return List.of(new MessageGuiShard(shard.message(), 0xFFFFFFFF));
            }
        });

        register(ActionReportShards.REJECTION, new ShardExtractor<RejectionShard>() {
            @Override
            public Map<UUID, List<BadgeGuiShard>> extractBadges(RejectionShard shard) {
                return Map.of();
            }

            @Override
            public List<MessageGuiShard> extractMessages(RejectionShard shard, Function<UUID, String> nameResolver) {
                return List.of(new MessageGuiShard(shard.message(), 0xFFFF0000, true));
            }
        });

        register(ActionReportShards.MINO_SHOUT, new ShardExtractor<MinoShoutShard>() {
            @Override
            public Map<UUID, List<BadgeGuiShard>> extractBadges(MinoShoutShard shard) {
                return Map.of(shard.subject(), List.of(new MinoShoutBadgeGuiShard()));
            }

            @Override
            public List<MessageGuiShard> extractMessages(MinoShoutShard shard, Function<UUID, String> nameResolver) {
                return List.of(new MessageGuiShard(
                        Component.translatable("game.minopp.play.mino_shout", nameResolver.apply(shard.subject())),
                        0xFF00DD55
                ));
            }
        });

        register(ActionReportShards.MINO_SHOUT_PENALTY, new ShardExtractor<MinoShoutPenaltyShard>() {
            @Override
            public Map<UUID, List<BadgeGuiShard>> extractBadges(MinoShoutPenaltyShard shard) {
                List<BadgeGuiShard> badges = new ArrayList<>();
                badges.add(new MinoShoutBadgeGuiShard());
                badges.add(new DrawBadgeGuiShard(shard.drawCount()));
                return Map.of(shard.subject(), badges);
            }

            @Override
            public List<MessageGuiShard> extractMessages(MinoShoutPenaltyShard shard, Function<UUID, String> nameResolver) {
                return List.of(new MessageGuiShard(
                        Component.translatable("game.minopp.play.mino_shout_invalid", nameResolver.apply(shard.subject())),
                        0xFFFF0000, true
                ));
            }
        });

        register(ActionReportShards.MINO_DOUBT, new ShardExtractor<MinoDoubtShard>() {
            @Override
            public Map<UUID, List<BadgeGuiShard>> extractBadges(MinoDoubtShard shard) {
                Map<UUID, List<BadgeGuiShard>> result = new HashMap<>();
                result.put(shard.source(), List.of(new DoubtBadgeGuiShard()));
                result.put(shard.target(), List.of(new DrawBadgeGuiShard(shard.drawCount())));
                return result;
            }

            @Override
            public List<MessageGuiShard> extractMessages(MinoDoubtShard shard, Function<UUID, String> nameResolver) {
                return List.of(new MessageGuiShard(
                        Component.translatable("game.minopp.play.doubt_success",
                                nameResolver.apply(shard.source()), nameResolver.apply(shard.target()))
                                .withStyle(Style.EMPTY.withColor(0xFFAA00)),
                        0xFFFFAA00
                ));
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <T extends ActionReportShard> ShardExtractor<T> get(ShardType<T> type) {
        return (ShardExtractor<T>) REGISTRY.get(type);
    }

    @SuppressWarnings("unchecked")
    public static ShardExtractor<ActionReportShard> getUnchecked(ShardType<?> type) {
        return (ShardExtractor<ActionReportShard>) REGISTRY.get(type);
    }

    private static <T extends ActionReportShard> void register(ShardType<T> type, ShardExtractor<T> extractor) {
        REGISTRY.put(type, extractor);
    }
}
