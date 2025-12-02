/*
 * Copyright (c) 2025 Shai List and contributors
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 *
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

package com.shailist.hytale.api.transfer.v1.storage.base;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringJoiner;

import com.shailist.hytale.api.transfer.v1.storage.Storage;
import com.shailist.hytale.api.transfer.v1.storage.StoragePreconditions;
import com.shailist.hytale.api.transfer.v1.storage.StorageView;
import com.shailist.hytale.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link Storage} wrapping multiple storages.
 *
 * <p>The storages passed to {@linkplain CombinedStorage#CombinedStorage the constructor} will be iterated in order.
 *
 * @param <T> The type of the stored resources.
 * @param <S> The class of every part. {@code ? extends Storage<T>} can be used if the parts are of different types.
 */
public class CombinedStorage<T, S extends Storage<T>> implements Storage<T> {
	/**
	 * The list of backing parts that make up this combined storage. Iterated in order.
	 */
	public List<S> parts;

	/**
	 * Create a combined storage delegating to the provided parts.
	 *
	 * @param parts The list of parts backing this combined storage.
	 */
	public CombinedStorage(List<S> parts) {
		this.parts = parts;
	}

	@Override
	public boolean supportsInsertion() {
		for (S part : parts) {
			if (part.supportsInsertion()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public long insert(T resource, long maxAmount, @NotNull TransactionContext transaction) {
		StoragePreconditions.notNegative(maxAmount);
		long amount = 0;

		for (S part : parts) {
			amount += part.insert(resource, maxAmount - amount, transaction);
			if (amount == maxAmount) break;
		}

		return amount;
	}

	@Override
	public boolean supportsExtraction() {
		for (S part : parts) {
			if (part.supportsExtraction()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public long extract(T resource, long maxAmount, @NotNull TransactionContext transaction) {
		StoragePreconditions.notNegative(maxAmount);
		long amount = 0;

		for (S part : parts) {
			amount += part.extract(resource, maxAmount - amount, transaction);
			if (amount == maxAmount) break;
		}

		return amount;
	}

	@Override
	public @NotNull Iterator<StorageView<T>> iterator() {
		return new CombinedIterator();
	}

	@Override
	public String toString() {
		StringJoiner partNames = new StringJoiner(", ");

		for (S part : parts) {
			partNames.add(part.toString());
		}

		return "CombinedStorage[" + partNames + "]";
	}

	/**
	 * The combined iterator for multiple storages.
	 */
	private class CombinedIterator implements Iterator<StorageView<T>> {
		final Iterator<S> partIterator = parts.iterator();
		// Always holds the next StorageView<T>, except during next() while the iterator is being advanced.
		Iterator<? extends StorageView<T>> currentPartIterator = null;

		CombinedIterator() {
			advanceCurrentPartIterator();
		}

		@Override
		public boolean hasNext() {
			return currentPartIterator != null && currentPartIterator.hasNext();
		}

		@Override
		public StorageView<T> next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			StorageView<T> returned = currentPartIterator.next();

			// Advance the current part iterator
			if (!currentPartIterator.hasNext()) {
				advanceCurrentPartIterator();
			}

			return returned;
		}

		private void advanceCurrentPartIterator() {
			while (partIterator.hasNext()) {
				this.currentPartIterator = partIterator.next().iterator();

				if (this.currentPartIterator.hasNext()) {
					break;
				}
			}
		}
	}
}
