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

package com.shailist.TransferAPI.api.transfer.v1.transaction.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.shailist.TransferAPI.api.transfer.v1.transaction.Transaction;
import com.shailist.TransferAPI.api.transfer.v1.transaction.TransactionContext;

/**
 * A helper base class for participants that need to take, update and restore a snapshot when transactions run.
 *
 * <p>Implementations provide a snapshot representation of their mutable state via {@link #createSnapshot} and
 * restore previous snapshots with {@link #readSnapshot}. Snapshots are automatically created for each nesting depth
 * the first time a participant is modified within that depth.
 *
 * @param <T> the snapshot type
 */
public abstract class SnapshotParticipant<T> implements Transaction.CloseCallback, Transaction.OuterCloseCallback {
	private final List<T> snapshots = new ArrayList<>();

	/**
	 * Create a new SnapshotParticipant. Subclasses should initialize their state in their own constructors.
	 */
	protected SnapshotParticipant() {
	}

	/**
	 * Create a fresh snapshot representing the participant's current state.
	 *
	 * @return a non-null snapshot instance
	 */
	protected abstract T createSnapshot();

	/**
	 * Restore the participant's state from the provided snapshot.
	 *
	 * @param snapshot the snapshot to restore from
	 */
	protected abstract void readSnapshot(T snapshot);

	/**
	 * Release resources associated with a snapshot when it is no longer needed.
	 *
	 * @param snapshot the snapshot to release
	 */
	protected void releaseSnapshot(T snapshot) {
	}

	/**
	 * Called after the outermost transaction commits; default implementation does nothing.
	 * Subclasses may override to perform finalization work.
	 */
	protected void onFinalCommit() {
	}

	/**
	 * Ensure a snapshot exists for the current transaction nesting depth and register this participant
	 * to be notified when the transaction closes.
	 *
	 * @param transaction the current transaction context
	 */
	public void updateSnapshots(TransactionContext transaction) {
		while (snapshots.size() <= transaction.nestingDepth()) {
			snapshots.add(null);
		}

		if (snapshots.get(transaction.nestingDepth()) == null) {
			T snapshot = createSnapshot();
			Objects.requireNonNull(snapshot, "Snapshot may not be null!");

			snapshots.set(transaction.nestingDepth(), snapshot);
			transaction.addCloseCallback(this);
		}
	}

	@Override
	public void onClose(TransactionContext transaction, Transaction.Result result) {
		T snapshot = snapshots.set(transaction.nestingDepth(), null);

		if (result.wasAborted()) {
			readSnapshot(snapshot);
			releaseSnapshot(snapshot);
		} else if (transaction.nestingDepth() > 0) {
			if (snapshots.get(transaction.nestingDepth() - 1) == null) {
				snapshots.set(transaction.nestingDepth() - 1, snapshot);
				transaction.getOpenTransaction(transaction.nestingDepth() - 1).addCloseCallback(this);
			} else {
				releaseSnapshot(snapshot);
			}
		} else {
			releaseSnapshot(snapshot);
			transaction.addOuterCloseCallback(this);
		}
	}

	@Override
	public void afterOuterClose(Transaction.Result result) {
		onFinalCommit();
	}
}
