package com.drobnapomoc.drobnapomoc.servicerequest.adapter.in.web;

import com.drobnapomoc.drobnapomoc.api.model.CreateServiceRequestInputRest;
import com.drobnapomoc.drobnapomoc.api.model.ServiceRequestClientInputRest;
import com.drobnapomoc.drobnapomoc.api.model.ServiceRequestClientRest;
import com.drobnapomoc.drobnapomoc.api.model.ServiceRequestListingRest;
import com.drobnapomoc.drobnapomoc.api.model.ServiceRequestRest;
import com.drobnapomoc.drobnapomoc.servicerequest.application.command.ClientCommand;
import com.drobnapomoc.drobnapomoc.servicerequest.application.command.CreateServiceRequestCommand;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequest;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequestClient;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequestListing;
import com.drobnapomoc.drobnapomoc.shared.mapping.MappingTool;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = MappingTool.class)
public interface ServiceRequestWebMapper {

    CreateServiceRequestCommand toCommand(CreateServiceRequestInputRest rest);

    ClientCommand toCommand(ServiceRequestClientInputRest rest);

    ServiceRequestRest toRest(ServiceRequest request);

    ServiceRequestClientRest toRest(ServiceRequestClient client);

    ServiceRequestListingRest toRest(ServiceRequestListing listing);

    List<ServiceRequestRest> toRestRequests(List<ServiceRequest> requests);

    List<ServiceRequestListingRest> toRestListings(List<ServiceRequestListing> listings);
}
