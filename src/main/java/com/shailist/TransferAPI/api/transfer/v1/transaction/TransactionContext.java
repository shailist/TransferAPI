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

package com.shailist.TransferAPI.api.transfer.v1.transaction;

/**
 * A subset of a {@link Transaction} that lets participants properly take part in transactions, manage their state,
 * or open nested transactions, but does not allow them to close the transaction they are passed.
 */
public interface TransactionContext {
	/**
	 * Open a new nested transaction.
	 *
	 * @throws IllegalStateException If this function is not called on the thread this transaction was opened in.
	 * @throws IllegalStateException If this transaction is not the current transaction.
	 * @throws IllegalStateException If this transaction was closed.
	 */
	/**
	 * Open a new nested {@link Transaction} and return it.
	 *
	 * @return a newly opened nested {@link Transaction}
	 * @throws IllegalStateException if called from the wrong thread, when this transaction is not current, or if this transaction is closed
	 */
	Transaction openNested();

	/**
	 * Return the nesting depth of this transaction.
	 *
	 * @return the nesting depth: 0 for an outer transaction, 1 for a direct nested child, and so on
	 * @throws IllegalStateException if called on the wrong thread
	 */
	int nestingDepth();

	/**
	 * Return the open transaction with the given nesting depth.
	 *
	 * @param nestingDepth the nesting depth to query
	 * @return the {@link Transaction} at the given depth
	 * @throws IndexOutOfBoundsException if there is no open transaction for the requested depth
	 * @throws IllegalStateException if called on the wrong thread
	 */
	Transaction getOpenTransaction(int nestingDepth);

	/**
	 * Register a callback that will be invoked when this transaction is closed.
	 * Registered callbacks are invoked last-to-first: the last callback to be registered will be the first to be invoked.
	 *
	 * <p>Updates that may change the state of other participants should be deferred until after the outermost transaction is closed
	 * using {@link #addOuterCloseCallback}.
	 *
	 * @param closeCallback the callback to register
	 * @throws IllegalStateException if this function is not called on the thread this transaction was opened in
	 */
	void addCloseCallback(CloseCallback closeCallback);

	/**
	 * A callback that will be invoked when a transaction is closed.
	 */
	@FunctionalInterface
	interface CloseCallback {
		/**
		 * Called when the transaction closes.
		 *
		 * @param transaction the transaction context being closed; callers may query {@link #nestingDepth}, {@link #getOpenTransaction}
		 *                    and may register outer close callbacks on parent transactions
		 * @param result the result of the close operation (committed or aborted)
		 */
		void onClose(TransactionContext transaction, Result result);
	}

	/**
	 * Register a callback that will be invoked after the outermost transaction is closed,
	 * and after callbacks registered with {@link #addCloseCallback} have run.
	 * Registered callbacks are invoked last-to-first.
	 *
	 * @param outerCloseCallback the callback to invoke after the outermost close
	 * @throws IllegalStateException if this function is not called on the thread this transaction was opened in
	 */
	void addOuterCloseCallback(OuterCloseCallback outerCloseCallback);

	/**
	 * A callback that is invoked after the outer transaction is closed.
	 */
	@FunctionalInterface
	interface OuterCloseCallback {
		/**
		 * Perform an action after the top-level transaction is closed.
		 *
		 * @param result The result of the top-level transaction.
		 */
		void afterOuterClose(Result result);
	}

	/**
	 * The result of a transaction operation.
	 */
	enum Result {
		/** The transaction was aborted and participants should revert changes. */
		ABORTED,
		/** The transaction was committed and participants may finalize changes. */
		COMMITTED;

		/**
		 * Returns whether this result indicates the transaction was aborted.
		 *
		 * @return {@code true} if the transaction was aborted, {@code false} if it was committed
		 */
		public boolean wasAborted() {
			return this == ABORTED;
		}

		/**
		 * Returns whether this result indicates the transaction was committed.
		 *
		 * @return {@code true} if the transaction was committed, {@code false} if it was aborted
		 */
		public boolean wasCommitted() {
			return this == COMMITTED;
		}
	}
}
