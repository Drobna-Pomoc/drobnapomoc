package com.drobnapomoc.drobnapomoc.servicerequest;

import com.drobnapomoc.drobnapomoc.BaseIntegrationTest;
import com.drobnapomoc.drobnapomoc.api.ServiceRequestsApi;
import com.drobnapomoc.drobnapomoc.api.model.ServiceRequestRest;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceRequestIntegrationTest extends BaseIntegrationTest {

    private static final String CREATE_PATH = BASE_PATH + ServiceRequestsApi.PATH_CREATE_SERVICE_REQUEST;
    private static final String MINE_PATH = BASE_PATH + ServiceRequestsApi.PATH_LIST_MY_SERVICE_REQUESTS;
    private static final String MINE_BY_ID_PATH = BASE_PATH + ServiceRequestsApi.PATH_GET_MY_SERVICE_REQUEST;

    @Value("classpath:json/servicerequest/create-service-request/valid.json")
    private Resource validBody;

    @Test
    void testCreateServiceRequest_givenAnonymousWithCsrf_shouldCreateRequestWithoutCreatedBy(final SoftAssertions softly) throws IOException {
        // when
        final var raw = Objects.requireNonNull(post(anonymousBrowser(), CREATE_PATH, validBody.getContentAsByteArray())
                .expectStatus().isCreated()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody());

        // then
        softly.assertThat(raw).doesNotContain("\"createdBy\"", ":null");
        softly.assertThat(raw).contains("\"phone\":\"+48600123456\"", "\"status\":\"NEW\"", "\"channel\":\"WEB_FORM\"");
    }

    @Test
    void testCreateServiceRequest_givenNoCsrfToken_shouldReturnForbidden() throws IOException {
        restTestClient.post().uri(CREATE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(validBody.getContentAsByteArray())
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void testCreateServiceRequest_givenLoggedInUser_shouldAttributeRequestToThem() throws IOException {
        // given
        final var user = registeredUser();

        // when
        final var request = create(user.browser());

        // then
        assertThat(request.getCreatedBy()).isEqualTo(user.id());
    }

    @Test
    void testListMyServiceRequests_givenTwoUsers_shouldReturnOnlyOwnRequests(final SoftAssertions softly) throws IOException {
        // given
        final var anna = registeredUser().browser();
        final var jan = registeredUser().browser();
        final var annas = create(anna);

        // when
        final var annaMine = listMine(anna);
        final var janMine = listMine(jan);

        // then
        softly.assertThat(annaMine).extracting(ServiceRequestRest::getId).containsExactly(annas.getId());
        softly.assertThat(janMine).isEmpty();
    }

    @Test
    void testGetMyServiceRequest_givenSomeoneElsesRequest_shouldReturnNotFound() throws IOException {
        // given
        final var annas = create(registeredUser().browser());
        final var jan = registeredUser().browser();

        // when & then
        get(jan, MINE_BY_ID_PATH, annas.getId()).expectStatus().isNotFound();
    }

    private ServiceRequestRest create(final Browser browser) throws IOException {
        return Objects.requireNonNull(post(browser, CREATE_PATH, validBody.getContentAsByteArray())
                .expectStatus().isCreated()
                .expectBody(ServiceRequestRest.class)
                .returnResult()
                .getResponseBody());
    }

    private List<ServiceRequestRest> listMine(final Browser browser) {
        return List.of(Objects.requireNonNull(get(browser, MINE_PATH)
                .expectStatus().isOk()
                .expectBody(ServiceRequestRest[].class)
                .returnResult()
                .getResponseBody()));
    }
}
