package com.drobnapomoc.drobnapomoc.servicerequest.application.port.out;

public interface ServiceRequestDictionaryPort {

    boolean isCategoryActive(long categoryId);

    boolean districtExists(long districtId);
}
