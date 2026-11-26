package com.drobnapomoc.drobnapomoc.dictionary.adapter.in.web;

import com.drobnapomoc.drobnapomoc.api.DictionariesApiDelegate;
import com.drobnapomoc.drobnapomoc.api.model.CityRest;
import com.drobnapomoc.drobnapomoc.api.model.ServiceCategoryRest;
import com.drobnapomoc.drobnapomoc.dictionary.application.port.in.ListCitiesUseCase;
import com.drobnapomoc.drobnapomoc.dictionary.application.port.in.ListServiceCategoriesUseCase;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DictionariesApiDelegateImpl implements DictionariesApiDelegate {

    private final ListCitiesUseCase listCitiesUseCase;
    private final ListServiceCategoriesUseCase listServiceCategoriesUseCase;
    private final DictionaryWebMapper mapper;

    @Override
    public ResponseEntity<List<CityRest>> listCities(final HttpServletRequest servletRequest) {
        return ResponseEntity.ok(mapper.toRestCities(listCitiesUseCase.listCities()));
    }

    @Override
    public ResponseEntity<List<ServiceCategoryRest>> listServiceCategories(final HttpServletRequest servletRequest) {
        return ResponseEntity.ok(mapper.toRestCategories(listServiceCategoriesUseCase.listServiceCategories()));
    }
}
