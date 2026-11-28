package com.drobnapomoc.drobnapomoc.dictionary.application.port.in;

import com.drobnapomoc.drobnapomoc.shared.domain.ServiceCategory;

import java.util.List;

public interface ListServiceCategoriesUseCase {

    List<ServiceCategory> listServiceCategories();
}
