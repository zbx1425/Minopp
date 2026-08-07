package cn.zbx1425.minopp.game.effect;

import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.gui.SeatControlScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

public record SeatActionTakenEffectEvent() implements EffectEvent {

    public static SeatActionTakenEffectEvent streamDecode(FriendlyByteBuf buf) {
        return new SeatActionTakenEffectEvent();
    }

    @Override
    public void streamEncode(FriendlyByteBuf buf) {
    }

    @Override
    public Type<? extends EffectEvent> type() {
        return EffectEvents.SEAT_ACTION_TAKEN;
    }

    @Override
    public int timeOffset() {
        return 0;
    }

    @Override
    public Optional<UUID> target() {
        return Optional.empty();
    }

    @Override
    public void summonClient(Level level, BlockPos origin, boolean selfIsPartOfSourceGame) {
        if (selfIsPartOfSourceGame && Minecraft.getInstance().screen instanceof SeatControlScreen screen) {
            screen.onClose();
        }
    }

    @Override
    public void summonServer(ServerLevel level, BlockPos origin, BlockEntityMinoTable tableEntity) {

    }
}
