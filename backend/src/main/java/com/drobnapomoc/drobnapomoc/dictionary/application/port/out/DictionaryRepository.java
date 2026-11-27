package com.drobnapomoc.drobnapomoc.dictionary.application.port.out;

import com.drobnapomoc.drobnapomoc.dictionary.domain.City;
import com.drobnapomoc.drobnapomoc.dictionary.domain.ServiceCategory;

import java.util.List;

public interface DictionaryRepository {

    List<City> findAllCities();

    List<ServiceCategory> findActiveCategories();

    boolean isCategoryActive(long categoryId);

    boolean districtExists(long districtId);
}
