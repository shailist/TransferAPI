/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shailist.TransferAPI.api.transfer.v1.storage;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;


import com.shailist.TransferAPI.api.transfer.v1.storage.base.ResourceAmount;
import com.shailist.TransferAPI.api.transfer.v1.storage.base.SingleSlotStorage;
import com.shailist.TransferAPI.api.transfer.v1.transaction.Transaction;
import com.shailist.TransferAPI.api.transfer.v1.transaction.TransactionContext;

/**
 * Convenience utilities for operating on {@link Storage} instances.
 *
 * <p>Most methods are convenience wrappers that perform common patterns such as simulating
 * transfers, moving resources between storages using nested transactions, or finding
 * extractable resources.
 */
public final class StorageUtil {

    private StorageUtil() {
    }

	/**
	 * Move up to {@code maxAmount} of resources satisfying {@code filter} from {@code from} to {@code to}.
	 *
	 * <p>The method iterates over the non-empty views of {@code from} and attempts to transfer matching
	 * resources into {@code to} using nested transactions so partial transfers can be rolled back.
	 *
	 * @param from the source storage, may be {@code null}
	 * @param to the destination storage, may be {@code null}
	 * @param filter a predicate to select which resources to move, must not be {@code null}
	 * @param maxAmount the maximum total amount to move, must be non-negative
	 * @param transaction the parent transaction context to use for nested transactions
	 * @param <T> the resource type
	 * @return the total amount actually moved
	 * @throws RuntimeException if an unexpected error occurs during the move
	 */
	public static <T> long move(Storage<T> from, Storage<T> to, Predicate<T> filter, long maxAmount, TransactionContext transaction) {
		Objects.requireNonNull(filter, "Filter may not be null");
		if (from == null || to == null) return 0;

		long totalMoved = 0;

		try (Transaction iterationTransaction = Transaction.openNested(transaction)) {
			for (StorageView<T> view : from.nonEmptyViews()) {
				T resource = view.getResource();
				if (!filter.test(resource)) continue;

				long maxExtracted = simulateExtract(view, resource, maxAmount - totalMoved, iterationTransaction);

				try (Transaction transferTransaction = iterationTransaction.openNested()) {
					long accepted = to.insert(resource, maxExtracted, transferTransaction);

					if (view.extract(resource, accepted, transferTransaction) == accepted) {
						totalMoved += accepted;
						transferTransaction.commit();
					}
				}

				if (maxAmount == totalMoved) {
					iterationTransaction.commit();
					return totalMoved;
				}
			}

			iterationTransaction.commit();
		} catch (Exception e) {
			throw new RuntimeException("Move failed", e);
		}

		return totalMoved;
	}

	/**
	 * Simulate an insertion by opening a nested transaction and performing the insert inside it.
	 * The nested transaction is closed before returning so no persistent change occurs.
	 *
	 * @param storage the storage to insert into
	 * @param resource the resource to insert
	 * @param maxAmount the maximum amount to insert
	 * @param transaction the parent transaction context
	 * @param <T> the resource type
	 * @return the amount that would have been inserted
	 */
	public static <T> long simulateInsert(Storage<T> storage, T resource, long maxAmount, TransactionContext transaction) {
		try (Transaction simulateTransaction = Transaction.openNested(transaction)) {
			return storage.insert(resource, maxAmount, simulateTransaction);
		}
	}

	/**
	 * Simulate extracting from a storage by running the extract inside a nested transaction.
	 *
	 * @param storage the storage to extract from
	 * @param resource the resource to extract
	 * @param maxAmount the maximum amount to extract
	 * @param transaction the parent transaction context
	 * @param <T> the resource type
	 * @return the amount that would have been extracted
	 */
	public static <T> long simulateExtract(Storage<T> storage, T resource, long maxAmount, TransactionContext transaction) {
		try (Transaction simulateTransaction = Transaction.openNested(transaction)) {
			return storage.extract(resource, maxAmount, simulateTransaction);
		}
	}

	/**
	 * Simulate extracting from a {@link StorageView} by running the extract inside a nested transaction.
	 *
	 * @param storageView the view to extract from
	 * @param resource the resource to extract
	 * @param maxAmount the maximum amount to extract
	 * @param transaction the parent transaction context
	 * @param <T> the resource type
	 * @return the amount that would have been extracted
	 */
	public static <T> long simulateExtract(StorageView<T> storageView, T resource, long maxAmount, TransactionContext transaction) {
		try (Transaction simulateTransaction = Transaction.openNested(transaction)) {
			return storageView.extract(resource, maxAmount, simulateTransaction);
		}
	}

	/**
	 * Generic simulateExtract for objects that are both {@link Storage} and {@link StorageView}.
	 *
	 * @param storage the storage object
	 * @param resource the resource to extract
	 * @param maxAmount maximum amount to extract
	 * @param transaction parent transaction
	 * @param <T> resource type
	 * @param <S> storage/view type
	 * @return amount that would have been extracted
	 */
	public static <T, S extends Object & Storage<T> & StorageView<T>> long simulateExtract(S storage, T resource, long maxAmount, TransactionContext transaction) {
		try (Transaction simulateTransaction = Transaction.openNested(transaction)) {
			return storage.extract(resource, maxAmount, simulateTransaction);
		}
	}

	/**
	 * Try to extract any non-zero amount of any resource from the storage.
	 *
	 * @param storage the storage to extract from, may be {@code null}
	 * @param maxAmount the maximum amount to extract
	 * @param transaction the transaction context
	 * @param <T> the resource type
	 * @return a {@link ResourceAmount} describing the resource and amount extracted, or {@code null} if none could be extracted
	 * @throws RuntimeException if an unexpected error occurs while extracting
	 */
	public static <T> ResourceAmount<T> extractAny(Storage<T> storage, long maxAmount, TransactionContext transaction) {
		if (storage == null) return null;

		try {
			for (StorageView<T> view : storage.nonEmptyViews()) {
				T resource = view.getResource();
				long amount = view.extract(resource, maxAmount, transaction);
				if (amount > 0) return new ResourceAmount<>(resource, amount);
			}
		} catch (Exception e) {
			throw new RuntimeException("Extract failed", e);
		}

		return null;
	}

	/**
	 * Insert into a list of single-slot storages preferring to stack onto existing
	 * non-empty slots first and then into empty slots.
	 *
	 * @param slots the destination slots
	 * @param resource the resource to insert
	 * @param maxAmount maximum amount to insert
	 * @param transaction transaction context
	 * @param <T> resource type
	 * @return amount actually inserted
	 */
	public static <T> long insertStacking(List<? extends SingleSlotStorage<T>> slots, T resource, long maxAmount, TransactionContext transaction) {
		long amount = 0;

		try {
			for (SingleSlotStorage<T> slot : slots) {
				if (!slot.isResourceBlank()) {
					amount += slot.insert(resource, maxAmount - amount, transaction);
					if (amount == maxAmount) return amount;
				}
			}

			for (SingleSlotStorage<T> slot : slots) {
				amount += slot.insert(resource, maxAmount - amount, transaction);
				if (amount == maxAmount) return amount;
			}
		} catch (Exception e) {
			throw new RuntimeException("Insert stacking failed", e);
		}

		return amount;
	}

	/**
	 * Try to insert into a storage using stacking semantics when it is a {@link SlottedStorage}.
	 *
	 * @param storage the target storage, may be {@code null}
	 * @param resource the resource to insert
	 * @param maxAmount maximum amount to insert
	 * @param transaction transaction context
	 * @param <T> resource type
	 * @return amount inserted
	 */
	public static <T> long tryInsertStacking(Storage<T> storage, T resource, long maxAmount, TransactionContext transaction) {
		try {
			if (storage instanceof SlottedStorage<T> slottedStorage) {
				return insertStacking(slottedStorage.getSlots(), resource, maxAmount, transaction);
			} else if (storage != null) {
				return storage.insert(resource, maxAmount, transaction);
			} else {
				return 0;
			}
		} catch (Exception e) {
			throw new RuntimeException("tryInsertStacking failed", e);
		}
	}

	/**
	 * Find any stored resource in the storage matching the given filter.
	 *
	 * @param storage the storage to inspect
	 * @param <T> the resource type
	 * @return a stored resource instance or {@code null} if none
	 */
	public static <T> T findStoredResource(Storage<T> storage) {
		return findStoredResource(storage, r -> true);
	}

	/**
	 * Find a stored resource that matches the provided filter.
	 *
	 * @param storage the storage to inspect
	 * @param filter predicate that selects acceptable resources, must not be {@code null}
	 * @param <T> the resource type
	 * @return a stored resource matching the filter or {@code null} if none found
	 */
	public static <T> T findStoredResource(Storage<T> storage, java.util.function.Predicate<T> filter) {
		Objects.requireNonNull(filter, "Filter may not be null");
		if (storage == null) return null;

		for (StorageView<T> view : storage.nonEmptyViews()) {
			if (filter.test(view.getResource())) {
				return view.getResource();
			}
		}

		return null;
	}

	/**
	 * Find any extractable resource from {@code storage} by testing whether an extract of any amount would succeed.
	 *
	 * @param storage the storage to inspect
	 * @param transaction current transaction context
	 * @param <T> the resource type
	 * @return a resource that can be extracted, or {@code null} if none
	 */
	public static <T> T findExtractableResource(Storage<T> storage, TransactionContext transaction) {
		return findExtractableResource(storage, r -> true, transaction);
	}

	/**
	 * Find an extractable resource that satisfies the provided filter.
	 *
	 * @param storage the storage to inspect
	 * @param filter predicate to select candidate resources, must not be {@code null}
	 * @param transaction current transaction context
	 * @param <T> resource type
	 * @return a resource that can be extracted or {@code null} if none
	 */
	public static <T> T findExtractableResource(Storage<T> storage, java.util.function.Predicate<T> filter, TransactionContext transaction) {
		Objects.requireNonNull(filter, "Filter may not be null");
		if (storage == null) return null;

		try (Transaction nested = Transaction.openNested(transaction)) {
			for (StorageView<T> view : storage.nonEmptyViews()) {
				T resource = view.getResource();

				if (filter.test(resource) && view.extract(resource, Long.MAX_VALUE, nested) > 0) {
					return resource;
				}
			}
		}

		return null;
	}

	/**
	 * Find an extractable resource and the maximum extractable amount for it.
	 *
	 * @param storage the storage to inspect
	 * @param transaction current transaction context
	 * @param <T> resource type
	 * @return a {@link ResourceAmount} describing the resource and amount, or {@code null} if none
	 */
	public static <T> ResourceAmount<T> findExtractableContent(Storage<T> storage, TransactionContext transaction) {
		return findExtractableContent(storage, r -> true, transaction);
	}

	/**
	 * Find an extractable resource satisfying the filter and return the estimated extractable amount.
	 *
	 * @param storage the storage to inspect
	 * @param filter predicate selecting candidate resources
	 * @param transaction current transaction context
	 * @param <T> resource type
	 * @return a {@link ResourceAmount} describing the resource and extractable amount, or {@code null} if none
	 */
	public static <T> ResourceAmount<T> findExtractableContent(Storage<T> storage, java.util.function.Predicate<T> filter, TransactionContext transaction) {
		T extractableResource = findExtractableResource(storage, filter, transaction);

		if (extractableResource != null) {
			long extractableAmount = simulateExtract(storage, extractableResource, Long.MAX_VALUE, transaction);

			if (extractableAmount > 0) {
				return new ResourceAmount<>(extractableResource, extractableAmount);
			}
		}

		return null;
	}

	/**
	 * Calculate a comparator-like integer output (0-15) representing how full the storage is.
	 *
	 * <p>The algorithm averages the fill ratios of all views and scales to the 0-15 range used
	 * by Minecraft comparator-like logic.
	 *
	 * @param storage the storage to evaluate, may be {@code null}
	 * @param <T> resource type
	 * @return an integer in the range [0, 15] describing the stored fill level
	 */
	public static <T> int calculateComparatorOutput(Storage<T> storage) {
		if (storage == null) return 0;

		double fillPercentage = 0;
		int viewCount = 0;
		boolean hasNonEmptyView = false;

		for (StorageView<T> view : storage) {
			viewCount++;

			if (view.getAmount() > 0) {
				fillPercentage += (double) view.getAmount() / view.getCapacity();
				hasNonEmptyView = true;
			}
		}

		return (int) Math.floor(fillPercentage / viewCount * 14) + (hasNonEmptyView ? 1 : 0);
	}
}
