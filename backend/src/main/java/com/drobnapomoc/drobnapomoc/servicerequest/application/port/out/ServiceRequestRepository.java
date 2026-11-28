package com.drobnapomoc.drobnapomoc.servicerequest.application.port.out;

import com.drobnapomoc.drobnapomoc.servicerequest.domain.NewServiceRequest;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequest;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequestListing;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ServiceRequestRepository {

    ServiceRequest create(NewServiceRequest request);

    Optional<ServiceRequest> findById(long id);

    Optional<ServiceRequest> findByIdAndCreator(long id, long accountId);

    /** Newest first. */
    List<ServiceRequest> findByCreator(long accountId);

    /** Newest first. */
    List<ServiceRequest> findAll();

    /** {@code NEW} and {@code PRICED} requests with {@code expiresAt} after {@code now}, newest first. */
    List<ServiceRequestListing> findOpen(Instant now);

    Optional<ServiceRequestListing> findOpenById(long id, Instant now);
}
