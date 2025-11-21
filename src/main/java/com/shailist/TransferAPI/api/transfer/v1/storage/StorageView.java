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

import com.shailist.TransferAPI.api.transfer.v1.transaction.TransactionContext;

/**
 * A view of a single stored resource in a {@link Storage}, for use with {@link Storage#iterator}.
 *
 * @param <T> The type of the stored resource.
 */
public interface StorageView<T> {
	/**
	 * Extract up to {@code maxAmount} of {@code resource} from this view.
	 *
	 * <p>This call will attempt to remove at most {@code maxAmount} units of the provided {@code resource}
	 * from the underlying storage represented by this view. The operation participates in the provided
	 * {@link TransactionContext} so it can be rolled back if the transaction aborts.
	 *
	 * @param resource the resource to extract, must not be blank
	 * @param maxAmount the maximum amount to extract, must be non-negative
	 * @param transaction the transaction context to associate with the extraction
	 * @return the amount actually extracted, non-negative and not greater than {@code maxAmount}
	 */
	long extract(T resource, long maxAmount, TransactionContext transaction);

	/**
	 * Return {@code true} if the {@link #getResource} contained in this storage view is blank, or {@code false} otherwise.
	 *
	 * <p>This function is mostly useful when dealing with storages of arbitrary types.
	 * For transfer variant storages, this should always be equivalent to {@code getResource().isBlank()}.
	 */
	/**
	 * Return {@code true} if the view contains no resource.
	 *
	 * @return {@code true} when the resource is blank
	 */
	boolean isResourceBlank();

	/**
	 * Return the resource stored in this view.
	 *
	 * @return the stored resource. If {@link #isResourceBlank()} is {@code false} this value will not be blank.
	 */
	T getResource();

	/**
	 * Return the amount of the stored resource in this view.
	 *
	 * @return the amount of {@link #getResource()} currently stored in this view, as a non-negative long
	 */
	long getAmount();

	/**
	 * Return the capacity of this view for the current resource type.
	 *
	 * @return the maximum amount of {@link #getResource()} that could be stored in this view, or an estimated
	 * upper bound if the view is currently blank
	 */
	long getCapacity();

	/**
	 * If this is view is a delegate around another storage view, return the underlying view.
	 * This can be used to check if two views refer to the same inventory "slot".
	 * <b>Do not try to extract from the underlying view, or you risk bypassing some checks.</b>
	 *
	 * <p>It is expected that two storage views with the same underlying view ({@code a.getUnderlyingView() == b.getUnderlyingView()})
	 * share the same content, and mutating one should mutate the other. However, one of them may allow extraction, and the other may not.
	 */
	/**
	 * Return the underlying view if this view delegates to another view.
	 *
	 * @return the underlying storage view if this view is a delegate, otherwise {@code this}
	 */
	default StorageView<T> getUnderlyingView() {
		return this;
	}
}
