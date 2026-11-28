package com.drobnapomoc.drobnapomoc.servicerequest.domain;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Objects;

public record ServiceRequest(
        long id,
        ServiceRequestStatus status,
        RequestChannel channel,
        long categoryId,
        String description,
        @Nullable Instant preferredFrom,
        @Nullable Instant preferredTo,
        NotifyTarget notifyTarget,
        ServiceRequestClient client,
        @Nullable Long estimateMinMinor,
        @Nullable Long estimateMaxMinor,
        @Nullable Instant estimatedAt,
        Instant expiresAt,
        @Nullable Long createdBy,
        @Nullable String cancelReason,
        Instant createdAt,
        Instant updatedAt
) {

    public ServiceRequest {
        Objects.requireNonNull(status, "Status cannot be null");
        Objects.requireNonNull(channel, "Channel cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");
        Objects.requireNonNull(notifyTarget, "NotifyTarget cannot be null");
        Objects.requireNonNull(client, "Client cannot be null");
        Objects.requireNonNull(expiresAt, "ExpiresAt cannot be null");
        Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        Objects.requireNonNull(updatedAt, "UpdatedAt cannot be null");
    }
}
