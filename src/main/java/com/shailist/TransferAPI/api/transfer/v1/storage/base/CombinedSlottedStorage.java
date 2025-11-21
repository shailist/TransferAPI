package com.shailist.TransferAPI.api.transfer.v1.storage.base;

import java.util.List;
import java.util.StringJoiner;

import com.shailist.TransferAPI.api.transfer.v1.storage.SlottedStorage;
import com.shailist.TransferAPI.api.transfer.v1.storage.Storage;

/**
 * A {@link SlottedStorage} composed from multiple underlying slotted storages.
 *
 * <p>The slots of the component storages are concatenated, so slot indices are translated
 * into the appropriate component storage and slot within that storage.
 *
 * @param <T> the resource type
 * @param <S> the concrete slotted storage type for each part
 */
public class CombinedSlottedStorage<T, S extends SlottedStorage<T>> extends CombinedStorage<T, S> implements SlottedStorage<T> {
	/**
	 * Create a combined slotted storage from the provided parts.
	 *
	 * @param parts the list of slotted storages to combine
	 */
	public CombinedSlottedStorage(List<S> parts) {
		super(parts);
	}

	@Override
	public int getSlotCount() {
		int count = 0;

		for (S part : parts) {
			count += part.getSlotCount();
		}

		return count;
	}

	@Override
	public SingleSlotStorage<T> getSlot(int slot) {
		int updatedSlot = slot;

		for (SlottedStorage<T> part : parts) {
			if (updatedSlot < part.getSlotCount()) {
				return part.getSlot(updatedSlot);
			}

			updatedSlot -= part.getSlotCount();
		}

		throw new IndexOutOfBoundsException("Slot " + slot + " is out of bounds. This storage has size " + getSlotCount());
	}

	@Override
	public String toString() {
		StringJoiner partNames = new StringJoiner(", ");

		for (S part : parts) {
			partNames.add(part.toString());
		}

		return "CombinedSlottedStorage[" + partNames + "]";
	}
}
