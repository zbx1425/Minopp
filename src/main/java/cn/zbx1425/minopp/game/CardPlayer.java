package cn.zbx1425.minopp.game;

import cn.zbx1425.minopp.platform.multiver.PlayerShim;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;

//? if >=26.1 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//? } else {
/*import cn.zbx1425.minopp.platform.multiver.ValueOutput;
*///? }

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class CardPlayer {

    public UUID uuid;
    public String name;

    public List<Card> hand = new ArrayList<>();

    public boolean hasShoutedMino = false;

    public CardPlayer(Player mcPlayer) {
        this.uuid = PlayerShim.getGameProfileId(mcPlayer);
        this.name = PlayerShim.getGameProfileName(mcPlayer);
    }

    public CardPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public CardPlayer(ValueInput input) {
        this.uuid = input.read("uuid", UUIDUtil.CODEC).orElse(Util.NIL_UUID);
        this.name = input.getStringOr("name", "");
        this.hand = input.childrenListOrEmpty("hand").stream().map(Card::new)
            .collect(Collectors.toCollection(ArrayList::new));
        this.hasShoutedMino = input.getBooleanOr("hasShoutedMino", false);
    }

    public void nbtWriteTo(ValueOutput output) {
        output.store("uuid", UUIDUtil.CODEC, uuid);
        output.putString("name", name);
        var handTag = output.childrenList("hand");
        for (Card card : hand) card.nbtWriteTo(handTag.addChild());
        output.putBoolean("hasShoutedMino", hasShoutedMino);
    }

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
