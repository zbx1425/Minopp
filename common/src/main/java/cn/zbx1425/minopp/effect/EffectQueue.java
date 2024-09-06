package cn.zbx1425.minopp.effect;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayPriorityQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EffectQueue {

    private final PriorityQueue<TimedEvent> queue = new ObjectArrayPriorityQueue<>(Comparator.comparingLong(a -> a.time));

    public void tick(Level level) {
        long time = System.currentTimeMillis();
        synchronized (queue) {
            while (!queue.isEmpty() && queue.first().time <= time) {
                queue.dequeue().summon(level);
            }
        }
    }

    public void addAll(List<EffectEvent> events, BlockPos origin, Player self) {
        synchronized (queue) {
            long time = System.currentTimeMillis();
            for (EffectEvent event : events) {
                if (event.target().isEmpty() || event.target().get().equals(self.getGameProfile().getId())) {
                    queue.enqueue(new TimedEvent(event, time, origin));
                }
            }
        }
    }

    private static class TimedEvent {

        private final EffectEvent event;
        private final long time;
        private final BlockPos origin;

        public TimedEvent(EffectEvent incoming, long baseTime, BlockPos origin) {
            this.event = incoming;
            this.time = baseTime + incoming.timeOffset();
            this.origin = origin;
        }

        public void summon(Level level) {
            event.summon(level, origin);
        }
    }
}
