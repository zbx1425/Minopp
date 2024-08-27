package cn.zbx1425.minopp.block;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.game.ActionMessage;
import cn.zbx1425.minopp.game.CardGame;
import cn.zbx1425.minopp.game.CardPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class BlockEntityMinoTable extends BlockEntity {

    public Map<Direction, CardPlayer> players = new HashMap<>();
    public CardGame game = null;
    public ActionMessage state = ActionMessage.NO_GAME;

    public BlockEntityMinoTable(BlockPos blockPos, BlockState blockState) {
        super(Mino.BLOCK_ENTITY_TYPE_MINO_TABLE.get(), blockPos, blockState);
        players.put(Direction.NORTH, null);
        players.put(Direction.EAST, null);
        players.put(Direction.SOUTH, null);
        players.put(Direction.WEST, null);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        CompoundTag playersTag = new CompoundTag();
        for (Map.Entry<Direction, CardPlayer> entry : players.entrySet()) {
            if (entry.getValue() != null) {
                playersTag.put(entry.getKey().getSerializedName(), entry.getValue().toTag());
            }
        }
        compoundTag.put("players", playersTag);
        if (game != null) {
            compoundTag.put("game", game.toTag());
        }
        compoundTag.put("state", state.toTag());
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        CompoundTag playersTag = compoundTag.getCompound("players");
        for (Direction direction : Direction.values()) {
            if (playersTag.contains(direction.getSerializedName())) {
                players.put(direction, new CardPlayer(playersTag.getCompound(direction.getSerializedName())));
            }
        }
        if (compoundTag.contains("game")) {
            game = new CardGame(compoundTag.getCompound("game"));
        } else {
            game = null;
        }
        state = new ActionMessage(compoundTag.getCompound("state"));
    }
}
