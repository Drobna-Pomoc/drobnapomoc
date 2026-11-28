package com.drobnapomoc.drobnapomoc.dictionary.adapter.in.web;

import com.drobnapomoc.drobnapomoc.api.model.CityRest;
import com.drobnapomoc.drobnapomoc.api.model.DistrictRest;
import com.drobnapomoc.drobnapomoc.api.model.ServiceCategoryRest;
import com.drobnapomoc.drobnapomoc.dictionary.domain.City;
import com.drobnapomoc.drobnapomoc.dictionary.domain.District;
import com.drobnapomoc.drobnapomoc.shared.domain.ServiceCategory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DictionaryWebMapperTest {

    private final DictionaryWebMapper mapper = new DictionaryWebMapperImpl();

    @Test
    void testToRestCities_givenCitiesWithDistricts_shouldMapAllFields() {
        // given
        final var cities = List.of(
                new City(1L, "Lublin", List.of(
                        new District(2L, 1L, "Czechów"),
                        new District(1L, 1L, "Wieniawa")
                )),
                new City(3L, "Zamość", List.of())
        );

        // when
        final var rest = mapper.toRestCities(cities);

        // then
        assertThat(rest).containsExactly(
                new CityRest(1L, "Lublin", List.of(
                        new DistrictRest(2L, "Czechów"),
                        new DistrictRest(1L, "Wieniawa")
                )),
                new CityRest(3L, "Zamość", List.of())
        );
    }

    @Test
    void testToRestCities_givenEmptyList_shouldReturnEmptyList() {
        // when
        final var cities = mapper.toRestCities(List.of());

        // then
        assertThat(cities).isEmpty();
    }

    @Test
    void testToRestCategories_givenEmptyList_shouldReturnEmptyList() {
        // when
        final var categories = mapper.toRestCategories(List.of());

        // then
        assertThat(categories).isEmpty();
    }

    @Test
    void testToRestCategories_givenCategories_shouldMapAllFields() {
        // given
        final var categories = List.of(new ServiceCategory(2L, "Elektryk"), new ServiceCategory(1L, "Hydraulik"));

        // when
        final var rest = mapper.toRestCategories(categories);

        // then
        assertThat(rest).containsExactly(
                new ServiceCategoryRest(2L, "Elektryk"),
                new ServiceCategoryRest(1L, "Hydraulik")
        );
    }
}
