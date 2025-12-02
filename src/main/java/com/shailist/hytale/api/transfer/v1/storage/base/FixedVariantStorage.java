package com.shailist.hytale.api.transfer.v1.storage.base;

import com.shailist.hytale.api.transfer.v1.storage.TransferVariant;
import com.shailist.hytale.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

/**
 * A storage that can store a fixed transfer variant or be empty.
 *
 * @param <T> The transfer variant type handled by this storage.
 * Implementors should at least override {@link #getCapacity(TransferVariant)},
 * {@link #getBlankVariant()}, {@link #getAllowedVariant()}, and probably
 * {@link #onFinalCommit} as well for {@code markDirty()} and similar calls.
 *
 * <p>{@link #canInsert} and {@link #canExtract} can be used for more precise control over which variants may be inserted or extracted.
 * If one of these two functions is overridden to always return false, implementors may also wish to override
 * {@link #supportsInsertion} and/or {@link #supportsExtraction}.
 */
public abstract class FixedVariantStorage<T extends TransferVariant<?>> extends SingleVariantStorage<T> {
    /**
     * Protected no-arg constructor to satisfy doclint requirements.
     */
    protected FixedVariantStorage() {
    }
    
    protected abstract T getBlankVariant();
    /**
     * Return the transfer variant that this storage accepts when not blank.
     *
     * @return The allowed variant for this storage.
     */
    protected abstract T getAllowedVariant();

    @Override
    protected boolean canInsert(T variant) {
        return getAllowedVariant().equals(variant);
    }

    @Override
    protected boolean canExtract(T variant) {
        return getAllowedVariant().equals(variant);
    }

    /**
     * Insert into the storage using the allowed variant.
     *
     * @param maxAmount maximum amount to insert
     * @param transaction transaction context
     * @return amount inserted
     */
    public long insert(long maxAmount, @NotNull TransactionContext transaction) {
        return super.insert(getAllowedVariant(), maxAmount, transaction);
    }

    /**
     * Extract from the storage using the allowed variant.
     *
     * @param maxAmount maximum amount to extract
     * @param transaction transaction context
     * @return amount extracted
     */
    public long extract(long maxAmount, @NotNull TransactionContext transaction) {
        return super.extract(getAllowedVariant(), maxAmount, transaction);
    }
}
