package com.drobnapomoc.drobnapomoc.servicerequest.adapter.out.dictionary;

import com.drobnapomoc.drobnapomoc.dictionary.application.port.in.DictionaryLookupUseCase;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.out.ServiceRequestDictionaryPort;
import com.drobnapomoc.drobnapomoc.shared.domain.ServiceCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
class DictionaryModuleAdapter implements ServiceRequestDictionaryPort {

    private final DictionaryLookupUseCase dictionaryLookup;

    @Override
    public Optional<ServiceCategory> findActiveCategory(final long categoryId) {
        return dictionaryLookup.findActiveCategory(categoryId);
    }

    @Override
    public boolean districtExists(final long districtId) {
        return dictionaryLookup.districtExists(districtId);
    }
}
