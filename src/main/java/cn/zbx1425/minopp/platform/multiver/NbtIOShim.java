package cn.zbx1425.minopp.platform.multiver;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import org.jetbrains.annotations.Nullable;

public class NbtIOShim {

    public static <T> CompoundTag encode(Codec<T> codec, T value) {
        return (CompoundTag) codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow();
    }

    public static <T> CompoundTag encode(Codec<T> codec, T value, HolderLookup.Provider provider) {
        return (CompoundTag) codec.encodeStart(RegistryOps.create(NbtOps.INSTANCE, provider), value).getOrThrow();
    }

    public static <T> T decode(Codec<T> codec, CompoundTag tag) {
        return codec.parse(NbtOps.INSTANCE, tag).getOrThrow();
    }

    public static <T> T decode(Codec<T> codec, CompoundTag tag, HolderLookup.Provider provider) {
        return codec.parse(RegistryOps.create(NbtOps.INSTANCE, provider), tag).getOrThrow();
    }

    @Nullable
    public static <T> T decodeNullable(Codec<T> codec, @Nullable CompoundTag tag) {
        if (tag == null) return null;
        return codec.parse(NbtOps.INSTANCE, tag).result().orElse(null);
    }
}
