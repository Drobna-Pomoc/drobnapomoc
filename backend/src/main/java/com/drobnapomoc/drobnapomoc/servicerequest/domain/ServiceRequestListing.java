package com.drobnapomoc.drobnapomoc.servicerequest.domain;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Objects;

/**
 * Open service request as contractors see it: no personal data of the client.
 */
public record ServiceRequestListing(
        long id,
        ServiceRequestStatus status,
        long categoryId,
        String description,
        @Nullable Instant preferredFrom,
        @Nullable Instant preferredTo,
        long districtId,
        @Nullable Long estimateMinMinor,
        @Nullable Long estimateMaxMinor,
        Instant expiresAt,
        Instant createdAt
) {

    public ServiceRequestListing {
        Objects.requireNonNull(status, "Status cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");
        Objects.requireNonNull(expiresAt, "ExpiresAt cannot be null");
        Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
    }
}
