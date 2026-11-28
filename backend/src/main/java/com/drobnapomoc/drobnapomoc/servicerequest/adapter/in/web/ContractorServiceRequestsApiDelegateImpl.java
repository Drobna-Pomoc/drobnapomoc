package com.drobnapomoc.drobnapomoc.servicerequest.adapter.in.web;

import com.drobnapomoc.drobnapomoc.api.ContractorServiceRequestsApiDelegate;
import com.drobnapomoc.drobnapomoc.api.model.ServiceRequestListingRest;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.in.GetOpenServiceRequestUseCase;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.in.ListOpenServiceRequestsUseCase;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ContractorServiceRequestsApiDelegateImpl implements ContractorServiceRequestsApiDelegate {

    private final ListOpenServiceRequestsUseCase listOpenUseCase;
    private final GetOpenServiceRequestUseCase getOpenUseCase;
    private final ServiceRequestWebMapper mapper;

    @Override
    public ResponseEntity<List<ServiceRequestListingRest>> listOpenServiceRequests(final HttpServletRequest servletRequest) {
        return ResponseEntity.ok(mapper.toRestListings(listOpenUseCase.listOpen()));
    }

    @Override
    public ResponseEntity<ServiceRequestListingRest> getOpenServiceRequest(
            final Long serviceRequestId,
            final HttpServletRequest servletRequest
    ) {
        return ResponseEntity.ok(mapper.toRest(getOpenUseCase.getOpen(serviceRequestId)));
    }
}
