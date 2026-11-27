package com.drobnapomoc.drobnapomoc.servicerequest.application.port.in;

import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequestListing;

import java.util.List;

public interface ListOpenServiceRequestsUseCase {

    List<ServiceRequestListing> listOpen();
}
