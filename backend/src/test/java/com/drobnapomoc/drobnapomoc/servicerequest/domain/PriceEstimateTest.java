package com.drobnapomoc.drobnapomoc.servicerequest.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriceEstimateTest {

    @Test
    void testPriceEstimate_givenValidRange_shouldKeepIt() {
        assertThat(new PriceEstimate(8000L, 15000L).maxMinor()).isEqualTo(15000L);
    }

    @Test
    void testPriceEstimate_givenZeroMinimum_shouldThrow() {
        assertThatThrownBy(() -> new PriceEstimate(0L, 15000L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testPriceEstimate_givenMaximumBelowMinimum_shouldThrow() {
        assertThatThrownBy(() -> new PriceEstimate(15000L, 8000L)).isInstanceOf(IllegalArgumentException.class);
    }
}
