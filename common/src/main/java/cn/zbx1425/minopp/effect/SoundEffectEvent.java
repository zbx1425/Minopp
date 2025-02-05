package cn.zbx1425.minopp.effect;

import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.gui.TurnDeadMan;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

public record SoundEffectEvent(int timeOffset, Optional<UUID> target, ResourceLocation soundId) implements EffectEvent {

    @Override
    public Type<SoundEffectEvent> type() {
        return EffectEvents.SOUND;
    }

    // TODO: fixup cannot write uuid crashed by length error
    @Override
    public void encode(EffectEvent event, FriendlyByteBuf buffer) {
        buffer.writeInt(event.timeOffset());
//        target.ifPresent(buffer::writeUUID);
        buffer.writeResourceLocation(soundId);
    }

    public static SoundEffectEvent decode(FriendlyByteBuf buffer) {
        return new SoundEffectEvent(buffer.readInt(), Optional.empty(), buffer.readResourceLocation());
    }

    @Override
    public void summonClient(Level level, BlockPos origin, boolean selfPartOfSourceGame) {
        var sound = SoundEvent.createFixedRangeEvent(soundId, 16);
        level.playLocalSound(origin, sound, SoundSource.PLAYERS, 1, 1, false);
        if (selfPartOfSourceGame) {
            // Something's happening, so reset the idle timer
            // This is chosen as most actions have a sound effect associated with them
            TurnDeadMan.pedal();
        }
    }

    @Override
    public void summonServer(ServerLevel level, BlockPos origin, BlockEntityMinoTable tableEntity) {

    }
}