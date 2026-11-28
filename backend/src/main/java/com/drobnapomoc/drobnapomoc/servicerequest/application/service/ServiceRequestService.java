package com.drobnapomoc.drobnapomoc.servicerequest.application.service;

import com.drobnapomoc.drobnapomoc.servicerequest.application.ServiceRequestProperties;
import com.drobnapomoc.drobnapomoc.servicerequest.application.command.ClientCommand;
import com.drobnapomoc.drobnapomoc.servicerequest.application.command.CreateServiceRequestCommand;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.in.CreateServiceRequestUseCase;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.in.GetMyServiceRequestUseCase;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.in.GetOpenServiceRequestUseCase;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.in.GetServiceRequestUseCase;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.in.ListAllServiceRequestsUseCase;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.in.ListMyServiceRequestsUseCase;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.in.ListOpenServiceRequestsUseCase;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.out.PriceEstimatorPort;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.out.ServiceRequestDictionaryPort;
import com.drobnapomoc.drobnapomoc.servicerequest.application.port.out.ServiceRequestRepository;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.NewServiceRequest;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.NotifyTarget;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.PhoneNumber;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.RequestChannel;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequest;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequestClient;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequestListing;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.exception.FieldViolation;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.exception.ServiceRequestNotFoundException;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.exception.ServiceRequestValidationException;
import com.drobnapomoc.drobnapomoc.shared.domain.CurrentUser;
import com.drobnapomoc.drobnapomoc.shared.domain.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
class ServiceRequestService implements
        CreateServiceRequestUseCase,
        ListMyServiceRequestsUseCase,
        GetMyServiceRequestUseCase,
        ListOpenServiceRequestsUseCase,
        GetOpenServiceRequestUseCase,
        ListAllServiceRequestsUseCase,
        GetServiceRequestUseCase {

    static final String EMPTY_FIELD = "Pole nie może być puste.";
    static final String DESCRIPTION_TOO_SHORT = "Opis musi mieć co najmniej 10 znaków.";
    static final String CATEGORY_INVALID = "Wybrana kategoria nie istnieje lub jest nieaktywna.";
    static final String DISTRICT_INVALID = "Wybrana dzielnica nie istnieje.";
    static final String CAREGIVER_NEEDS_SESSION = "Powiadamianie opiekuna wymaga zalogowania.";
    static final String WINDOW_REVERSED = "Koniec terminu nie może być wcześniej niż jego początek.";
    static final String WINDOW_IN_PAST = "Koniec terminu nie może być w przeszłości.";

    private static final int DESCRIPTION_MIN_LENGTH = 10;

    private final ServiceRequestRepository repository;
    private final ServiceRequestDictionaryPort dictionaryPort;
    private final CurrentUserProvider currentUserProvider;
    private final PriceEstimatorPort priceEstimator;
    private final ServiceRequestProperties properties;
    private final Clock clock;

    @Override
    public ServiceRequest create(final CreateServiceRequestCommand command) {
        var now = clock.instant();
        final var accountId = currentUserProvider.current().map(CurrentUser::id).orElse(null);
        final var notifyTarget = Objects.requireNonNullElse(command.notifyTarget(), NotifyTarget.CLIENT);
        final var description = command.description().trim();
        final var client = command.client();

        final var violations = new ArrayList<FieldViolation>();
        validateDescription(description, violations);
        validateClientText(client, violations);
        final var category = dictionaryPort.findActiveCategory(command.categoryId());
        if (category.isEmpty()) {
            violations.add(new FieldViolation("categoryId", CATEGORY_INVALID));
        }
        if (!dictionaryPort.districtExists(client.districtId())) {
            violations.add(new FieldViolation("client.districtId", DISTRICT_INVALID));
        }
        if (notifyTarget == NotifyTarget.CAREGIVER && accountId == null) {
            violations.add(new FieldViolation("notifyTarget", CAREGIVER_NEEDS_SESSION));
        }
        validateWindow(command.preferredFrom(), command.preferredTo(), now, violations);
        if (!violations.isEmpty()) {
            throw new ServiceRequestValidationException(violations);
        }

        final var estimate = priceEstimator.estimate(category.orElseThrow().name(), description).orElse(null);

        now = clock.instant();

        return repository.create(new NewServiceRequest(
                toClient(client),
                command.categoryId(),
                description,
                command.preferredFrom(),
                command.preferredTo(),
                notifyTarget,
                RequestChannel.WEB_FORM,
                accountId,
                now.plus(properties.expiry()),
                estimate,
                estimate != null ? now : null
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequest> listMine() {
        return repository.findByCreator(currentUserProvider.require().id());
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRequest getMine(final long id) {
        return repository.findByIdAndCreator(id, currentUserProvider.require().id())
                .orElseThrow(() -> new ServiceRequestNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequestListing> listOpen() {
        return repository.findOpen(clock.instant());
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRequestListing getOpen(final long id) {
        return repository.findOpenById(id, clock.instant())
                .orElseThrow(() -> new ServiceRequestNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequest> listAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRequest get(final long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ServiceRequestNotFoundException(id));
    }

    private static void validateDescription(final String description, final List<FieldViolation> violations) {
        if (description.isEmpty()) {
            violations.add(new FieldViolation("description", EMPTY_FIELD));
        } else if (description.length() < DESCRIPTION_MIN_LENGTH) {
            violations.add(new FieldViolation("description", DESCRIPTION_TOO_SHORT));
        }
    }

    private static void validateClientText(final ClientCommand client, final List<FieldViolation> violations) {
        requireText("client.firstName", client.firstName(), violations);
        requireText("client.lastName", client.lastName(), violations);
        requireText("client.street", client.street(), violations);
        requireText("client.houseNumber", client.houseNumber(), violations);
    }

    private static void requireText(final String field, final String value, final List<FieldViolation> violations) {
        if (value.isBlank()) {
            violations.add(new FieldViolation(field, EMPTY_FIELD));
        }
    }

    private static void validateWindow(
            final @Nullable Instant from,
            final @Nullable Instant to,
            final Instant now,
            final List<FieldViolation> violations
    ) {
        if (to == null) {
            return;
        }
        if (from != null && to.isBefore(from)) {
            violations.add(new FieldViolation("preferredTo", WINDOW_REVERSED));
        } else if (to.isBefore(now)) {
            violations.add(new FieldViolation("preferredTo", WINDOW_IN_PAST));
        }
    }

    private static ServiceRequestClient toClient(final ClientCommand client) {
        final var apartment = client.apartmentNumber() == null || client.apartmentNumber().isBlank()
                ? null
                : client.apartmentNumber().trim();
        return new ServiceRequestClient(
                client.firstName().trim(),
                client.lastName().trim(),
                PhoneNumber.normalize(client.phone()),
                client.street().trim(),
                client.houseNumber().trim(),
                apartment,
                client.postalCode().trim(),
                client.districtId()
        );
    }
}
