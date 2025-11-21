package com.shailist.TransferAPI;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class placeholderTest {

    @Test
    void runDemoDoesNotThrow() {
        assertDoesNotThrow(() -> {
            placeholder ph = new placeholder();
        });
    }
}
