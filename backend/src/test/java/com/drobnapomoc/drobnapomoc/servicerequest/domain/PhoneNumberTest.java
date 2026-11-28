package com.drobnapomoc.drobnapomoc.servicerequest.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PhoneNumberTest {

    @ParameterizedTest
    @CsvSource({
            "+48600123456, +48600123456",
            "600123456, +48600123456",
            "' 600123456 ', +48600123456"
    })
    void testNormalize_givenPolishNumber_shouldReturnItWithPlus48Prefix(final String raw, final String expected) {
        // when
        final var normalized = PhoneNumber.normalize(raw);

        // then
        assertThat(normalized).isEqualTo(expected);
    }
}
