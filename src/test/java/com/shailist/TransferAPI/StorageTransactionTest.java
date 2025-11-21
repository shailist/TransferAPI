package com.shailist.TransferAPI;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.shailist.TransferAPI.api.transfer.v1.storage.Storage;
import com.shailist.TransferAPI.api.transfer.v1.storage.StorageView;
import com.shailist.TransferAPI.api.transfer.v1.transaction.Transaction;
import com.shailist.TransferAPI.api.transfer.v1.transaction.TransactionContext;

import java.util.Iterator;
 

public class StorageTransactionTest {

    static class SimpleStorage implements Storage<String> {
        private String content = "";
        private long amount = 0;

        @Override
        public boolean supportsInsertion() { return true; }

        @Override
        public long insert(String resource, long maxAmount, TransactionContext transaction) {
            long toInsert = Math.min(maxAmount, 1);
            if (toInsert <= 0) return 0;
            content = resource;
            amount = toInsert;
            return toInsert;
        }

        @Override
        public boolean supportsExtraction() { return true; }

        @Override
        public long extract(String resource, long maxAmount, TransactionContext transaction) {
            if (!resource.equals(content) || amount == 0) return 0;
            long taken = Math.min(maxAmount, amount);
            amount -= taken;
            if (amount == 0) content = "";
            return taken;
        }

        @Override
        public Iterator<StorageView<String>> iterator() {
            return new Iterator<>() {
                boolean seen = false;

                @Override
                public boolean hasNext() { return !seen && amount > 0; }

                @Override
                public StorageView<String> next() {
                    seen = true;
                    return new StorageView<String>() {
                        @Override public long extract(String resource, long maxAmount, TransactionContext transaction) { return SimpleStorage.this.extract(resource, maxAmount, transaction); }
                        @Override public boolean isResourceBlank() { return content.isEmpty(); }
                        @Override public String getResource() { return content; }
                        @Override public long getAmount() { return amount; }
                        @Override public long getCapacity() { return 1; }
                        @Override public StorageView<String> getUnderlyingView() { return this; }
                    };
                }
            };
        }

        @Override
        public long getVersion() { return Transaction.isOpen() ? 0 : 0; }
    }

    @Test
    public void testInsertExtractWithTransaction() {
        SimpleStorage storage = new SimpleStorage();

        Transaction outer = Transaction.openOuter();
        assertNotNull(outer);

        // Use the returned Transaction as the TransactionContext
        TransactionContext ctx = outer;

        long inserted = storage.insert("apple", 1, ctx);
        assertEquals(1, inserted);

        long extracted = storage.extract("apple", 1, ctx);
        assertEquals(1, extracted);

        outer.commit();
    }
}
