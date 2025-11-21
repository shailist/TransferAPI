package com.shailist.TransferAPI.api.transfer.v1.storage.base;

/**
 * A small tuple describing a resource together with an amount.
 *
 * @param <T> the resource type
 * @param resource the resource instance
 * @param amount the amount of the resource
 */
public record ResourceAmount<T>(T resource, long amount) {
}
