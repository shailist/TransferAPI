package com.shailist.hytale.test.transfer.unittests.utils;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class StringVariantImpl implements StringVariant {
    public static StringVariant of(@Nullable String string) {
        if (string != null && string.isBlank()) {
            string = null;
        }

        return new StringVariantImpl(string);
    }

    private final @Nullable String string;
    private final int hashCode;

    public StringVariantImpl(@Nullable String string) {
        this.string = string;
        this.hashCode = string == null ? 0 : Objects.hash(string);
    }

    @Override
    public boolean isBlank() {
        return string == null;
    }

    @Override
    public @Nullable String getObject() {
        return string;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        var stringVariant = (StringVariantImpl)obj;
        // Fail fast with hash code
        return hashCode == stringVariant.hashCode && Objects.equals(string, stringVariant.string);
    }

    @Override
    public String toString() {
        return "StringVariant{string=" + string + '}';
    }
}
