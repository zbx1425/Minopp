package cn.zbx1425.minopp.game.effect;

import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import cn.zbx1425.minopp.gui.TurnDeadMan;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

public record SoundEffectEvent(int timeOffset, Optional<UUID> target, SoundEvent sound) implements EffectEvent {

    public static SoundEffectEvent streamDecode(FriendlyByteBuf buf) {
        int timeOffset = buf.readInt();
        Optional<UUID> target = buf.readOptional(b -> b.readUUID());
        //? if <1.20.5 {
        /*SoundEvent sound = SoundEvent.readFromNetwork(buf);
        *///? } else {
        SoundEvent sound = SoundEvent.DIRECT_STREAM_CODEC.decode(buf);
        //? }
        return new SoundEffectEvent(timeOffset, target, sound);
    }

    @Override
    public void streamEncode(FriendlyByteBuf buf) {
        buf.writeInt(timeOffset);
        buf.writeOptional(target, (b, uuid) -> b.writeUUID(uuid));
        //? if <1.20.5 {
        /*sound.writeToNetwork(buf);
        *///? } else {
        SoundEvent.DIRECT_STREAM_CODEC.encode(buf, sound);
        //? }
    }

    @Override
    public Type<SoundEffectEvent> type() {
        return EffectEvents.SOUND;
    }

    @Override
    public void summonClient(Level level, BlockPos origin, boolean selfPartOfSourceGame) {
        level.playLocalSound(origin, sound, SoundSource.PLAYERS, 1, 1, false);
        if (selfPartOfSourceGame) {
            TurnDeadMan.pedal();
        }
    }

    @Override
    public void summonServer(ServerLevel level, BlockPos origin, BlockEntityMinoTable tableEntity) {

    }
}
