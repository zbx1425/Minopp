package cn.zbx1425.minopp.platform.multiver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

public class ValueOutput {
    private final HolderLookup.Provider holders;
    private final CompoundTag output;

    public ValueOutput(final HolderLookup.Provider holders, final CompoundTag output) {
        this.holders = holders;
        this.output = output;
    }

    public <T> void store(final String name, final Codec<T> codec, final T value) {
        DataResult<Tag> result = codec.encodeStart(holders.createSerializationContext(NbtOps.INSTANCE), value);
        if (result.isSuccess()) {
            this.output.put(name, result.getOrThrow());
        } else {
            result.error().orElseThrow().partialValue().ifPresent((partial) -> this.output.put(name, partial));
        }
    }

    public void putBoolean(final String name, final boolean value) {
        this.output.putBoolean(name, value);
    }

    public void putByte(final String name, final byte value) {
        this.output.putByte(name, value);
    }

    public void putShort(final String name, final short value) {
        this.output.putShort(name, value);
    }

    public void putInt(final String name, final int value) {
        this.output.putInt(name, value);
    }

    public void putLong(final String name, final long value) {
        this.output.putLong(name, value);
    }

    public void putFloat(final String name, final float value) {
        this.output.putFloat(name, value);
    }

    public void putDouble(final String name, final double value) {
        this.output.putDouble(name, value);
    }

    public void putString(final String name, final String value) {
        this.output.putString(name, value);
    }

    public void putIntArray(final String name, final int[] value) {
        this.output.putIntArray(name, value);
    }

    public ValueOutput child(final String name) {
        CompoundTag childTag = new CompoundTag();
        this.output.put(name, childTag);
        return new ValueOutput(holders, childTag);
    }

    public ValueOutput.ListWrapper childrenList(final String name) {
        ListTag childList = new ListTag();
        this.output.put(name, childList);
        return new ValueOutput.ListWrapper(name, childList, holders);
    }

    public void discard(final String name) {
        this.output.remove(name);
    }

    public boolean isEmpty() {
        return this.output.isEmpty();
    }

    public CompoundTag buildResult() {
        return this.output;
    }

    public static class ListWrapper {
        private final String fieldName;
        private final HolderLookup.Provider holders;
        private final ListTag output;

        private ListWrapper(final String fieldName, final ListTag output,  final HolderLookup.Provider holders) {
            this.fieldName = fieldName;
            this.output = output;
            this.holders = holders;
        }

        public ValueOutput addChild() {
            int newChildIndex = this.output.size();
            CompoundTag child = new CompoundTag();
            this.output.add(child);
            return new ValueOutput(holders, child);
        }

        public void discardLast() {
            this.output.removeLast();
        }

        public boolean isEmpty() {
            return this.output.isEmpty();
        }
    }
}