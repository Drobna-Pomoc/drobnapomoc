package com.drobnapomoc.drobnapomoc.servicerequest.adapter.in.web;

import com.drobnapomoc.drobnapomoc.api.ServiceRequestsApiDelegate;
import com.drobnapomoc.drobnapomoc.api.model.CreateServiceRequestInputRest;
import com.drobnapomoc.drobnapomoc.api.model.ServiceRequestRest;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.in.CreateServiceRequestUseCase;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.in.GetMyServiceRequestUseCase;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.in.ListMyServiceRequestsUseCase;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ServiceRequestsApiDelegateImpl implements ServiceRequestsApiDelegate {

    private final CreateServiceRequestUseCase createUseCase;
    private final ListMyServiceRequestsUseCase listMyUseCase;
    private final GetMyServiceRequestUseCase getMyUseCase;
    private final ServiceRequestWebMapper mapper;

    @Override
    public ResponseEntity<ServiceRequestRest> createServiceRequest(
            final CreateServiceRequestInputRest input,
            final HttpServletRequest servletRequest
    ) {
        Objects.requireNonNull(input, "Dane zgłoszenia nie mogą być puste");
        final var created = createUseCase.create(mapper.toCommand(input));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toRest(created));
    }

    @Override
    public ResponseEntity<List<ServiceRequestRest>> listMyServiceRequests(final HttpServletRequest servletRequest) {
        return ResponseEntity.ok(mapper.toRestRequests(listMyUseCase.listMine()));
    }

    @Override
    public ResponseEntity<ServiceRequestRest> getMyServiceRequest(
            final Long serviceRequestId,
            final HttpServletRequest servletRequest
    ) {
        return ResponseEntity.ok(mapper.toRest(getMyUseCase.getMine(serviceRequestId)));
    }
}
