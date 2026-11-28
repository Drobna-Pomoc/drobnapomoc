package com.drobnapomoc.drobnapomoc.servicerequest.application.port.in;

import com.drobnapomoc.drobnapomoc.servicerequest.application.command.CreateServiceRequestCommand;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequest;

public interface CreateServiceRequestUseCase {

    ServiceRequest create(CreateServiceRequestCommand command);
}
