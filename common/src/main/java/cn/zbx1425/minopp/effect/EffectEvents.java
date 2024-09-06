package cn.zbx1425.minopp.effect;

import cn.zbx1425.minopp.Mino;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class EffectEvents {

    public static final EffectEvent.Type<SoundEffectEvent> SOUND = new EffectEvent.Type<>(Mino.id("sound"), SoundEffectEvent.STREAM_CODEC);
    public static final EffectEvent.Type<PlayerFireworkEffectEvent> PLAYER_FIREWORK = new EffectEvent.Type<>(Mino.id("player_firework"), PlayerFireworkEffectEvent.STREAM_CODEC);

    public static final Map<ResourceLocation, EffectEvent.Type<?>> REGISTRY = Map.of(
            SOUND.id(), SOUND,
            PLAYER_FIREWORK.id(), PLAYER_FIREWORK
    );
}
