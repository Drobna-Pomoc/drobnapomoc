package com.drobnapomoc.drobnapomoc.dictionary.application.service;

import com.drobnapomoc.drobnapomoc.dictionary.application.port.out.DictionaryRepository;
import com.drobnapomoc.drobnapomoc.dictionary.domain.City;
import com.drobnapomoc.drobnapomoc.dictionary.domain.District;
import com.drobnapomoc.drobnapomoc.shared.domain.ServiceCategory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DictionaryServiceTest {

    @Mock
    private DictionaryRepository dictionaryRepository;

    @InjectMocks
    private DictionaryService dictionaryService;

    @Test
    void testListCities_givenCitiesInRepository_shouldReturnThem() {
        // given
        final var cities = List.of(new City(1L, "Lublin", List.of(new District(7L, 1L, "Czechów"))));
        given(dictionaryRepository.findAllCities()).willReturn(cities);

        // when
        final var result = dictionaryService.listCities();

        // then
        assertThat(result).isEqualTo(cities);
    }

    @Test
    void testListServiceCategories_givenCategoriesInRepository_shouldReturnThem() {
        // given
        final var categories = List.of(new ServiceCategory(3L, "Hydraulik"));
        given(dictionaryRepository.findActiveCategories()).willReturn(categories);

        // when
        final var result = dictionaryService.listServiceCategories();

        // then
        assertThat(result).isEqualTo(categories);
    }

    @Test
    void testFindActiveCategory_givenRepositoryAnswer_shouldReturnIt() {
        // given
        given(dictionaryRepository.findActiveCategory(3L)).willReturn(Optional.of(new ServiceCategory(3L, "Hydraulik")));

        // when & then
        assertThat(dictionaryService.findActiveCategory(3L)).contains(new ServiceCategory(3L, "Hydraulik"));
    }

    @Test
    void testDistrictExists_givenRepositoryAnswer_shouldReturnIt() {
        // given
        given(dictionaryRepository.districtExists(7L)).willReturn(false);

        // when & then
        assertThat(dictionaryService.districtExists(7L)).isFalse();
    }
}
