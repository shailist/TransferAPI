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

package com.shailist.hytale.impl.transfer;

import java.util.AbstractList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shailist.hytale.api.transfer.v1.storage.SlottedStorage;
import com.shailist.hytale.api.transfer.v1.storage.Storage;
import com.shailist.hytale.api.transfer.v1.storage.StorageView;
import com.shailist.hytale.api.transfer.v1.storage.base.SingleSlotStorage;
import com.shailist.hytale.api.transfer.v1.transaction.TransactionContext;

/**
 * Implementation helpers for the transfer API.
 *
 * <p>This class contains internal utilities used by the implementation. It is not part of the public API
 * and is intended for internal use only.
 */
public class TransferApiImpl {
    /** Logger for transfer API implementation internals. */
    public static final Logger LOGGER = LoggerFactory.getLogger("transfer-api-v1");
    /** Monotonic version counter used by internal storages. */
    public static final AtomicLong version = new AtomicLong();

    /**
     * Internal empty storage instance used as a placeholder where no storage is available.
     */
    @SuppressWarnings("rawtypes")
    public static final Storage EMPTY_STORAGE = new Storage() {
        @Override
        public boolean supportsInsertion() {
            return false;
        }

        @Override
        public long insert(Object resource, long maxAmount, @NotNull TransactionContext transaction) {
            return 0;
        }

        @Override
        public boolean supportsExtraction() {
            return false;
        }

        @Override
        public long extract(Object resource, long maxAmount, @NotNull TransactionContext transaction) {
            return 0;
        }

        @Override
        public @NotNull Iterator<StorageView> iterator() {
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
     * Return an iterator that yields a single value.
     *
     * @param it the element to iterate over
     * @param <T> element type
     * @return an iterator over the single element
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
     * Create a view over a {@link SlottedStorage} as a {@link List} of {@link SingleSlotStorage}.
     *
     * @param storage the slotted storage to wrap
     * @param <T> the resource type
     * @return a list view of the storage slots
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

    /**
     * Private constructor to avoid instantiation of this utility class.
     */
    private TransferApiImpl() {
    }

//    TODO: Data Components
//    public static DataComponentPatch mergeChanges(DataComponentPatch base, DataComponentPatch applied) {
//        DataComponentPatch.Builder builder = DataComponentPatch.builder();
//
//        writeChangesTo(base, builder);
//        writeChangesTo(applied, builder);
//
//        return builder.build();
//    }
//
//    @SuppressWarnings("unchecked")
//    private static void writeChangesTo(DataComponentPatch changes, DataComponentPatch.Builder builder) {
//        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : changes.entrySet()) {
//            if (entry.getValue().isPresent()) {
//                builder.set((DataComponentType<Object>) entry.getKey(), entry.getValue().get());
//            } else {
//                builder.remove(entry.getKey());
//            }
//        }
//    }
}
