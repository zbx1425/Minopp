package cn.zbx1425.minopp.forge.compat.touhou_little_maid;

import cn.zbx1425.minopp.Mino;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class PoiRegistry {
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Mino.MOD_ID);

    public static final RegistryObject<PoiType> MINO_TABLE = POI_TYPES.register("mino_table", SeatPoiManager::getMinoTable);
}
