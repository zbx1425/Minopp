package cn.zbx1425.minopp.neoforge.compat.touhou_little_maid;

import cn.zbx1425.minopp.Mino;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PoiRegistry {
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Mino.MOD_ID);

    public static final DeferredHolder<PoiType, PoiType> MINO_TABLE = POI_TYPES.register("mino_table", SeatPoiManager::getMinoTable);
}
