package cn.zbx1425.minopp.sound;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayPriorityQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SoundQueue {

    private final PriorityQueue<TimedEvent> queue = new ObjectArrayPriorityQueue<>(Comparator.comparingLong(a -> a.time));

    public void tick(Level level) {
        long time = System.currentTimeMillis();
        synchronized (queue) {
            while (!queue.isEmpty() && queue.first().time <= time) {
                queue.dequeue().play(level);
            }
        }
    }

    public void addAll(List<Event> events, BlockPos origin, Player self) {
        synchronized (queue) {
            long time = System.currentTimeMillis();
            for (Event event : events) {
                if (event.target.isEmpty() || event.target.get().equals(self.getGameProfile().getId())) {
                    queue.enqueue(new TimedEvent(event, time, origin));
                }
            }
        }
    }

    public record Event(SoundEvent sound, int timeOffset, Optional<UUID> target) {

        public static StreamCodec<ByteBuf, Event> STREAM_CODEC = StreamCodec.composite(
                SoundEvent.DIRECT_STREAM_CODEC, Event::sound,
                ByteBufCodecs.INT, Event::timeOffset,
                UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional), Event::target,
                Event::new
        );
    }

    private record TimedEvent(SoundEvent sound, long time, BlockPos origin) {

        public TimedEvent(Event incoming, long baseTime, BlockPos origin) {
            this(incoming.sound(), baseTime + incoming.timeOffset(), origin);
        }

        public void play(Level level) {
            level.playLocalSound(origin, sound, SoundSource.PLAYERS,
                    1, 1, false);
        }
    }
}
