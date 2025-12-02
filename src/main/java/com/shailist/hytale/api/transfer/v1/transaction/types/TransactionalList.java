package com.shailist.hytale.api.transfer.v1.transaction.types;

import com.shailist.hytale.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Helper class that implements a transactional {@link List}.
 * Extends {@link TransactionalValue} and provides transaction aware implementations for all modifying methods of {@link List}.
 * @param <T> The type of the list's elements.
 */
public class TransactionalList<T> extends TransactionalValue<List<T>> {
    /**
     * Create a new transactional list with the given starting value.
     *
     * @param startingValue Initial backing list.
     */
    public TransactionalList(@NotNull List<T> startingValue) {
        super(startingValue);
    }

    @Override
    protected @NotNull List<T> createSnapshot() {
        // Since List<T> is mutable, we must return a clone, or at least a new list
        return new ArrayList<>(value);
    }

    /**
     * Add an element using the provided transaction.
     *
     * @param t The element to add.
     * @param transaction The transaction to use.
     * @return true if the list changed as a result of the call.
     */
    public boolean add(T t, @NotNull TransactionContext transaction) {
        updateSnapshots(transaction);
        return value.add(t);
    }

    /**
     * Remove an element using the provided transaction.
     *
     * @param o element to be removed
     * @param transaction the transaction to use
     * @return true if the list contained the specified element
     */
    public boolean remove(T o, @NotNull TransactionContext transaction) {
        updateSnapshots(transaction);
        return value.remove(o);
    }


    /**
     * Add all elements from the collection using the provided transaction.
     *
     * @param c collection to add
     * @param transaction transaction to use
     * @return true if the list changed as a result
     */
    public boolean addAll(@NotNull Collection<? extends T> c, @NotNull TransactionContext transaction) {
        updateSnapshots(transaction);
        return value.addAll(c);
    }

    /**
     * Add all elements at the given index using the provided transaction.
     *
     * @param index insertion index
     * @param c collection to add
     * @param transaction transaction to use
     * @return true if the list changed as a result
     */
    public boolean addAll(int index, @NotNull Collection<? extends T> c, @NotNull TransactionContext transaction) {
        updateSnapshots(transaction);
        return value.addAll(index, c);
    }

    /**
     * Remove all elements in the collection using the provided transaction.
     *
     * @param c collection to remove
     * @param transaction transaction to use
     * @return true if the list changed as a result
     */
    public boolean removeAll(@NotNull Collection<? extends T> c, @NotNull TransactionContext transaction) {
        updateSnapshots(transaction);
        return value.removeAll(c);
    }

    /**
     * Retain only the elements in the specified collection, using the provided transaction.
     *
     * @param c collection to retain
     * @param transaction transaction to use
     * @return true if the list changed as a result
     */
    public boolean retainAll(@NotNull Collection<? extends T> c, @NotNull TransactionContext transaction) {
        updateSnapshots(transaction);
        return value.retainAll(c);
    }

    /**
     * Clear the list using the provided transaction.
     *
     * @param transaction transaction to use
     */
    public void clear(@NotNull TransactionContext transaction) {
        updateSnapshots(transaction);
        value.clear();
    }

    /**
     * Replace the element at the specified position with the given element using the provided transaction.
     *
     * @param index index of the element to replace
     * @param element element to set
     * @param transaction transaction to use
     * @return the element previously at the specified position
     */
    public T set(int index, T element, @NotNull TransactionContext transaction) {
        updateSnapshots(transaction);
        return value.set(index, element);
    }

    /**
     * Insert the specified element at the specified position using the provided transaction.
     *
     * @param index index at which the element is to be inserted
     * @param element element to insert
     * @param transaction transaction to use
     */
    public void add(int index, T element, @NotNull TransactionContext transaction) {
        updateSnapshots(transaction);
        value.add(index, element);
    }

    /**
     * Remove the element at the specified position using the provided transaction.
     *
     * @param index index of the element to remove
     * @param transaction transaction to use
     * @return the element previously at the specified position
     */
    public T remove(int index, @NotNull TransactionContext transaction) {
        updateSnapshots(transaction);
        return value.remove(index);
    }
}
