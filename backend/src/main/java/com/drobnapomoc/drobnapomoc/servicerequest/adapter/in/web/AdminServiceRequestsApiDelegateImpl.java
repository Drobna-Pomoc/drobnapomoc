package com.drobnapomoc.drobnapomoc.servicerequest.adapter.in.web;

import com.drobnapomoc.drobnapomoc.api.AdminServiceRequestsApiDelegate;
import com.drobnapomoc.drobnapomoc.api.model.ServiceRequestRest;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.in.GetServiceRequestUseCase;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.in.ListAllServiceRequestsUseCase;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminServiceRequestsApiDelegateImpl implements AdminServiceRequestsApiDelegate {

    private final ListAllServiceRequestsUseCase listAllUseCase;
    private final GetServiceRequestUseCase getUseCase;
    private final ServiceRequestWebMapper mapper;

    @Override
    public ResponseEntity<List<ServiceRequestRest>> listAllServiceRequests(final HttpServletRequest servletRequest) {
        return ResponseEntity.ok(mapper.toRestRequests(listAllUseCase.listAll()));
    }

    @Override
    public ResponseEntity<ServiceRequestRest> getServiceRequest(
            final Long serviceRequestId,
            final HttpServletRequest servletRequest
    ) {
        return ResponseEntity.ok(mapper.toRest(getUseCase.get(serviceRequestId)));
    }
}
