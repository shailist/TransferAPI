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

/**
 * Small helpers for checking method preconditions used by storage implementations.
 */
public final class StoragePreconditions {
	/**
	 * Ensure that the provided amount is not negative.
	 *
	 * @param amount the amount to validate
	 * @throws IllegalArgumentException if {@code amount} is negative
	 */
	public static void notNegative(long amount) {
		if (amount < 0) {
			throw new IllegalArgumentException("Amount may not be negative, but it is: " + amount);
		}
	}

	private StoragePreconditions() {}
}
