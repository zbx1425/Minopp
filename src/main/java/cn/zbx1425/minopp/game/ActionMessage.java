package cn.zbx1425.minopp.game;

import cn.zbx1425.minopp.platform.DummyLookupProvider;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

//? if >=26.1 {
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//? } else {
/*import cn.zbx1425.minopp.platform.multiver.ValueOutput;
 *///? }


public record ActionMessage(Type type, Component message) {

    public ActionMessage(ValueInput input) {
        this(
            input.getString("type").map(Type::valueOf).orElse(Type.STATE),
            // This is ugly but we did it in the first place, so for backward compatibility...
            ComponentSerialization.CODEC.parse(JsonOps.INSTANCE,
                JsonParser.parseString(input.getStringOr("message", ""))).getOrThrow()
        );
    }

    public void nbtWriteTo(ValueOutput output) {
        output.putString("type", type.name());
        output.putString("message", ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, message)
            .getOrThrow().toString());
    }

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
