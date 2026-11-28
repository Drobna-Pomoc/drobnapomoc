package com.drobnapomoc.drobnapomoc.servicerequest.application.port.in;

import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequest;

import java.util.List;

public interface ListMyServiceRequestsUseCase {

    List<ServiceRequest> listMine();
}
