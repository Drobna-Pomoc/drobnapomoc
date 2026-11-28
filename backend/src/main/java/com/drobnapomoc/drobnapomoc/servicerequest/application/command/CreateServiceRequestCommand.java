package com.drobnapomoc.drobnapomoc.servicerequest.application.command;

import com.drobnapomoc.drobnapomoc.servicerequest.domain.NotifyTarget;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Objects;

public record CreateServiceRequestCommand(
        ClientCommand client,
        long categoryId,
        String description,
        @Nullable Instant preferredFrom,
        @Nullable Instant preferredTo,
        @Nullable NotifyTarget notifyTarget
) {

    public CreateServiceRequestCommand {
        Objects.requireNonNull(client, "Client cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");
    }
}
