package com.shailist.hytale.test.transfer.unittests.utils;

import com.shailist.hytale.api.transfer.v1.storage.TransferVariant;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * An immutable association of a string.
 *
 * <p>Do not extend this class. Use {@link #of(String)} to create instances.
 *
 * <p><b>String variants must always be compared with {@code equals}, never by reference!</b>
 * {@code hashCode} is guaranteed to be correct and constant time independently of the size of the components.
 */
@ApiStatus.NonExtendable
public interface StringVariant extends TransferVariant<@Nullable String> {
    /**
     * Retrieve a blank StringVariant.
     */
    static StringVariant blank() {
        return of(null);
    }

    /**
     * Retrieve a StringVariant with a string.
     */
    static StringVariant of(@Nullable String string) {
        return StringVariantImpl.of(string);
    }

    /**
     * Return the string of this variant.
     */
    default @Nullable String getString() {
        return getObject();
    }
}
