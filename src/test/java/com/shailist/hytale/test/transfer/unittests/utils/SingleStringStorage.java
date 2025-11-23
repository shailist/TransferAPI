package com.shailist.hytale.test.transfer.unittests.utils;

import com.shailist.hytale.api.transfer.v1.storage.StoragePreconditions;
import com.shailist.hytale.api.transfer.v1.storage.TransferVariant;
import com.shailist.hytale.api.transfer.v1.storage.base.SingleVariantStorage;

import java.util.Objects;

/**
 * A storage that can store a single string variant at any given time.
 * Implementors should at least override {@link #getCapacity(TransferVariant) getCapacity(FluidVariant)},
 * and probably {@link #onFinalCommit} as well for {@code markDirty()} and similar calls.
 *
 * <p>This is a convenient specialization of {@link SingleVariantStorage} for fluids that additionally offers methods
 * to deserialize the contents of the storage.
 */
public abstract class SingleStringStorage extends SingleVariantStorage<StringVariant> {
    /**
     * Create a fluid storage with a fixed capacity and a change handler.
     *
     * @param capacity Fixed capacity of the fluid storage. Must be non-negative.
     * @param onChange Change handler, generally for {@code markDirty()} or similar calls. May not be null.
     */
    public static SingleStringStorage withFixedCapacity(long capacity, Runnable onChange) {
        StoragePreconditions.notNegative(capacity);
        Objects.requireNonNull(onChange, "onChange may not be null");

        return new SingleStringStorage() {
            @Override
            protected long getCapacity(StringVariant variant) {
                return capacity;
            }

            @Override
            protected void onFinalCommit() {
                onChange.run();
            }
        };
    }

    @Override
    protected final StringVariant getBlankVariant() {
        return StringVariant.blank();
    }
}

