package com.drobnapomoc.drobnapomoc.dictionary.application.port.in;

import com.drobnapomoc.drobnapomoc.dictionary.domain.ServiceCategory;

import java.util.List;

public interface ListServiceCategoriesUseCase {

    List<ServiceCategory> listServiceCategories();
}
