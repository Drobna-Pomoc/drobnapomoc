package com.drobnapomoc.drobnapomoc.dictionary.adapter.in.web;

import com.drobnapomoc.drobnapomoc.api.model.CityRest;
import com.drobnapomoc.drobnapomoc.api.model.DistrictRest;
import com.drobnapomoc.drobnapomoc.api.model.ServiceCategoryRest;
import com.drobnapomoc.drobnapomoc.dictionary.domain.City;
import com.drobnapomoc.drobnapomoc.dictionary.domain.District;
import com.drobnapomoc.drobnapomoc.shared.domain.ServiceCategory;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DictionaryWebMapper {

    CityRest toRest(City city);

    DistrictRest toRest(District district);

    ServiceCategoryRest toRest(ServiceCategory category);

    List<CityRest> toRestCities(List<City> cities);

    List<ServiceCategoryRest> toRestCategories(List<ServiceCategory> categories);
}
