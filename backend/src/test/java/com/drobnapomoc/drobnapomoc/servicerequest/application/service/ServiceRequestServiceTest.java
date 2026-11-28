package com.drobnapomoc.drobnapomoc.servicerequest.application.service;

import com.drobnapomoc.drobnapomoc.servicerequest.application.ServiceRequestProperties;
import com.drobnapomoc.drobnapomoc.servicerequest.application.command.ClientCommand;
import com.drobnapomoc.drobnapomoc.servicerequest.application.command.CreateServiceRequestCommand;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.out.ServiceRequestDictionaryPort;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.out.ServiceRequestRepository;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.NewServiceRequest;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.NotifyTarget;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.RequestChannel;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequest;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequestClient;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequestListing;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequestStatus;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.exception.FieldViolation;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.exception.ServiceRequestNotFoundException;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.exception.ServiceRequestValidationException;
import com.drobnapomoc.drobnapomoc.shared.domain.AccountRole;
import com.drobnapomoc.drobnapomoc.shared.domain.CurrentUser;
import com.drobnapomoc.drobnapomoc.shared.domain.CurrentUserProvider;
import com.drobnapomoc.drobnapomoc.shared.domain.exception.UnauthenticatedException;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ServiceRequestServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-23T11:00:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2026-12-07T11:00:00Z");
    private static final long CATEGORY_ID = 3L;
    private static final long DISTRICT_ID = 7L;
    private static final long ACCOUNT_ID = 123L;
    private static final long REQUEST_ID = 1024L;
    private static final long MISSING_ID = 99L;
    private static final String DESCRIPTION = "Cieknie kran w kuchni, trzeba wymienić uszczelkę.";
    private static final String EMPTY_FIELD = "Pole nie może być puste.";

    @Mock
    private ServiceRequestRepository repository;

    @Mock
    private ServiceRequestDictionaryPort dictionaryPort;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private ServiceRequestService service;

    @BeforeEach
    void setUp() {
        service = new ServiceRequestService(
                repository,
                dictionaryPort,
                currentUserProvider,
                new ServiceRequestProperties(Duration.ofDays(14)),
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void testCreate_givenAnonymousValidCommand_shouldStoreNormalizedRequestWithDefaults() {
        // given
        givenAnonymousVisitor();
        givenDictionaryEntriesExist();
        givenStoredRequest();

        // when
        service.create(validCommand());

        // then
        assertThat(savedRequest()).isEqualTo(new NewServiceRequest(
                new ServiceRequestClient("Janina", "Nowak", "+48600123456", "ul. Lipowa", "12A", null, "20-001", DISTRICT_ID),
                CATEGORY_ID, DESCRIPTION, null, null, NotifyTarget.CLIENT, RequestChannel.WEB_FORM, null, EXPIRES_AT
        ));
    }

    @Test
    void testCreate_givenValidCommand_shouldReturnStoredRequest() {
        // given
        givenAnonymousVisitor();
        givenDictionaryEntriesExist();
        givenStoredRequest();

        // when
        final var result = service.create(validCommand());

        // then
        assertThat(result).isEqualTo(sampleRequest());
    }

    @Test
    void testCreate_givenLoggedInAccount_shouldAttributeRequestToIt() {
        // given
        givenLoggedInUser();
        givenDictionaryEntriesExist();
        givenStoredRequest();

        // when
        service.create(command(clientWithPadding(), DESCRIPTION, null, null, NotifyTarget.CAREGIVER));

        // then
        final var saved = savedRequest();
        assertThat(saved.createdBy()).isEqualTo(ACCOUNT_ID);
        assertThat(saved.notifyTarget()).isEqualTo(NotifyTarget.CAREGIVER);
    }

    @Test
    void testCreate_givenPreferredWindow_shouldKeepIt() {
        // given
        givenAnonymousVisitor();
        givenDictionaryEntriesExist();
        givenStoredRequest();
        final var from = NOW.plus(Duration.ofDays(1));
        final var to = from.plus(Duration.ofHours(6));

        // when
        service.create(command(clientWithPadding(), DESCRIPTION, from, to, null));

        // then
        final var saved = savedRequest();
        assertThat(saved.preferredFrom()).isEqualTo(from);
        assertThat(saved.preferredTo()).isEqualTo(to);
    }

    @Test
    void testCreate_givenSavedRequestCannotBeRead_shouldThrowIllegalState() {
        // given
        givenAnonymousVisitor();
        givenDictionaryEntriesExist();
        given(repository.create(any())).willReturn(REQUEST_ID);
        given(repository.findById(REQUEST_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.create(validCommand())).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testCreate_givenSeveralBusinessViolations_shouldReportAllAtOnce() {
        // given
        givenAnonymousVisitor();
        given(dictionaryPort.isCategoryActive(CATEGORY_ID)).willReturn(false);
        given(dictionaryPort.districtExists(DISTRICT_ID)).willReturn(false);
        final var from = NOW.plus(Duration.ofDays(2));
        final var to = NOW.plus(Duration.ofDays(1));

        // when
        final var violations = violationsOf(command(clientWithPadding(), DESCRIPTION, from, to, NotifyTarget.CAREGIVER));

        // then
        assertThat(violations).containsExactlyInAnyOrder(
                new FieldViolation("categoryId", "Wybrana kategoria nie istnieje lub jest nieaktywna."),
                new FieldViolation("client.districtId", "Wybrana dzielnica nie istnieje."),
                new FieldViolation("notifyTarget", "Powiadamianie opiekuna wymaga zalogowania."),
                new FieldViolation("preferredTo", "Koniec terminu nie może być wcześniej niż jego początek.")
        );
    }

    @Test
    void testCreate_givenPreferredToInThePast_shouldRejectIt() {
        // given
        givenAnonymousVisitor();
        givenDictionaryEntriesExist();

        // when
        final var violations = violationsOf(command(clientWithPadding(), DESCRIPTION, null, NOW.minusSeconds(60), null));

        // then
        assertThat(violations).containsExactly(
                new FieldViolation("preferredTo", "Koniec terminu nie może być w przeszłości.")
        );
    }

    @Test
    void testCreate_givenBlankRequiredFields_shouldRejectEachOfThem() {
        // given
        givenAnonymousVisitor();
        givenDictionaryEntriesExist();
        final var blankClient = new ClientCommand("  ", " ", "600123456", "  ", " ", null, "20-001", DISTRICT_ID);

        // when
        final var violations = violationsOf(command(blankClient, "          ", null, null, null));

        // then
        assertThat(violations).containsExactlyInAnyOrder(
                new FieldViolation("description", EMPTY_FIELD),
                new FieldViolation("client.firstName", EMPTY_FIELD),
                new FieldViolation("client.lastName", EMPTY_FIELD),
                new FieldViolation("client.street", EMPTY_FIELD),
                new FieldViolation("client.houseNumber", EMPTY_FIELD)
        );
    }

    @Test
    void testCreate_givenDescriptionShorterThanTenCharactersAfterTrim_shouldRejectIt() {
        // given
        givenAnonymousVisitor();
        givenDictionaryEntriesExist();

        // when
        final var violations = violationsOf(command(clientWithPadding(), "   Kran    ", null, null, null));

        // then
        assertThat(violations).containsExactly(
                new FieldViolation("description", "Opis musi mieć co najmniej 10 znaków.")
        );
    }

    // listMine / getMine

    @Test
    void testListMine_givenLoggedInAccount_shouldReturnItsRequests() {
        // given
        givenRequiredUser();
        given(repository.findByCreator(ACCOUNT_ID)).willReturn(List.of(sampleRequest()));

        // when & then
        assertThat(service.listMine()).containsExactly(sampleRequest());
    }

    @Test
    void testListMine_givenNoAccount_shouldThrowUnauthenticated() {
        // given
        givenNoRequiredUser();

        // when & then
        assertThatThrownBy(() -> service.listMine()).isInstanceOf(UnauthenticatedException.class);
    }

    @Test
    void testGetMine_givenOwnRequest_shouldReturnIt() {
        // given
        givenRequiredUser();
        given(repository.findByIdAndCreator(REQUEST_ID, ACCOUNT_ID)).willReturn(Optional.of(sampleRequest()));

        // when & then
        assertThat(service.getMine(REQUEST_ID)).isEqualTo(sampleRequest());
    }

    @Test
    void testGetMine_givenSomeoneElsesOrMissingRequest_shouldThrowNotFound() {
        // given
        givenRequiredUser();
        given(repository.findByIdAndCreator(MISSING_ID, ACCOUNT_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.getMine(MISSING_ID)).isInstanceOf(ServiceRequestNotFoundException.class);
    }

    @Test
    void testGetMine_givenNoAccount_shouldThrowUnauthenticated() {
        // given
        givenNoRequiredUser();

        // when & then
        assertThatThrownBy(() -> service.getMine(REQUEST_ID)).isInstanceOf(UnauthenticatedException.class);
    }

    @Test
    void testListOpen_givenOpenRequests_shouldQueryAtCurrentTime() {
        // given
        given(repository.findOpen(NOW)).willReturn(List.of(sampleListing()));

        // when & then
        assertThat(service.listOpen()).containsExactly(sampleListing());
    }

    @Test
    void testGetOpen_givenOpenRequest_shouldReturnIt() {
        // given
        given(repository.findOpenById(REQUEST_ID, NOW)).willReturn(Optional.of(sampleListing()));

        // when & then
        assertThat(service.getOpen(REQUEST_ID)).isEqualTo(sampleListing());
    }

    @Test
    void testGetOpen_givenRequestThatIsNotOpen_shouldThrowNotFound() {
        // given
        given(repository.findOpenById(MISSING_ID, NOW)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.getOpen(MISSING_ID)).isInstanceOf(ServiceRequestNotFoundException.class);
    }

    @Test
    void testListAll_givenRequests_shouldReturnAllOfThem() {
        // given
        given(repository.findAll()).willReturn(List.of(sampleRequest()));

        // when & then
        assertThat(service.listAll()).containsExactly(sampleRequest());
    }

    @Test
    void testGet_givenExistingRequest_shouldReturnIt() {
        // given
        given(repository.findById(REQUEST_ID)).willReturn(Optional.of(sampleRequest()));

        // when & then
        assertThat(service.get(REQUEST_ID)).isEqualTo(sampleRequest());
    }

    @Test
    void testGet_givenMissingRequest_shouldThrowNotFound() {
        // given
        given(repository.findById(MISSING_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.get(MISSING_ID)).isInstanceOf(ServiceRequestNotFoundException.class);
    }

    private void givenAnonymousVisitor() {
        given(currentUserProvider.current()).willReturn(Optional.empty());
    }

    private void givenLoggedInUser() {
        given(currentUserProvider.current()).willReturn(Optional.of(currentUser()));
    }

    private void givenRequiredUser() {
        given(currentUserProvider.require()).willReturn(currentUser());
    }

    private void givenNoRequiredUser() {
        given(currentUserProvider.require()).willThrow(new UnauthenticatedException());
    }

    private void givenDictionaryEntriesExist() {
        given(dictionaryPort.isCategoryActive(CATEGORY_ID)).willReturn(true);
        given(dictionaryPort.districtExists(DISTRICT_ID)).willReturn(true);
    }

    private void givenStoredRequest() {
        given(repository.create(any())).willReturn(REQUEST_ID);
        given(repository.findById(REQUEST_ID)).willReturn(Optional.of(sampleRequest()));
    }

    private static CurrentUser currentUser() {
        return new CurrentUser(ACCOUNT_ID, "anna@example.com", Set.of(AccountRole.CLIENT));
    }

    private static ClientCommand clientWithPadding() {
        return new ClientCommand(" Janina ", " Nowak ", "600123456", " ul. Lipowa ", " 12A ", " ", "20-001", DISTRICT_ID);
    }

    private static CreateServiceRequestCommand command(
            final ClientCommand client,
            final String description,
            final @Nullable Instant preferredFrom,
            final @Nullable Instant preferredTo,
            final @Nullable NotifyTarget notifyTarget
    ) {
        return new CreateServiceRequestCommand(client, CATEGORY_ID, description, preferredFrom, preferredTo, notifyTarget);
    }

    private static CreateServiceRequestCommand validCommand() {
        return command(clientWithPadding(), DESCRIPTION, null, null, null);
    }

    private static ServiceRequest sampleRequest() {
        return new ServiceRequest(
                REQUEST_ID, ServiceRequestStatus.NEW, RequestChannel.WEB_FORM, CATEGORY_ID, DESCRIPTION, null, null,
                NotifyTarget.CLIENT,
                new ServiceRequestClient("Janina", "Nowak", "+48600123456", "ul. Lipowa", "12A", null, "20-001", DISTRICT_ID),
                null, null, null, EXPIRES_AT, null, null, NOW, NOW
        );
    }

    private static ServiceRequestListing sampleListing() {
        return new ServiceRequestListing(
                REQUEST_ID, ServiceRequestStatus.NEW, CATEGORY_ID, DESCRIPTION, null, null, DISTRICT_ID,
                null, null, EXPIRES_AT, NOW
        );
    }

    private NewServiceRequest savedRequest() {
        final var captor = ArgumentCaptor.forClass(NewServiceRequest.class);
        then(repository).should().create(captor.capture());
        return captor.getValue();
    }

    private List<FieldViolation> violationsOf(final CreateServiceRequestCommand command) {
        final var exception = catchThrowableOfType(ServiceRequestValidationException.class, () -> service.create(command));
        assertThat(exception).isNotNull();
        then(repository).should(never()).create(any());
        return exception.violations();
    }
}
