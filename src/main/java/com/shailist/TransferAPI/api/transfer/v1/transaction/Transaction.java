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

import com.shailist.TransferAPI.impl.transfer.transaction.TransactionManagerImpl;

/**
 * A global operation where participants guarantee atomicity: either the whole operation succeeds,
 * or it is completely aborted and rolled back.
 */
public interface Transaction extends AutoCloseable, TransactionContext {
	/**
	 * Open an outermost transaction for the current thread/manager.
	 *
	 * @return a new outer {@link Transaction}
	 */
	static Transaction openOuter() {
		return TransactionManagerImpl.MANAGERS.get().openOuter();
	}

	/**
	 * Return whether a transaction is currently open in this thread/manager.
	 *
	 * @return {@code true} if a transaction is open, {@code false} otherwise
	 */
	static boolean isOpen() {
		return getLifecycle() != Lifecycle.NONE;
	}

	/**
	 * Return the lifecycle state of the current transaction manager.
	 *
	 * @return the current {@link Lifecycle}
	 */
	static Lifecycle getLifecycle() {
		return TransactionManagerImpl.MANAGERS.get().getLifecycle();
	}

	/**
	 * Open a nested transaction using the provided parent if not {@code null}, otherwise open an outer transaction.
	 *
	 * @param maybeParent an optional parent transaction context
	 * @return an opened {@link Transaction}
	 */
	static Transaction openNested(TransactionContext maybeParent) {
		return maybeParent == null ? openOuter() : maybeParent.openNested();
	}

	/**
	 * Unsafe access to the current transaction context for legacy compatibility.
	 *
	 * @return the current {@link TransactionContext} for the thread-local manager, or {@code null}
	 * @deprecated prefer passing {@link TransactionContext} explicitly to APIs
	 */
	@Deprecated
	static TransactionContext getCurrentUnsafe() {
		return TransactionManagerImpl.MANAGERS.get().getCurrentUnsafe();
	}

	/**
	 * Abort the transaction. All changes since the transaction was opened will be discarded.
	 */
	void abort();

	/**
	 * Commit the transaction. All changes since the transaction was opened will become permanent.
	 */
	void commit();

	/**
	 * Close the transaction. Implementations should ensure this behaves like either {@link #abort} or {@link #commit}
	 * depending on the transaction's state.
	 */
	@Override
	void close();

	/**
	 * The lifecycle states that a transaction manager may be in.
	 */
	enum Lifecycle {
		/** No transaction is open. */
		NONE,
		/** A transaction is currently open. */
		OPEN,
		/** A transaction is in the process of closing. */
		CLOSING,
		/** The outer (top-level) transaction is in the process of closing. */
		OUTER_CLOSING
	}
}
