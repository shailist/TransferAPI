package com.shailist.TransferAPI.impl.transfer.transaction;

import com.shailist.TransferAPI.api.transfer.v1.transaction.Transaction;
import com.shailist.TransferAPI.api.transfer.v1.transaction.TransactionContext;

/**
 * Very small, thread-local transaction manager implementation used by the standalone API.
 *
 * <p>This manager provides basic stubs sufficient for running tests and documentation generation.
 */
public class TransactionManagerImpl {
	/** Prevent instantiation of this utility holder class. */
	private TransactionManagerImpl() {
	}
	/** Thread-local provider of managers for each thread. */
	public static final ThreadLocal<Manager> MANAGERS = ThreadLocal.withInitial(Manager::new);

	/**
	 * Per-thread manager providing transaction operations.
	 */
	public static class Manager {
		/** Create a manager for a thread. */
		public Manager() {
		}

		/**
		 * Open a new outermost transaction.
		 *
		 * @return a newly created outer {@link Transaction}
		 */
		public Transaction openOuter() {
			return new SimpleTransaction();
		}

		/**
		 * Return the current lifecycle state.
		 *
		 * @return the {@link Transaction.Lifecycle} representing the current state
		 */
		public Transaction.Lifecycle getLifecycle() {
			return Transaction.Lifecycle.NONE;
		}

		/**
		 * Unsafe access to the current transaction context (legacy compatibility).
		 *
		 * @return the current {@link TransactionContext} or {@code null}
		 */
		public TransactionContext getCurrentUnsafe() {
			return null;
		}

		/**
		 * Get the current transaction context wrapper for this manager.
		 *
		 * @return the current {@link TransactionContext} or {@code null}
		 */
		public TransactionContext get() {
			return null;
		}
	}

	/**
	 * A trivial {@link Transaction} implementation used as a placeholder.
	 */
	private static class SimpleTransaction implements Transaction {
		@Override
		public Transaction openNested() {
			return this;
		}

		@Override
		public int nestingDepth() {
			return 0;
		}

		@Override
		public Transaction getOpenTransaction(int nestingDepth) {
			throw new IndexOutOfBoundsException();
		}

		@Override
		public void addCloseCallback(CloseCallback closeCallback) {
		}

		@Override
		public void addOuterCloseCallback(OuterCloseCallback outerCloseCallback) {
		}

		@Override
		public void abort() {
		}

		@Override
		public void commit() {
		}

		@Override
		public void close() {
		}
	}
}
