package com.drobnapomoc.drobnapomoc.servicerequest.application.port.in;

import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequest;

public interface GetMyServiceRequestUseCase {

    ServiceRequest getMine(long id);
}
