package com.drobnapomoc.drobnapomoc.servicerequest.domain;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Objects;

/**
 * A validated service request with its client, ready to be stored: {@link ServiceRequestStatus#PRICED} with an
 * estimate, otherwise {@link ServiceRequestStatus#NEW}.
 */
public record NewServiceRequest(
        ServiceRequestClient client,
        long categoryId,
        String description,
        @Nullable Instant preferredFrom,
        @Nullable Instant preferredTo,
        NotifyTarget notifyTarget,
        RequestChannel channel,
        @Nullable Long createdBy,
        Instant expiresAt,
        @Nullable PriceEstimate estimate,
        @Nullable Instant estimatedAt
) {

    public NewServiceRequest {
        Objects.requireNonNull(client, "Client cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");
        Objects.requireNonNull(notifyTarget, "NotifyTarget cannot be null");
        Objects.requireNonNull(channel, "Channel cannot be null");
        Objects.requireNonNull(expiresAt, "ExpiresAt cannot be null");
        if ((estimate == null) != (estimatedAt == null)) {
            throw new IllegalArgumentException("Estimate and estimatedAt must be both set or both missing");
        }
    }

    public ServiceRequestStatus status() {
        return estimate != null ? ServiceRequestStatus.PRICED : ServiceRequestStatus.NEW;
    }
}
