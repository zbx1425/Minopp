package cn.zbx1425.minopp.neoforge.compat.touhou_little_maid;

import cn.zbx1425.minopp.Mino;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public class SeatPoiManager {
    private static final Set<BlockState> MINO_TABLE = ImmutableList.of(Mino.BLOCK_MINO_TABLE.get())
            .stream().flatMap(block -> block.getStateDefinition().getPossibleStates().stream())
            .collect(ImmutableSet.toImmutableSet());

    public static PoiType getMinoTable() {
        return new PoiType(MINO_TABLE, 0, 1);
    }
}
