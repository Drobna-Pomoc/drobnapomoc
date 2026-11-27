package com.drobnapomoc.drobnapomoc.shared.mapping;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Conversions reused by mappers across modules: MapStruct mappers ({@code @Mapper(uses = MappingTool.class)}),
 * JDBC row mappers and repositories. Missing values stay missing: {@code null} in gives {@code null} out.
 */
public final class MappingTool {

    private MappingTool() {
    }

    /**
     * Converts a date-time from the API models or the database ({@code timestamptz}) into the {@link Instant}
     * the domain uses. The offset is dropped; the moment in time stays the same.
     */
    public static @Nullable Instant toInstant(final @Nullable OffsetDateTime dateTime) {
        return dateTime != null ? dateTime.toInstant() : null;
    }

    /**
     * Converts a domain {@link Instant} into the {@link OffsetDateTime} the API models and the PostgreSQL driver
     * expect. Always uses UTC, so responses and stored values share one offset.
     */
    public static @Nullable OffsetDateTime toOffsetDateTime(final @Nullable Instant instant) {
        return instant != null ? instant.atOffset(ZoneOffset.UTC) : null;
    }
}
