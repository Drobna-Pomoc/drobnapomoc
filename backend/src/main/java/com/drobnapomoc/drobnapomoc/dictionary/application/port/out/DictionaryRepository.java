package com.drobnapomoc.drobnapomoc.dictionary.application.port.out;

import com.drobnapomoc.drobnapomoc.dictionary.domain.City;
import com.drobnapomoc.drobnapomoc.shared.domain.ServiceCategory;

import java.util.List;
import java.util.Optional;

public interface DictionaryRepository {

    List<City> findAllCities();

    List<ServiceCategory> findActiveCategories();

    Optional<ServiceCategory> findActiveCategory(long categoryId);

    boolean districtExists(long districtId);
}
