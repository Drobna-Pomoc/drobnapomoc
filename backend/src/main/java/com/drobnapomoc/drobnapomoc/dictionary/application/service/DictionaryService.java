package com.drobnapomoc.drobnapomoc.dictionary.application.service;

import com.drobnapomoc.drobnapomoc.dictionary.application.port.in.ListCitiesUseCase;
import com.drobnapomoc.drobnapomoc.dictionary.application.port.in.ListServiceCategoriesUseCase;
import com.drobnapomoc.drobnapomoc.dictionary.application.port.out.DictionaryRepository;
import com.drobnapomoc.drobnapomoc.dictionary.domain.City;
import com.drobnapomoc.drobnapomoc.dictionary.domain.ServiceCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
class DictionaryService implements ListCitiesUseCase, ListServiceCategoriesUseCase {

    private final DictionaryRepository dictionaryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<City> listCities() {
        return dictionaryRepository.findAllCities();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceCategory> listServiceCategories() {
        return dictionaryRepository.findActiveCategories();
    }
}
