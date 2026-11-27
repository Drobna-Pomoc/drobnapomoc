package com.drobnapomoc.drobnapomoc.servicerequest.adapter.in.web;

import com.drobnapomoc.drobnapomoc.api.model.CreateServiceRequestInputRest;
import com.drobnapomoc.drobnapomoc.api.model.NotifyTargetRest;
import com.drobnapomoc.drobnapomoc.api.model.RequestChannelRest;
import com.drobnapomoc.drobnapomoc.api.model.ServiceRequestClientInputRest;
import com.drobnapomoc.drobnapomoc.api.model.ServiceRequestClientRest;
import com.drobnapomoc.drobnapomoc.api.model.ServiceRequestListingRest;
import com.drobnapomoc.drobnapomoc.api.model.ServiceRequestRest;
import com.drobnapomoc.drobnapomoc.api.model.ServiceRequestStatusRest;
import com.drobnapomoc.drobnapomoc.servicerequest.application.command.ClientCommand;
import com.drobnapomoc.drobnapomoc.servicerequest.application.command.CreateServiceRequestCommand;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.NotifyTarget;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.RequestChannel;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequest;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequestClient;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequestListing;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequestStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceRequestWebMapperTest {

    private static final Instant CREATED_AT = Instant.parse("2026-11-23T11:00:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2026-12-07T11:00:00Z");
    private static final Instant PREFERRED_FROM = Instant.parse("2026-12-01T08:00:00Z");
    private static final Instant PREFERRED_TO = Instant.parse("2026-12-01T14:00:00Z");
    private static final String DESCRIPTION = "Cieknie kran w kuchni, trzeba wymienić uszczelkę.";

    private final ServiceRequestWebMapper mapper = new ServiceRequestWebMapperImpl();

    @Test
    void testToCommand_givenAllFields_shouldMapThemConvertingTimesToInstants() {
        // given
        final var input = new CreateServiceRequestInputRest(clientInput().apartmentNumber("5"), 3L, DESCRIPTION)
                .preferredFrom(OffsetDateTime.of(2026, 12, 1, 9, 0, 0, 0, ZoneOffset.ofHours(1)))
                .preferredTo(OffsetDateTime.of(2026, 12, 1, 15, 0, 0, 0, ZoneOffset.ofHours(1)))
                .notifyTarget(NotifyTargetRest.CAREGIVER);

        // when
        final var command = mapper.toCommand(input);

        // then
        assertThat(command).isEqualTo(new CreateServiceRequestCommand(
                new ClientCommand("Janina", "Nowak", "600123456", "ul. Lipowa", "12A", "5", "20-001", 7L),
                3L, DESCRIPTION, PREFERRED_FROM, PREFERRED_TO, NotifyTarget.CAREGIVER
        ));
    }

    @Test
    void testToCommand_givenOnlyRequiredFields_shouldLeaveOptionalFieldsEmpty() {
        // given
        final var input = new CreateServiceRequestInputRest(clientInput(), 3L, DESCRIPTION);

        // when
        final var command = mapper.toCommand(input);

        // then
        assertThat(command).isEqualTo(new CreateServiceRequestCommand(
                new ClientCommand("Janina", "Nowak", "600123456", "ul. Lipowa", "12A", null, "20-001", 7L),
                3L, DESCRIPTION, null, null, null
        ));
    }

    @Test
    void testToRest_givenRequestWithAllOptionalFields_shouldMapAllFields() {
        // given
        final var request = new ServiceRequest(
                1L, ServiceRequestStatus.PRICED, RequestChannel.PHONE, 3L, DESCRIPTION, PREFERRED_FROM, PREFERRED_TO,
                NotifyTarget.CAREGIVER, client("5"), 8000L, 15000L, CREATED_AT, EXPIRES_AT, 123L,
                "Usterkę naprawił sąsiad.", CREATED_AT, CREATED_AT
        );

        // when
        final var rest = mapper.toRest(request);

        // then
        assertThat(rest).isEqualTo(new ServiceRequestRest(
                1L, ServiceRequestStatusRest.PRICED, RequestChannelRest.PHONE, 3L, DESCRIPTION, NotifyTargetRest.CAREGIVER,
                new ServiceRequestClientRest("Janina", "Nowak", "+48600123456", "ul. Lipowa", "12A", "20-001", 7L)
                        .apartmentNumber("5"),
                utc(EXPIRES_AT), utc(CREATED_AT), utc(CREATED_AT)
        )
                .preferredFrom(utc(PREFERRED_FROM))
                .preferredTo(utc(PREFERRED_TO))
                .estimateMinMinor(8000L)
                .estimateMaxMinor(15000L)
                .estimatedAt(utc(CREATED_AT))
                .createdBy(123L)
                .cancelReason("Usterkę naprawił sąsiad."));
    }

    @Test
    void testToRest_givenAnonymousRequestWithoutOptionalFields_shouldLeaveThemEmpty() {
        // given
        final var request = new ServiceRequest(
                1L, ServiceRequestStatus.NEW, RequestChannel.WEB_FORM, 3L, DESCRIPTION, null, null,
                NotifyTarget.CLIENT, client(null), null, null, null, EXPIRES_AT, null, null, CREATED_AT, CREATED_AT
        );

        // when
        final var rest = mapper.toRest(request);

        // then
        assertThat(rest).isEqualTo(new ServiceRequestRest(
                1L, ServiceRequestStatusRest.NEW, RequestChannelRest.WEB_FORM, 3L, DESCRIPTION, NotifyTargetRest.CLIENT,
                new ServiceRequestClientRest("Janina", "Nowak", "+48600123456", "ul. Lipowa", "12A", "20-001", 7L),
                utc(EXPIRES_AT), utc(CREATED_AT), utc(CREATED_AT)
        ));
    }

    @Test
    void testToRest_givenListingWithEstimate_shouldMapAllFields() {
        // given
        final var listing = new ServiceRequestListing(
                1L, ServiceRequestStatus.PRICED, 3L, DESCRIPTION, PREFERRED_FROM, PREFERRED_TO, 7L, 8000L, 15000L,
                EXPIRES_AT, CREATED_AT
        );

        // when
        final var rest = mapper.toRest(listing);

        // then
        assertThat(rest).isEqualTo(new ServiceRequestListingRest(
                1L, ServiceRequestStatusRest.PRICED, 3L, DESCRIPTION, 7L, utc(EXPIRES_AT), utc(CREATED_AT)
        )
                .preferredFrom(utc(PREFERRED_FROM))
                .preferredTo(utc(PREFERRED_TO))
                .estimateMinMinor(8000L)
                .estimateMaxMinor(15000L));
    }

    @Test
    void testToRestRequests_givenEmptyList_shouldReturnEmptyList() {
        assertThat(mapper.toRestRequests(List.of())).isEmpty();
    }

    @Test
    void testToRestListings_givenEmptyList_shouldReturnEmptyList() {
        assertThat(mapper.toRestListings(List.of())).isEmpty();
    }

    @Test
    void testServiceRequestStatus_givenGeneratedRestEnum_shouldHaveSameConstants() {
        assertThat(names(ServiceRequestStatus.values())).isEqualTo(names(ServiceRequestStatusRest.values()));
    }

    @Test
    void testRequestChannel_givenGeneratedRestEnum_shouldHaveSameConstants() {
        assertThat(names(RequestChannel.values())).isEqualTo(names(RequestChannelRest.values()));
    }

    @Test
    void testNotifyTarget_givenGeneratedRestEnum_shouldHaveSameConstants() {
        assertThat(names(NotifyTarget.values())).isEqualTo(names(NotifyTargetRest.values()));
    }

    private static List<String> names(final Enum<?>[] values) {
        return Arrays.stream(values).map(Enum::name).toList();
    }

    private static OffsetDateTime utc(final Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }

    private static ServiceRequestClientInputRest clientInput() {
        return new ServiceRequestClientInputRest("Janina", "Nowak", "600123456", "ul. Lipowa", "12A", "20-001", 7L);
    }

    private static ServiceRequestClient client(final String apartmentNumber) {
        return new ServiceRequestClient("Janina", "Nowak", "+48600123456", "ul. Lipowa", "12A", apartmentNumber, "20-001", 7L);
    }
}
