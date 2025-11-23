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

package com.shailist.hytale.test.transfer.unittests;

import java.util.Iterator;

import com.shailist.hytale.test.transfer.unittests.utils.SingleStringStorage;
import com.shailist.hytale.test.transfer.unittests.utils.StringConstants;
import com.shailist.hytale.test.transfer.unittests.utils.StringVariant;
import org.junit.jupiter.api.Test;

import com.shailist.hytale.api.transfer.v1.storage.Storage;
import com.shailist.hytale.api.transfer.v1.storage.StorageUtil;
import com.shailist.hytale.api.transfer.v1.storage.StorageView;
import com.shailist.hytale.api.transfer.v1.storage.base.FilteringStorage;
import com.shailist.hytale.api.transfer.v1.storage.base.SingleVariantStorage;
import com.shailist.hytale.api.transfer.v1.transaction.Transaction;

import static org.junit.jupiter.api.Assertions.*;

public class BaseStorageTests {
	@Test
	public void testFilteringStorage() {
		SingleVariantStorage<StringVariant> storage = new SingleVariantStorage<>() {
			@Override
			protected StringVariant getBlankVariant() {
				return StringVariant.blank();
			}

			@Override
			protected long getCapacity(StringVariant variant) {
				return 10 * StringConstants.UNIT;
			}
		};
		Storage<StringVariant> noHello = new FilteringStorage<>(storage) {
			@Override
			protected boolean canExtract(StringVariant resource) {
				return !resource.isOf(StringConstants.HELLO);
			}

			@Override
			protected boolean canInsert(StringVariant resource) {
				return !resource.isOf(StringConstants.HELLO);
			}
		};
		StringVariant hello = StringVariant.of(StringConstants.HELLO);
		StringVariant world = StringVariant.of(StringConstants.WORLD);

		// Insertion into the backing storage should succeed.
		try (Transaction tx = Transaction.openOuter()) {
			assertEquals(StringConstants.UNIT, storage.insert(hello, StringConstants.UNIT, tx));
			tx.commit();
		}

		// Insertion through the filter should fail.
		assertEquals(0L, StorageUtil.simulateInsert(noHello, hello, StringConstants.UNIT, null));
		// Extraction should also fail.
		assertEquals(0L, StorageUtil.simulateExtract(noHello, hello, StringConstants.UNIT, null));
		// The fluid should be visible.
		assertEquals(hello, StorageUtil.findStoredResource(noHello));
		// Test the filter.
        assertNull(StorageUtil.findStoredResource(noHello, fv -> fv.isOf(StringConstants.WORLD)));
		// But it can't be extracted, even through a storage view.
        assertNull(StorageUtil.findExtractableResource(noHello, null));
        assertNull(StorageUtil.findExtractableContent(noHello, null));

		storage.amount = 0;
		storage.variant = StringVariant.blank();

		// Lava insertion and extract should proceed just fine.
		try (Transaction tx = Transaction.openOuter()) {
			assertEquals(StringConstants.UNIT, noHello.insert(world, StringConstants.UNIT, tx));
			assertEquals(StringConstants.UNIT, StorageUtil.simulateExtract(noHello, world, StringConstants.UNIT, tx));
			// Test that simulating doesn't change the state...
			assertEquals(StringConstants.UNIT, StorageUtil.simulateExtract(noHello, world, StringConstants.UNIT, tx));
			assertEquals(StringConstants.UNIT, StorageUtil.simulateExtract(noHello, world, StringConstants.UNIT, tx));
			tx.commit();
		}

		assertEquals(StringConstants.UNIT, StorageUtil.simulateExtract(storage, world, StringConstants.UNIT, null));
	}

	/**
	 * Regression test for <a href="https://github.com/FabricMC/fabric/issues/3414">
	 * {@code nonEmptyIterator} not handling views that become empty during iteration correctly</a>.
	 */
	@Test
	public void testNonEmptyIteratorWithModifiedView() {
		SingleVariantStorage<StringVariant> storage = SingleStringStorage.withFixedCapacity(StringConstants.UNIT, () -> { });
		storage.variant = StringVariant.of(StringConstants.HELLO);

		Iterator<StorageView<StringVariant>> iterator = storage.nonEmptyIterator();
		storage.amount = StringConstants.UNIT;
		// Iterator should have a next element now
        assertTrue(iterator.hasNext());
		assertEquals(storage, iterator.next());

		iterator = storage.nonEmptyIterator();
		storage.amount = 0;
		// Iterator should not have a next element...
        assertFalse(iterator.hasNext());
	}
}
