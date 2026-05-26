package cn.zbx1425.minopp.platform;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;
import java.util.stream.Stream;

public class DummyLookupProvider implements HolderLookup.Provider {

    @Override
    //? if <26.1
    //public Stream<ResourceKey<? extends Registry<?>>> listRegistries() {
    //? if >=26.1
    public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
        return Stream.empty();
    }

    @Override
    public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
        return Optional.empty();
    }

    public static final DummyLookupProvider INSTANCE = new DummyLookupProvider();
}
