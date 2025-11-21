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

package com.shailist.TransferAPI.api.transfer.v1.storage.base;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringJoiner;

import com.shailist.TransferAPI.api.transfer.v1.storage.Storage;
import com.shailist.TransferAPI.api.transfer.v1.storage.StoragePreconditions;
import com.shailist.TransferAPI.api.transfer.v1.storage.StorageView;
import com.shailist.TransferAPI.api.transfer.v1.transaction.TransactionContext;

/**
 * A {@link Storage} implementation that delegates operations to a list of component storages.
 *
 * <p>Useful when an inventory is composed of several sub-storages and should behave like a single
 * large storage. Insertions and extractions are attempted sequentially on the component parts until
 * the desired amount is handled or all parts are exhausted.
 *
 * @param <T> the resource type
 * @param <S> the concrete storage type for each part
 */
public class CombinedStorage<T, S extends Storage<T>> implements Storage<T> {
	/** The list of storage parts combined into this storage. */
	public List<S> parts;

	/**
	 * Create a new combined storage from the provided list of parts.
	 *
	 * @param parts the component storages combined by this instance; must not be null
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
	public long insert(T resource, long maxAmount, TransactionContext transaction) {
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
	public long extract(T resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notNegative(maxAmount);
		long amount = 0;

		for (S part : parts) {
			amount += part.extract(resource, maxAmount - amount, transaction);
			if (amount == maxAmount) break;
		}

		return amount;
	}

	@Override
	public Iterator<StorageView<T>> iterator() {
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

	private class CombinedIterator implements Iterator<StorageView<T>> {
		final Iterator<S> partIterator = parts.iterator();
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
