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

package com.shailist.hytale.api.transfer.v1.storage;

/**
 * An immutable association of an immutable object instance (for example {@code Item} or {@code Fluid}) and data components.
 *
 * <p>This is exposed for convenience for code that needs to be generic across multiple transfer variants,
 * but note that a {@link Storage} is not necessarily bound to {@code TransferVariant}. Its generic parameter can be any immutable object.
 *
 * <p><b>Transfer variants must always be compared with {@code equals}, never by reference!</b>
 * {@code hashCode} is guaranteed to be correct and constant time independently of the size of the components.
 *
 * @param <O> The type of the immutable object instance, for example {@code Item} or {@code Fluid}.
 */
public interface TransferVariant<O> {
    /**
     * Return true if this variant is blank, and false otherwise.
     *
     * @return true if this variant is blank, false otherwise.
     */
    boolean isBlank();

    /**
     * Return the immutable object instance of this variant.
     *
     * @return The immutable object instance of this variant.
     */
    O getObject();

//     TODO: Data Components
//     /**
//      * @return The {@link DataComponentPatch} of this variant.
//      */
//     DataComponentPatch getComponents();
//
//     /**
//      * @return The {@link DataComponentMap} of this variant.
//      */
//     DataComponentMap getComponentMap();
//
//     /**
//      * Return true if this variant has a component changes.
//      */
//     default boolean hasComponents() {
//         return !getComponents().isEmpty();
//     }
//
//     /**
//      * Return true if the tag of this variant matches the passed tag, and false otherwise.
//      *
//      * <p>Note: True is returned if both tags are {@code null}.
//      */
//     default boolean componentsMatch(DataComponentPatch other) {
//         return Objects.equals(getComponents(), other);
//     }

    /**
     * Return {@code true} if the object of this variant matches the passed resource.
     *
     * @param object The object to compare against.
     * @return {@code true} if the object of this variant matches the passed resource, false otherwise.
     */
    default boolean isOf(O object) {
        return getObject() == object;
    }

//     TODO: Data Components
//     /**
//      * Creates a copy of this TransferVariant with the provided component changes applied.
//      * @param changes the changes to apply
//      * @return the new variant with the changes applied
//      */
//     default TransferVariant<O> withComponentChanges(DataComponentPatch changes) {
//         throw new UnsupportedOperationException("withComponentChanges is not supported by this TransferVariant");
//     }
}
