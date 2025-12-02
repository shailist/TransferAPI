package com.shailist.hytale.api.transfer.v1.transaction.types;

import com.shailist.hytale.api.transfer.v1.transaction.TransactionContext;
import com.shailist.hytale.api.transfer.v1.transaction.base.SnapshotParticipant;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class that implements a simple transactional value.
 * Stores a value of the given type, and provides the {@link #getValue()} and {@link #assignValue(T, TransactionContext)} methods
 * for accessing and updating the stored value.
 * @param <T> The type of the stored object.
 */
public class TransactionalValue<T> extends SnapshotParticipant<T> {
    /** The current value stored by this transactional value. */
    protected @NotNull T value;

    /**
     * Create a new transactional value with the provided starting value.
     *
     * @param startingValue The initial stored value.
     */
    public TransactionalValue(@NotNull T startingValue) {
        this.value = startingValue;
    }

    /**
     * Fetches the current stored value.
     * @return The current value.
     */
    public @NotNull T getValue() {
        return this.value;
    }

    /**
     * Stores a new value using the given transaction.
     * @param newValue The new value to store.
     * @param transaction Transaction to use.
     */
    public void assignValue(@NotNull T newValue, @NotNull TransactionContext transaction) {
        updateSnapshots(transaction);
        value = newValue;
    }

    @Override
    protected @NotNull T createSnapshot() {
        // Since we are using reference types, cloning isn't necessary
        return value;
    }

    @Override
    protected void readSnapshot(@NotNull T snapshot) {
        value = snapshot;
    }
}
