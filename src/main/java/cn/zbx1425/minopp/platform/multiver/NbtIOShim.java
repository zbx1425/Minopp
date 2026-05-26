package cn.zbx1425.minopp.platform.multiver;

import cn.zbx1425.minopp.platform.DummyLookupProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.function.Consumer;
import java.util.function.Function;

public class NbtIOShim {

    public static CompoundTag pourOne(Consumer<ValueOutput> writer) {
        TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        writer.accept(output);
        return output.buildResult();
    }

    public static CompoundTag pourOne(Consumer<ValueOutput> writer, HolderLookup.Provider registries) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        writer.accept(output);
        return output.buildResult();
    }

    public static <T> T fillOne(Function<ValueInput, T> reader, CompoundTag tag) {
        if (tag == null) return null;
        ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, new DummyLookupProvider(), tag);
        return reader.apply(input);
    }

    public static void topUpOne(Consumer<ValueInput> reader, CompoundTag tag) {
        if (tag == null) return;
        ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, new DummyLookupProvider(), tag);
        reader.accept(input);
    }

    public static <T> T fillOne(Function<ValueInput, T> reader, HolderLookup.Provider registries, CompoundTag tag) {
        if (tag == null) return null;
        ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, registries, tag);
        return reader.apply(input);
    }
}
