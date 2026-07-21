package cn.zbx1425.minopp.game;

import cn.zbx1425.minopp.platform.multiver.PlayerShim;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class CardPlayer {

    public UUID uuid;
    public String name;

    public ArrayList<Card> hand = new ArrayList<>();

    public boolean hasShoutedMino = false;
    public int swapGeneration = 0;

    public CardPlayer(Player mcPlayer) {
        this.uuid = PlayerShim.getGameProfileId(mcPlayer);
        this.name = PlayerShim.getGameProfileName(mcPlayer);
    }

    public CardPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    private CardPlayer(UUID uuid, String name, ArrayList<Card> hand, boolean hasShoutedMino, int swapGeneration) {
        this.uuid = uuid;
        this.name = name;
        this.hand = hand;
        this.hasShoutedMino = hasShoutedMino;
        this.swapGeneration = swapGeneration;
    }

    public static final Codec<CardPlayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.optionalFieldOf("uuid", Util.NIL_UUID).forGetter(p -> p.uuid),
        Codec.STRING.optionalFieldOf("name", "").forGetter(p -> p.name),
        Card.CODEC.listOf().xmap(ArrayList::new, Function.identity())
            .optionalFieldOf("hand").xmap(opt -> opt.orElseGet(ArrayList::new), Optional::of).forGetter(p -> p.hand),
        Codec.BOOL.optionalFieldOf("hasShoutedMino", false).forGetter(p -> p.hasShoutedMino),
        Codec.INT.optionalFieldOf("swapGeneration", 0).forGetter(p -> p.swapGeneration)
    ).apply(instance, CardPlayer::new));

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardPlayer that = (CardPlayer) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
