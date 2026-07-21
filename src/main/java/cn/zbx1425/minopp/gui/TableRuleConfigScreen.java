package cn.zbx1425.minopp.gui;

import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.game.TableRuleConfig;
import cn.zbx1425.minopp.network.C2STableRuleConfigPacket;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TableRuleConfigScreen {

    private static boolean stackingEnabled;
    private static boolean jumpInEnabled;
    private static boolean sevenRuleEnabled;
    private static boolean zeroRuleEnabled;
    private static boolean drawUntilMatch;
    private static boolean forcePlay;
    private static boolean wildDrawFourFreeUse;

    public static Screen create(BlockEntityMinoTable tableEntity, Screen parent) {
        TableRuleConfig current = tableEntity.rules;
        stackingEnabled = current.stackingEnabled();
        jumpInEnabled = current.jumpInEnabled();
        sevenRuleEnabled = current.sevenRuleEnabled();
        zeroRuleEnabled = current.zeroRuleEnabled();
        drawUntilMatch = current.drawUntilMatch();
        forcePlay = current.forcePlay();
        wildDrawFourFreeUse = current.wildDrawFourFreeUse();

        ConfigCategory.Builder categoryBuilder = ConfigCategory.createBuilder()
                .name(Component.translatable("gui.minopp.table_rules.title"));

        OptionGroup ruleOpts = OptionGroup.createBuilder()
                .name(Component.translatable("gui.minopp.table_rules.category.rules"))
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("gui.minopp.table_rules.stacking"))
                        .description(OptionDescription.of(Component.translatable("gui.minopp.table_rules.stacking.desc")))
                        .binding(true, () -> stackingEnabled, v -> stackingEnabled = v)
                        .controller(opt -> BooleanControllerBuilder.create(opt).onOffFormatter())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("gui.minopp.table_rules.jump_in"))
                        .description(OptionDescription.of(Component.translatable("gui.minopp.table_rules.jump_in.desc")))
                        .binding(true, () -> jumpInEnabled, v -> jumpInEnabled = v)
                        .controller(opt -> BooleanControllerBuilder.create(opt).onOffFormatter())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("gui.minopp.table_rules.seven_rule"))
                        .description(OptionDescription.of(Component.translatable("gui.minopp.table_rules.seven_rule.desc")))
                        .binding(false, () -> sevenRuleEnabled, v -> sevenRuleEnabled = v)
                        .controller(opt -> BooleanControllerBuilder.create(opt).onOffFormatter())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("gui.minopp.table_rules.zero_rule"))
                        .description(OptionDescription.of(Component.translatable("gui.minopp.table_rules.zero_rule.desc")))
                        .binding(false, () -> zeroRuleEnabled, v -> zeroRuleEnabled = v)
                        .controller(opt -> BooleanControllerBuilder.create(opt).onOffFormatter())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("gui.minopp.table_rules.draw_until_match"))
                        .description(OptionDescription.of(Component.translatable("gui.minopp.table_rules.draw_until_match.desc")))
                        .binding(false, () -> drawUntilMatch, v -> drawUntilMatch = v)
                        .controller(opt -> BooleanControllerBuilder.create(opt).onOffFormatter())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("gui.minopp.table_rules.force_play"))
                        .description(OptionDescription.of(Component.translatable("gui.minopp.table_rules.force_play.desc")))
                        .binding(false, () -> forcePlay, v -> forcePlay = v)
                        .controller(opt -> BooleanControllerBuilder.create(opt).onOffFormatter())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("gui.minopp.table_rules.wd4_free"))
                        .description(OptionDescription.of(Component.translatable("gui.minopp.table_rules.wd4_free.desc")))
                        .binding(false, () -> wildDrawFourFreeUse, v -> wildDrawFourFreeUse = v)
                        .controller(opt -> BooleanControllerBuilder.create(opt).onOffFormatter())
                        .build())
                .build();

        categoryBuilder.group(ruleOpts);

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("gui.minopp.table_rules.title"))
                .category(categoryBuilder.build())
                .save(() -> {
                    TableRuleConfig newConfig = new TableRuleConfig(
                            stackingEnabled, jumpInEnabled, sevenRuleEnabled, zeroRuleEnabled,
                            drawUntilMatch, forcePlay, wildDrawFourFreeUse);
                    C2STableRuleConfigPacket.Client.sendC2S(tableEntity.getBlockPos(), newConfig);
                })
                .build()
                .generateScreen(parent);
    }
}
