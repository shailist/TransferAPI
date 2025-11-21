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

import java.util.List;

import com.shailist.TransferAPI.api.transfer.v1.storage.base.SingleSlotStorage;
import com.shailist.TransferAPI.impl.transfer.TransferApiImpl;

/**
 * A storage with indexed slots.
 *
 * @param <T> resource type
 */
public interface SlottedStorage<T> extends Storage<T> {
    /**
     * Return the number of slots in this storage.
     *
     * @return slot count
     */
    int getSlotCount();

    /**
     * Return the slot at the given index.
     *
     * @param slot index of the slot
     * @return the slot view
     */
    SingleSlotStorage<T> getSlot(int slot);

    /**
     * Convenience: return a list view over slots.
     *
     * @return list of slot views
     */
    default List<SingleSlotStorage<T>> getSlots() {
        return TransferApiImpl.makeListView(this);
    }
}
