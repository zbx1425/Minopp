package cn.zbx1425.minopp.effect;

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

    public static final Serializer<SoundEffectEvent> SERIALIZER = new Serializer<>() {
        @Override
        public void serialize(FriendlyByteBuf buf, SoundEffectEvent event) {
            buf.writeInt(event.timeOffset);
            buf.writeOptional(event.target, FriendlyByteBuf::writeUUID);
            buf.writeResourceLocation(event.sound.getLocation());
        }

        @Override
        public SoundEffectEvent deserialize(FriendlyByteBuf buf) {
            int timeOffset = buf.readInt();
            Optional<UUID> target = buf.readOptional(FriendlyByteBuf::readUUID);
            SoundEvent sound = SoundEvent.createFixedRangeEvent(buf.readResourceLocation(), 16);
            return new SoundEffectEvent(timeOffset, target, sound);
        }
    };

    @Override
    public Type<SoundEffectEvent> type() {
        return EffectEvents.SOUND;
    }

    @Override
    public void summonClient(Level level, BlockPos origin, boolean selfPartOfSourceGame) {
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