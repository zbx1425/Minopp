package cn.zbx1425.minopp.effect;

import cn.zbx1425.minopp.block.BlockEntityMinoTable;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record PlayerFireworkEffectEvent(int timeOffset, UUID targetPlayer, CompoundTag firework) implements EffectEvent {

    public static final Serializer<PlayerFireworkEffectEvent> SERIALIZER = new Serializer<>() {
        @Override
        public void serialize(FriendlyByteBuf buf, PlayerFireworkEffectEvent event) {
            buf.writeInt(event.timeOffset);
            buf.writeUUID(event.targetPlayer);
            buf.writeNbt(event.firework);
        }

        @Override
        public PlayerFireworkEffectEvent deserialize(FriendlyByteBuf buf) {
            int timeOffset = buf.readInt();
            UUID targetPlayer = buf.readUUID();
            CompoundTag firework = buf.readNbt();
            return new PlayerFireworkEffectEvent(timeOffset, targetPlayer, firework);
        }
    };

    @Override
    public Optional<UUID> target() {
        return Optional.empty();
    }

    @Override
    public Type<PlayerFireworkEffectEvent> type() {
        return EffectEvents.PLAYER_FIREWORK;
    }

    @Override
    public void summonClient(Level level, BlockPos origin, boolean selfPartOfSourceGame) {
        Player player = level.getPlayerByUUID(targetPlayer);
        if (player != null) {
            level.createFireworks(player.getX(), player.getY() + 3, player.getZ(), 0, 0, 0, firework);
            return;
        }
        List<Entity> entities = level.getEntities((Entity)null,
                AABB.ofSize(Vec3.atLowerCornerOf(origin), 8, 8, 4),
                it -> it.getUUID().equals(targetPlayer));
        if (!entities.isEmpty()) {
            Entity entity = entities.get(0);
            level.createFireworks(entity.getX(), entity.getY() + 3, entity.getZ(), 0, 0, 0, firework);
            return;
        }
        level.createFireworks(origin.getX() + 0.5f, origin.getY() + 3, origin.getZ() + 0.5f, 0, 0, 0, firework);
    }

    @Override
    public void summonServer(ServerLevel level, BlockPos origin, BlockEntityMinoTable tableEntity) {

    }

    public static final CompoundTag WIN_EXPLOSION;

    static {
        try {
            WIN_EXPLOSION = TagParser.parseTag("{Explosions:[" +
                    "{Type:0,Colors:[13840175,16011550],FadeColors:[15702682,16755601],Trail:0,Flicker:0}," +
                    "{Type:1,Colors:[16635957,12634675],FadeColors:[16774557,15134364],Trail:0,Flicker:0}" +
                    "]}");
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
