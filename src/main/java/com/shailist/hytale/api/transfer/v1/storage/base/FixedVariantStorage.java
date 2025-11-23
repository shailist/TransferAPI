package com.shailist.hytale.api.transfer.v1.storage.base;

import com.shailist.hytale.api.transfer.v1.storage.TransferVariant;
import com.shailist.hytale.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

/**
 * A storage that can store a fixed transfer variant or be empty.
 * Implementors should at least override {@link #getCapacity(TransferVariant)},
 * {@link #getBlankVariant()}, {@link #getAllowedVariant()}, and probably
 * {@link #onFinalCommit} as well for {@code markDirty()} and similar calls.
 *
 * <p>{@link #canInsert} and {@link #canExtract} can be used for more precise control over which variants may be inserted or extracted.
 * If one of these two functions is overridden to always return false, implementors may also wish to override
 * {@link #supportsInsertion} and/or {@link #supportsExtraction}.
 */
public abstract class FixedVariantStorage<T extends TransferVariant<?>> extends SingleVariantStorage<T> {
    protected abstract T getBlankVariant();
    protected abstract T getAllowedVariant();

    @Override
    protected boolean canInsert(T variant) {
        return getAllowedVariant().equals(variant);
    }

    @Override
    protected boolean canExtract(T variant) {
        return getAllowedVariant().equals(variant);
    }

    public long insert(long maxAmount, @NotNull TransactionContext transaction) {
        return super.insert(getAllowedVariant(), maxAmount, transaction);
    }

    public long extract(long maxAmount, @NotNull TransactionContext transaction) {
        return super.extract(getAllowedVariant(), maxAmount, transaction);
    }
}
