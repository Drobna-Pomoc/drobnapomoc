package com.drobnapomoc.drobnapomoc.dictionary.adapter.out.persistence;

import com.drobnapomoc.drobnapomoc.dictionary.application.port.out.DictionaryRepository;
import com.drobnapomoc.drobnapomoc.dictionary.domain.City;
import com.drobnapomoc.drobnapomoc.dictionary.domain.District;
import com.drobnapomoc.drobnapomoc.dictionary.domain.ServiceCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
class JdbcDictionaryRepository implements DictionaryRepository {

    private static final String SELECT_CITIES_SQL = "SELECT id, name FROM city ORDER BY name, id";

    private static final String SELECT_DISTRICTS_SQL = "SELECT id, city_id, name FROM district ORDER BY name, id";

    private static final String SELECT_ACTIVE_CATEGORIES_SQL =
            "SELECT id, name FROM service_category WHERE active ORDER BY name, id";

    private static final String ACTIVE_CATEGORY_EXISTS_SQL =
            "SELECT EXISTS(SELECT 1 FROM service_category WHERE id = :id AND active)";

    private static final String DISTRICT_EXISTS_SQL = "SELECT EXISTS(SELECT 1 FROM district WHERE id = :id)";

    private final JdbcClient jdbcClient;
    private final CityRowMapper cityRowMapper;
    private final DistrictRowMapper districtRowMapper;
    private final ServiceCategoryRowMapper serviceCategoryRowMapper;

    @Override
    public List<City> findAllCities() {
        final var districtsByCity = jdbcClient.sql(SELECT_DISTRICTS_SQL)
                .query(districtRowMapper)
                .list()
                .stream()
                .collect(Collectors.groupingBy(District::cityId));

        return jdbcClient.sql(SELECT_CITIES_SQL)
                .query(cityRowMapper)
                .list()
                .stream()
                .map(city -> city.withDistricts(districtsByCity.getOrDefault(city.id(), List.of())))
                .toList();
    }

    @Override
    public List<ServiceCategory> findActiveCategories() {
        return jdbcClient.sql(SELECT_ACTIVE_CATEGORIES_SQL)
                .query(serviceCategoryRowMapper)
                .list();
    }

    @Override
    public boolean isCategoryActive(final long categoryId) {
        return jdbcClient.sql(ACTIVE_CATEGORY_EXISTS_SQL)
                .param("id", categoryId)
                .query(Boolean.class)
                .single();
    }

    @Override
    public boolean districtExists(final long districtId) {
        return jdbcClient.sql(DISTRICT_EXISTS_SQL)
                .param("id", districtId)
                .query(Boolean.class)
                .single();
    }
}
