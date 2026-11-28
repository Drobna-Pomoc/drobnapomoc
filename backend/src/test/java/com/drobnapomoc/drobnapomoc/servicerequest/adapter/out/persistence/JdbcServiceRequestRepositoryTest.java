package com.drobnapomoc.drobnapomoc.servicerequest.adapter.out.persistence;

import com.drobnapomoc.drobnapomoc.RepositoryTest;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.NewServiceRequest;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.NotifyTarget;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.RequestChannel;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequest;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequestClient;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequestListing;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequestStatus;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@RepositoryTest
@Import({
        JdbcServiceRequestRepository.class,
        ServiceRequestRowMapper.class,
        ServiceRequestListingRowMapper.class
})
@ExtendWith(SoftAssertionsExtension.class)
class JdbcServiceRequestRepositoryTest {

    private static final Instant NOW = Instant.parse("2026-11-23T11:00:00Z");
    private static final long ANNA_ID = 1L;
    private static final long NEW_REQUEST_ID = 1L;
    private static final long PRICED_REQUEST_ID = 2L;
    private static final long ACCEPTED_REQUEST_ID = 3L;
    private static final long EXPIRED_REQUEST_ID = 4L;
    private static final long MISSING_ID = 999_999L;
    private static final String DESCRIPTION = "Cieknie kran w kuchni, trzeba wymienić uszczelkę.";

    @Autowired
    private JdbcServiceRequestRepository repository;

    @Test
    void testFindById_givenSeededRequestWithPreferredWindow_shouldMapAllFields() {
        // when
        final var request = repository.findById(NEW_REQUEST_ID);

        // then
        assertThat(request).contains(new ServiceRequest(
                NEW_REQUEST_ID, ServiceRequestStatus.NEW, RequestChannel.WEB_FORM, 1L, DESCRIPTION,
                Instant.parse("2026-12-01T08:00:00Z"), Instant.parse("2026-12-01T14:00:00Z"), NotifyTarget.CLIENT,
                new ServiceRequestClient("Janina", "Nowak", "+48600123456", "ul. Lipowa", "12A", "5", "20-001", 1L),
                null, null, null, Instant.parse("2026-12-04T10:00:00Z"), ANNA_ID, null,
                Instant.parse("2026-11-20T10:00:00Z"), Instant.parse("2026-11-20T10:00:00Z")
        ));
    }

    @Test
    void testFindById_givenSeededPricedRequest_shouldMapEstimate(final SoftAssertions softly) {
        // when
        final var request = repository.findById(PRICED_REQUEST_ID);

        // then
        assertThat(request).hasValueSatisfying(priced -> {
            softly.assertThat(priced.status()).isEqualTo(ServiceRequestStatus.PRICED);
            softly.assertThat(priced.estimateMinMinor()).isEqualTo(8000L);
            softly.assertThat(priced.estimateMaxMinor()).isEqualTo(15000L);
            softly.assertThat(priced.estimatedAt()).isEqualTo(Instant.parse("2026-11-21T12:00:00Z"));
            softly.assertThat(priced.notifyTarget()).isEqualTo(NotifyTarget.CAREGIVER);
        });
    }

    @Test
    void testFindById_givenMissingId_shouldReturnEmpty() {
        assertThat(repository.findById(MISSING_ID)).isEmpty();
    }

    @Test
    void testCreate_givenAnonymousRequest_shouldStoreItAsNewWithoutOptionalFields(final SoftAssertions softly) {
        // given
        final var expiresAt = NOW.plus(Duration.ofDays(14));

        // when
        final var id = repository.create(newRequest(null, null, null, null));

        // then
        assertThat(repository.findById(id)).hasValueSatisfying(request -> {
            softly.assertThat(request.status()).isEqualTo(ServiceRequestStatus.NEW);
            softly.assertThat(request.channel()).isEqualTo(RequestChannel.WEB_FORM);
            softly.assertThat(request.client()).isEqualTo(client(null));
            softly.assertThat(request.preferredFrom()).isNull();
            softly.assertThat(request.preferredTo()).isNull();
            softly.assertThat(request.estimateMinMinor()).isNull();
            softly.assertThat(request.createdBy()).isNull();
            softly.assertThat(request.expiresAt()).isEqualTo(expiresAt);
            softly.assertThat(request.createdAt()).isNotNull();
        });
    }

    @Test
    void testCreate_givenOptionalFields_shouldStoreThem(final SoftAssertions softly) {
        // given
        final var from = NOW.plus(Duration.ofDays(1));
        final var to = from.plus(Duration.ofHours(6));

        // when
        final var id = repository.create(newRequest(ANNA_ID, from, to, "5"));

        // then
        assertThat(repository.findById(id)).hasValueSatisfying(saved -> {
            softly.assertThat(saved.preferredFrom()).isEqualTo(from);
            softly.assertThat(saved.preferredTo()).isEqualTo(to);
            softly.assertThat(saved.createdBy()).isEqualTo(ANNA_ID);
            softly.assertThat(saved.client().apartmentNumber()).isEqualTo("5");
        });
    }

    @Test
    void testFindByCreator_givenSeededRequests_shouldReturnOnlyOwnNewestFirst() {
        assertThat(repository.findByCreator(ANNA_ID)).extracting(ServiceRequest::id)
                .containsExactly(PRICED_REQUEST_ID, NEW_REQUEST_ID);
    }

    @Test
    void testFindByIdAndCreator_givenOwnRequest_shouldReturnIt() {
        assertThat(repository.findByIdAndCreator(NEW_REQUEST_ID, ANNA_ID)).isPresent();
    }

    @Test
    void testFindByIdAndCreator_givenSomeoneElsesRequest_shouldReturnEmpty() {
        assertThat(repository.findByIdAndCreator(ACCEPTED_REQUEST_ID, ANNA_ID)).isEmpty();
    }

    @Test
    void testFindAll_givenSeededRequests_shouldReturnAllNewestFirst() {
        assertThat(repository.findAll()).extracting(ServiceRequest::id)
                .containsExactly(ACCEPTED_REQUEST_ID, PRICED_REQUEST_ID, NEW_REQUEST_ID, EXPIRED_REQUEST_ID);
    }

    @Test
    void testFindOpen_givenRequestsInVariousStates_shouldReturnOnlyNewAndPricedNotExpiredNewestFirst() {
        assertThat(repository.findOpen(NOW)).extracting(ServiceRequestListing::id)
                .containsExactly(PRICED_REQUEST_ID, NEW_REQUEST_ID);
    }

    @Test
    void testFindOpenById_givenOpenRequest_shouldReturnListingWithDistrict() {
        assertThat(repository.findOpenById(NEW_REQUEST_ID, NOW))
                .hasValueSatisfying(listing -> assertThat(listing.districtId()).isEqualTo(1L));
    }

    @Test
    void testFindOpenById_givenAcceptedRequest_shouldReturnEmpty() {
        assertThat(repository.findOpenById(ACCEPTED_REQUEST_ID, NOW)).isEmpty();
    }

    @Test
    void testFindOpenById_givenExpiredRequest_shouldReturnEmpty() {
        assertThat(repository.findOpenById(EXPIRED_REQUEST_ID, NOW)).isEmpty();
    }

    private static ServiceRequestClient client(final @Nullable String apartmentNumber) {
        return new ServiceRequestClient("Janina", "Nowak", "+48600123456", "ul. Lipowa", "12A", apartmentNumber, "20-001", 1L);
    }

    private static NewServiceRequest newRequest(
            final @Nullable Long createdBy,
            final @Nullable Instant preferredFrom,
            final @Nullable Instant preferredTo,
            final @Nullable String apartmentNumber
    ) {
        return new NewServiceRequest(
                client(apartmentNumber), 1L, DESCRIPTION, preferredFrom, preferredTo,
                NotifyTarget.CLIENT, RequestChannel.WEB_FORM, createdBy, NOW.plus(Duration.ofDays(14))
        );
    }
}
