package com.drobnapomoc.drobnapomoc.dictionary.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CityTest {

    @Test
    void testWithDistricts_givenDistricts_shouldReturnCopyWithThem() {
        // given
        final var city = new City(1L, "Lublin", List.of());
        final var districts = List.of(new District(7L, 1L, "Czechów"));

        // when
        final var withDistricts = city.withDistricts(districts);

        // then
        assertThat(withDistricts).isEqualTo(new City(1L, "Lublin", districts));
        assertThat(city.districts()).isEmpty();
    }
}
