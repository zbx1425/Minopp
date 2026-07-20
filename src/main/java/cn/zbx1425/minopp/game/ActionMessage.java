package cn.zbx1425.minopp.game;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;


public record ActionMessage(Type type, Component message) {

    private static final Codec<Component> JSON_STRING_COMPONENT_CODEC = Codec.STRING.xmap(
        s -> ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(s)).getOrThrow(),
        c -> ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, c).getOrThrow().toString()
    );

    public static final Codec<ActionMessage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.xmap(Type::valueOf, Type::name)
            .optionalFieldOf("type", Type.STATE).forGetter(ActionMessage::type),
        // This is ugly but we did it in the first place, so for backward compatibility...
        JSON_STRING_COMPONENT_CODEC.fieldOf("message").forGetter(ActionMessage::message)
    ).apply(instance, ActionMessage::new));

    public enum Type {
        STATE,
        FAIL,
        MESSAGE_ALL;

        public boolean isEphemeral() {
            return this == FAIL || this == MESSAGE_ALL;
        }
    }

    public static final ActionMessage NO_GAME = new ActionMessage(Type.STATE, Component.translatable("game.minopp.play.no_game"));
}
