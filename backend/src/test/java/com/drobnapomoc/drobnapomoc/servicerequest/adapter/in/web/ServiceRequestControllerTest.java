package com.drobnapomoc.drobnapomoc.servicerequest.adapter.in.web;

import com.drobnapomoc.drobnapomoc.BaseControllerTest;
import com.drobnapomoc.drobnapomoc.api.AdminServiceRequestsApi;
import com.drobnapomoc.drobnapomoc.api.AdminServiceRequestsApiController;
import com.drobnapomoc.drobnapomoc.api.ContractorServiceRequestsApi;
import com.drobnapomoc.drobnapomoc.api.ContractorServiceRequestsApiController;
import com.drobnapomoc.drobnapomoc.api.ServiceRequestsApi;
import com.drobnapomoc.drobnapomoc.api.ServiceRequestsApiController;
import com.drobnapomoc.drobnapomoc.api.model.FieldErrorRest;
import com.drobnapomoc.drobnapomoc.api.model.ServiceRequestListingRest;
import com.drobnapomoc.drobnapomoc.api.model.ServiceRequestRest;
import com.drobnapomoc.drobnapomoc.servicerequest.application.command.ClientCommand;
import com.drobnapomoc.drobnapomoc.servicerequest.application.command.CreateServiceRequestCommand;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.in.CreateServiceRequestUseCase;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.in.GetMyServiceRequestUseCase;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.in.GetOpenServiceRequestUseCase;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.in.GetServiceRequestUseCase;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.in.ListAllServiceRequestsUseCase;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.in.ListMyServiceRequestsUseCase;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.in.ListOpenServiceRequestsUseCase;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.NotifyTarget;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.RequestChannel;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequest;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequestClient;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequestListing;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequestStatus;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.exception.FieldViolation;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.exception.ServiceRequestNotFoundException;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.exception.ServiceRequestValidationException;
import com.drobnapomoc.drobnapomoc.shared.domain.exception.UnauthenticatedException;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest({
        ServiceRequestsApiController.class,
        ContractorServiceRequestsApiController.class,
        AdminServiceRequestsApiController.class
})
@Import({
        ServiceRequestsApiDelegateImpl.class,
        ContractorServiceRequestsApiDelegateImpl.class,
        AdminServiceRequestsApiDelegateImpl.class,
        ServiceRequestWebMapperImpl.class,
        ServiceRequestExceptionHandler.class
})
class ServiceRequestControllerTest extends BaseControllerTest {

    private static final String CREATE_PATH = BASE_PATH + ServiceRequestsApi.PATH_CREATE_SERVICE_REQUEST;
    private static final String MINE_PATH = BASE_PATH + ServiceRequestsApi.PATH_LIST_MY_SERVICE_REQUESTS;
    private static final String MINE_BY_ID_PATH = BASE_PATH + ServiceRequestsApi.PATH_GET_MY_SERVICE_REQUEST;
    private static final String OPEN_PATH = BASE_PATH + ContractorServiceRequestsApi.PATH_LIST_OPEN_SERVICE_REQUESTS;
    private static final String OPEN_BY_ID_PATH = BASE_PATH + ContractorServiceRequestsApi.PATH_GET_OPEN_SERVICE_REQUEST;
    private static final String ADMIN_PATH = BASE_PATH + AdminServiceRequestsApi.PATH_LIST_ALL_SERVICE_REQUESTS;
    private static final String ADMIN_BY_ID_PATH = BASE_PATH + AdminServiceRequestsApi.PATH_GET_SERVICE_REQUEST;

    private static final long REQUEST_ID = 1024L;
    private static final Instant CREATED_AT = Instant.parse("2026-11-23T11:00:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2026-12-07T11:00:00Z");
    private static final String DESCRIPTION = "Cieknie kran w kuchni, trzeba wymienić uszczelkę.";

    @Value("classpath:json/servicerequest/create-service-request/valid.json")
    private Resource validBody;

    @Value("classpath:json/servicerequest/create-service-request/with-optional-fields.json")
    private Resource withOptionalFieldsBody;

    @Value("classpath:json/servicerequest/create-service-request/non-polish-phone.json")
    private Resource nonPolishPhoneBody;

    @MockitoBean
    private CreateServiceRequestUseCase createUseCase;

    @MockitoBean
    private ListMyServiceRequestsUseCase listMyUseCase;

    @MockitoBean
    private GetMyServiceRequestUseCase getMyUseCase;

    @MockitoBean
    private ListOpenServiceRequestsUseCase listOpenUseCase;

    @MockitoBean
    private GetOpenServiceRequestUseCase getOpenUseCase;

    @MockitoBean
    private ListAllServiceRequestsUseCase listAllUseCase;

    @MockitoBean
    private GetServiceRequestUseCase getUseCase;

    @Test
    void testCreateServiceRequest_givenAnonymousWithCsrf_shouldReturnCreatedWithoutEmptyFields(final SoftAssertions softly) throws IOException {
        // given
        given(createUseCase.create(any())).willReturn(anonymousRequest());

        // when
        final var result = mockMvc.post().uri(CREATE_PATH).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(validBody.getContentAsByteArray()).exchange();

        // then
        final var responseJson = json(result);
        final var body = body(result, ServiceRequestRest.class);
        softly.assertThat(status(result)).isEqualTo(HttpStatus.CREATED);
        softly.assertThat(responseJson).doesNotContain("\"createdBy\"", "\"preferredFrom\"", "\"estimateMinMinor\"", "\"apartmentNumber\"");
        softly.assertThat(body.getId()).isEqualTo(REQUEST_ID);
        softly.assertThat(body.getClient().getPhone()).isEqualTo("+48600123456");
        softly.assertThat(body.getCreatedAt().toInstant()).isEqualTo(CREATED_AT);
    }

    @Test
    void testCreateServiceRequest_givenAllFields_shouldPassMappedCommandToUseCase() throws IOException {
        // given
        given(createUseCase.create(any())).willReturn(anonymousRequest());

        // when
        mockMvc.post().uri(CREATE_PATH).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(withOptionalFieldsBody.getContentAsByteArray()).exchange();

        // then
        final var captor = ArgumentCaptor.forClass(CreateServiceRequestCommand.class);
        then(createUseCase).should().create(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new CreateServiceRequestCommand(
                new ClientCommand("Janina", "Nowak", "600123456", "ul. Lipowa", "12A", "5", "20-001", 1L),
                1L,
                DESCRIPTION,
                OffsetDateTime.of(2026, 12, 1, 9, 0, 0, 0, ZoneOffset.ofHours(1)).toInstant(),
                OffsetDateTime.of(2026, 12, 1, 15, 0, 0, 0, ZoneOffset.ofHours(1)).toInstant(),
                NotifyTarget.CAREGIVER
        ));
    }

    @Test
    void testCreateServiceRequest_givenNoCsrfToken_shouldReturnForbidden(final SoftAssertions softly) throws IOException {
        // when
        final var result = mockMvc.post().uri(CREATE_PATH)
                .contentType(MediaType.APPLICATION_JSON).content(validBody.getContentAsByteArray()).exchange();

        // then
        softly.assertThat(status(result)).isEqualTo(HttpStatus.FORBIDDEN);
        softly.assertThat(error(result).getCode()).isEqualTo("CSRF_INVALID");
    }

    @Test
    void testCreateServiceRequest_givenBusinessViolations_shouldReturnUnprocessableContent(final SoftAssertions softly) throws IOException {
        // given
        given(createUseCase.create(any())).willThrow(new ServiceRequestValidationException(List.of(
                new FieldViolation("client.districtId", "Wybrana dzielnica nie istnieje."))));

        // when
        final var result = mockMvc.post().uri(CREATE_PATH).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(validBody.getContentAsByteArray()).exchange();

        // then
        final var error = error(result);
        softly.assertThat(status(result)).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        softly.assertThat(error.getCode()).isEqualTo("VALIDATION_FAILED");
        softly.assertThat(error.getMessage()).isEqualTo("Dane nie przeszły walidacji.");
        softly.assertThat(error.getErrors()).containsExactly(
                new FieldErrorRest("client.districtId", "Wybrana dzielnica nie istnieje."));
    }

    @Test
    void testCreateServiceRequest_givenNonPolishPhone_shouldReturnUnprocessableContent(final SoftAssertions softly) throws IOException {
        // when
        final var result = mockMvc.post().uri(CREATE_PATH).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(nonPolishPhoneBody.getContentAsByteArray()).exchange();

        // then
        softly.assertThat(status(result)).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        softly.assertThat(error(result).getErrors()).extracting(FieldErrorRest::getField).containsOnly("client.phone");
    }

    @Test
    void testListMyServiceRequests_givenNoSession_shouldReturnUnauthorized() {
        assertThat(mockMvc.get().uri(MINE_PATH).exchange()).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testListMyServiceRequests_givenLoggedInClient_shouldReturnTheirRequests(final SoftAssertions softly) throws IOException {
        // given
        given(listMyUseCase.listMine()).willReturn(List.of(anonymousRequest()));

        // when
        final var result = mockMvc.get().uri(MINE_PATH).with(loggedInAs("CLIENT")).exchange();

        // then
        softly.assertThat(status(result)).isEqualTo(HttpStatus.OK);
        softly.assertThat(body(result, ServiceRequestRest[].class))
                .extracting(ServiceRequestRest::getId).containsExactly(REQUEST_ID);
    }

    @Test
    void testGetMyServiceRequest_givenSomeoneElsesRequest_shouldReturnNotFound(final SoftAssertions softly) throws IOException {
        // given
        given(getMyUseCase.getMine(99L)).willThrow(new ServiceRequestNotFoundException(99L));

        // when
        final var result = mockMvc.get().uri(MINE_BY_ID_PATH, 99L).with(loggedInAs("CLIENT")).exchange();

        // then
        softly.assertThat(status(result)).isEqualTo(HttpStatus.NOT_FOUND);
        softly.assertThat(error(result).getCode()).isEqualTo("NOT_FOUND");
    }

    @Test
    void testGetMyServiceRequest_givenSessionWithoutAccount_shouldReturnUnauthorized() {
        // given
        given(getMyUseCase.getMine(REQUEST_ID)).willThrow(new UnauthenticatedException());

        // when
        final var result = mockMvc.get().uri(MINE_BY_ID_PATH, REQUEST_ID).with(loggedInAs("CLIENT")).exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testListOpenServiceRequests_givenContractor_shouldReturnListingsWithoutPersonalData(final SoftAssertions softly) throws IOException {
        // given
        given(listOpenUseCase.listOpen()).willReturn(List.of(listing()));

        // when
        final var result = mockMvc.get().uri(OPEN_PATH).with(loggedInAs("CONTRACTOR")).exchange();

        // then
        final var responseJson = json(result);
        softly.assertThat(status(result)).isEqualTo(HttpStatus.OK);
        softly.assertThat(responseJson).doesNotContain("\"client\"", "\"phone\"", "\"createdBy\"");
        softly.assertThat(body(result, ServiceRequestListingRest[].class))
                .extracting(ServiceRequestListingRest::getDistrictId).containsExactly(7L);
    }

    @Test
    void testListOpenServiceRequests_givenClient_shouldReturnForbidden() {
        assertThat(mockMvc.get().uri(OPEN_PATH).with(loggedInAs("CLIENT")).exchange()).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    void testListOpenServiceRequests_givenNoSession_shouldReturnUnauthorized() {
        assertThat(mockMvc.get().uri(OPEN_PATH).exchange()).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testGetOpenServiceRequest_givenContractor_shouldReturnListing() {
        // given
        given(getOpenUseCase.getOpen(REQUEST_ID)).willReturn(listing());

        // when & then
        assertThat(mockMvc.get().uri(OPEN_BY_ID_PATH, REQUEST_ID).with(loggedInAs("CONTRACTOR")).exchange())
                .hasStatusOk();
    }

    @Test
    void testGetOpenServiceRequest_givenClient_shouldReturnForbidden() {
        assertThat(mockMvc.get().uri(OPEN_BY_ID_PATH, REQUEST_ID).with(loggedInAs("CLIENT")).exchange())
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CONSULTANT", "ADMIN"})
    void testListAllServiceRequests_givenStaff_shouldReturnRequests(final String role) {
        // given
        given(listAllUseCase.listAll()).willReturn(List.of(anonymousRequest()));

        // when & then
        assertThat(mockMvc.get().uri(ADMIN_PATH).with(loggedInAs(role)).exchange()).hasStatusOk();
    }

    @Test
    void testListAllServiceRequests_givenContractor_shouldReturnForbidden() {
        assertThat(mockMvc.get().uri(ADMIN_PATH).with(loggedInAs("CONTRACTOR")).exchange())
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CONSULTANT", "ADMIN"})
    void testGetServiceRequest_givenStaff_shouldReturnRequest(final String role) {
        // given
        given(getUseCase.get(REQUEST_ID)).willReturn(anonymousRequest());

        // when & then
        assertThat(mockMvc.get().uri(ADMIN_BY_ID_PATH, REQUEST_ID).with(loggedInAs(role)).exchange()).hasStatusOk();
    }

    @Test
    void testGetServiceRequest_givenMissingRequest_shouldReturnNotFound() {
        // given
        given(getUseCase.get(5L)).willThrow(new ServiceRequestNotFoundException(5L));

        // when & then
        assertThat(mockMvc.get().uri(ADMIN_BY_ID_PATH, 5L).with(loggedInAs("ADMIN")).exchange())
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "0", "-1"})
    void testGetServiceRequest_givenInvalidId_shouldReturnNotFound(final String id, final SoftAssertions softly) throws IOException {
        // when
        final var result = mockMvc.get().uri(ADMIN_BY_ID_PATH, id).with(loggedInAs("ADMIN")).exchange();

        // then
        softly.assertThat(status(result)).isEqualTo(HttpStatus.NOT_FOUND);
        softly.assertThat(error(result).getCode()).isEqualTo("NOT_FOUND");
    }

    private static ServiceRequest anonymousRequest() {
        return new ServiceRequest(
                REQUEST_ID, ServiceRequestStatus.NEW, RequestChannel.WEB_FORM, 3L, DESCRIPTION, null, null,
                NotifyTarget.CLIENT,
                new ServiceRequestClient("Janina", "Nowak", "+48600123456", "ul. Lipowa", "12A", null, "20-001", 7L),
                null, null, null, EXPIRES_AT, null, null, CREATED_AT, CREATED_AT
        );
    }

    private static ServiceRequestListing listing() {
        return new ServiceRequestListing(
                REQUEST_ID, ServiceRequestStatus.NEW, 3L, DESCRIPTION, null, null, 7L, null, null, EXPIRES_AT, CREATED_AT
        );
    }
}
