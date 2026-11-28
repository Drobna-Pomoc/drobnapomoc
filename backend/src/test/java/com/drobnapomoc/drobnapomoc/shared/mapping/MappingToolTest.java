package com.drobnapomoc.drobnapomoc.shared.mapping;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class MappingToolTest {

    private static final Instant MOMENT = Instant.parse("2026-12-01T08:00:00Z");

    @Test
    void testToInstant_givenDateTimeWithOffset_shouldReturnSameMoment() {
        // given
        final var warsawTime = OffsetDateTime.of(2026, 12, 1, 9, 0, 0, 0, ZoneOffset.ofHours(1));

        // when & then
        assertThat(MappingTool.toInstant(warsawTime)).isEqualTo(MOMENT);
    }

    @Test
    void testToInstant_givenNoValue_shouldReturnNull() {
        assertThat(MappingTool.toInstant(null)).isNull();
    }

    @Test
    void testToOffsetDateTime_givenInstant_shouldReturnSameMomentInUtc() {
        assertThat(MappingTool.toOffsetDateTime(MOMENT))
                .isEqualTo(OffsetDateTime.of(2026, 12, 1, 8, 0, 0, 0, ZoneOffset.UTC));
    }

    @Test
    void testToOffsetDateTime_givenNoValue_shouldReturnNull() {
        assertThat(MappingTool.toOffsetDateTime(null)).isNull();
    }
}
