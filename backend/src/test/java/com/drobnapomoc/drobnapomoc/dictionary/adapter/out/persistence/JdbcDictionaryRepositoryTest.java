package com.drobnapomoc.drobnapomoc.dictionary.adapter.out.persistence;

import com.drobnapomoc.drobnapomoc.RepositoryTest;
import com.drobnapomoc.drobnapomoc.dictionary.domain.City;
import com.drobnapomoc.drobnapomoc.dictionary.domain.District;
import com.drobnapomoc.drobnapomoc.shared.domain.ServiceCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RepositoryTest
@Import({
        JdbcDictionaryRepository.class,
        CityRowMapper.class,
        DistrictRowMapper.class,
        ServiceCategoryRowMapper.class
})
class JdbcDictionaryRepositoryTest {

    @Autowired
    private JdbcDictionaryRepository repository;

    @Test
    void testFindAllCities_givenSeededCities_shouldReturnThemSortedByNameWithDistricts() {
        // when
        final var cities = repository.findAllCities();

        // then
        assertThat(cities).containsExactly(
                new City(2L, "Chełm", List.of(new District(3L, 2L, "Centrum"))),
                new City(1L, "Lublin", List.of(
                        new District(2L, 1L, "Czechów"),
                        new District(1L, 1L, "Wieniawa")
                )),
                new City(3L, "Zamość", List.of())
        );
    }

    @Test
    void testFindActiveCategories_givenActiveAndInactiveCategories_shouldReturnOnlyActiveSortedByName() {
        // when
        final var categories = repository.findActiveCategories();

        // then
        assertThat(categories).containsExactly(
                new ServiceCategory(2L, "Elektryk"),
                new ServiceCategory(1L, "Hydraulik")
        );
    }

    @Test
    void testFindActiveCategory_givenActiveCategory_shouldReturnIt() {
        assertThat(repository.findActiveCategory(1L)).contains(new ServiceCategory(1L, "Hydraulik"));
    }

    @Test
    void testFindActiveCategory_givenInactiveCategory_shouldReturnEmpty() {
        assertThat(repository.findActiveCategory(3L)).isEmpty();
    }

    @Test
    void testFindActiveCategory_givenMissingCategory_shouldReturnEmpty() {
        assertThat(repository.findActiveCategory(999_999L)).isEmpty();
    }

    @Test
    void testDistrictExists_givenExistingDistrict_shouldReturnTrue() {
        assertThat(repository.districtExists(1L)).isTrue();
    }

    @Test
    void testDistrictExists_givenMissingDistrict_shouldReturnFalse() {
        assertThat(repository.districtExists(999_999L)).isFalse();
    }
}
