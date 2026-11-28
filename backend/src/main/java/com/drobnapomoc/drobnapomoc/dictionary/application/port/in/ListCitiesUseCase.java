package com.drobnapomoc.drobnapomoc.dictionary.application.port.in;

import com.drobnapomoc.drobnapomoc.dictionary.domain.City;

import java.util.List;

public interface ListCitiesUseCase {

    List<City> listCities();
}
