package com.drobnapomoc.drobnapomoc.servicerequest.application.port.in;

import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequestListing;

public interface GetOpenServiceRequestUseCase {

    ServiceRequestListing getOpen(long id);
}
