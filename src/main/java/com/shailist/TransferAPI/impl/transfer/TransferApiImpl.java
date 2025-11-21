package com.shailist.TransferAPI.impl.transfer;

import java.util.AbstractList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

import com.shailist.TransferAPI.api.transfer.v1.storage.SlottedStorage;
import com.shailist.TransferAPI.api.transfer.v1.storage.Storage;
import com.shailist.TransferAPI.api.transfer.v1.storage.StorageView;
import com.shailist.TransferAPI.api.transfer.v1.storage.base.SingleSlotStorage;
import com.shailist.TransferAPI.api.transfer.v1.transaction.TransactionContext;

/**
 * Internal utilities used by the standalone Transfer API implementation.
 *
 * <p>These helpers are not part of the public API surface but are used by the library during
 * construction of default values and lightweight adapters.
 */
public class TransferApiImpl {
	/** Version counter used for {@link com.shailist.TransferAPI.api.transfer.v1.storage.Storage#getVersion}. */
	public static final AtomicLong version = new AtomicLong();

	/** Prevent instantiation of this utility class. */
	private TransferApiImpl() {
	}

	/**
	 * Empty storage singleton used as a safe no-op storage.
	 */
	@SuppressWarnings("rawtypes")
	public static final Storage EMPTY_STORAGE = new Storage() {
		@Override
		public boolean supportsInsertion() {
			return false;
		}

		@Override
		public long insert(Object resource, long maxAmount, TransactionContext transaction) {
			return 0;
		}

		@Override
		public boolean supportsExtraction() {
			return false;
		}

		@Override
		public long extract(Object resource, long maxAmount, TransactionContext transaction) {
			return 0;
		}

		@Override
		public Iterator<StorageView> iterator() {
			return Collections.emptyIterator();
		}

		@Override
		public long getVersion() {
			return 0;
		}

		@Override
		public String toString() {
			return "EmptyStorage";
		}
	};

	/**
	 * Create an iterator that yields a single element.
	 *
	 * @param it single element to yield
	 * @param <T> element type
	 * @return an {@link Iterator} that returns the given element once
	 */
	public static <T> Iterator<T> singletonIterator(T it) {
		return new Iterator<T>() {
			boolean hasNext = true;

			@Override
			public boolean hasNext() {
				return hasNext;
			}

			@Override
			public T next() {
				if (!hasNext) {
					throw new NoSuchElementException();
				}

				hasNext = false;
				return it;
			}
		};
	}

	/**
	 * Wrap a {@link SlottedStorage} as a {@link List} view of {@link SingleSlotStorage} elements.
	 *
	 * @param storage slotted storage to adapt
	 * @param <T> resource type
	 * @return a {@link List} view delegating to {@code storage}
	 */
	public static <T> List<SingleSlotStorage<T>> makeListView(SlottedStorage<T> storage) {
		return new AbstractList<>() {
			@Override
			public SingleSlotStorage<T> get(int index) {
				return storage.getSlot(index);
			}

			@Override
			public int size() {
				return storage.getSlotCount();
			}
		};
	}
}
