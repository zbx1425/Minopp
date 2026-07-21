package cn.zbx1425.minopp.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record TableRuleConfig(
    boolean stackingEnabled,
    boolean jumpInEnabled,
    boolean sevenRuleEnabled,
    boolean zeroRuleEnabled,
    boolean drawUntilMatch,
    boolean forcePlay,
    boolean wildDrawFourFreeUse
) {
    public static final TableRuleConfig DEFAULT = new TableRuleConfig(true, true, false, false, false, false, false);

    public static final Codec<TableRuleConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.optionalFieldOf("stackingEnabled", true).forGetter(TableRuleConfig::stackingEnabled),
        Codec.BOOL.optionalFieldOf("jumpInEnabled", true).forGetter(TableRuleConfig::jumpInEnabled),
        Codec.BOOL.optionalFieldOf("sevenRuleEnabled", false).forGetter(TableRuleConfig::sevenRuleEnabled),
        Codec.BOOL.optionalFieldOf("zeroRuleEnabled", false).forGetter(TableRuleConfig::zeroRuleEnabled),
        Codec.BOOL.optionalFieldOf("drawUntilMatch", false).forGetter(TableRuleConfig::drawUntilMatch),
        Codec.BOOL.optionalFieldOf("forcePlay", false).forGetter(TableRuleConfig::forcePlay),
        Codec.BOOL.optionalFieldOf("wildDrawFourFreeUse", false).forGetter(TableRuleConfig::wildDrawFourFreeUse)
    ).apply(instance, TableRuleConfig::new));

    public static final MapCodec<TableRuleConfig> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.BOOL.optionalFieldOf("stackingEnabled", true).forGetter(TableRuleConfig::stackingEnabled),
        Codec.BOOL.optionalFieldOf("jumpInEnabled", true).forGetter(TableRuleConfig::jumpInEnabled),
        Codec.BOOL.optionalFieldOf("sevenRuleEnabled", false).forGetter(TableRuleConfig::sevenRuleEnabled),
        Codec.BOOL.optionalFieldOf("zeroRuleEnabled", false).forGetter(TableRuleConfig::zeroRuleEnabled),
        Codec.BOOL.optionalFieldOf("drawUntilMatch", false).forGetter(TableRuleConfig::drawUntilMatch),
        Codec.BOOL.optionalFieldOf("forcePlay", false).forGetter(TableRuleConfig::forcePlay),
        Codec.BOOL.optionalFieldOf("wildDrawFourFreeUse", false).forGetter(TableRuleConfig::wildDrawFourFreeUse)
    ).apply(instance, TableRuleConfig::new));
}
