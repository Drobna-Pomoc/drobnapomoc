package com.drobnapomoc.drobnapomoc.servicerequest.adapter.out.dictionary;

import com.drobnapomoc.drobnapomoc.dictionary.application.port.in.DictionaryLookupUseCase;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.out.ServiceRequestDictionaryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class DictionaryModuleAdapter implements ServiceRequestDictionaryPort {

    private final DictionaryLookupUseCase dictionaryLookup;

    @Override
    public boolean isCategoryActive(final long categoryId) {
        return dictionaryLookup.isCategoryActive(categoryId);
    }

    @Override
    public boolean districtExists(final long districtId) {
        return dictionaryLookup.districtExists(districtId);
    }
}
