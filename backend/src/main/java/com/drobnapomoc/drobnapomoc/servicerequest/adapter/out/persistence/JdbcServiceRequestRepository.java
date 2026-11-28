package com.drobnapomoc.drobnapomoc.servicerequest.adapter.out.persistence;

import com.drobnapomoc.drobnapomoc.servicerequest.application.port.out.ServiceRequestRepository;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.NewServiceRequest;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequest;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequestListing;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.drobnapomoc.drobnapomoc.shared.mapping.MappingTool.toOffsetDateTime;

@Repository
@RequiredArgsConstructor
class JdbcServiceRequestRepository implements ServiceRequestRepository {

    private static final String INSERT_CLIENT_SQL = """
            INSERT INTO client (first_name, last_name, phone, street, house_number, apartment_number, postal_code, district_id)
            VALUES (:firstName, :lastName, :phone, :street, :houseNumber, :apartmentNumber, :postalCode, :districtId)
            RETURNING id
            """;

    private static final String INSERT_SERVICE_REQUEST_SQL = """
            INSERT INTO service_request (client_id, category_id, description, preferred_from, preferred_to,
                                         notify_target, expires_at, channel, created_by)
            VALUES (:clientId, :categoryId, :description, :preferredFrom, :preferredTo,
                    CAST(:notifyTarget AS notify_target), :expiresAt, CAST(:channel AS request_channel), :createdBy)
            RETURNING id
            """;

    private static final String SELECT_SERVICE_REQUEST = """
            SELECT r.id, r.status, r.channel, r.category_id, r.description, r.preferred_from, r.preferred_to,
                   r.notify_target, r.estimate_min_minor, r.estimate_max_minor, r.estimated_at, r.expires_at,
                   r.created_by, r.cancel_reason, r.created_at, r.updated_at,
                   c.first_name, c.last_name, c.phone, c.street, c.house_number, c.apartment_number,
                   c.postal_code, c.district_id
            FROM service_request r
            JOIN client c ON c.id = r.client_id
            """;

    /** Selects no personal data of the client on purpose: contractors see only the district. */
    private static final String SELECT_OPEN_LISTING = """
            SELECT r.id, r.status, r.category_id, r.description, r.preferred_from, r.preferred_to,
                   c.district_id, r.estimate_min_minor, r.estimate_max_minor, r.expires_at, r.created_at
            FROM service_request r
            JOIN client c ON c.id = r.client_id
            WHERE r.status IN ('NEW', 'PRICED') AND r.expires_at > :now
            """;

    private static final String NEWEST_FIRST = "ORDER BY r.created_at DESC, r.id DESC";

    private static final String SELECT_BY_ID_SQL = SELECT_SERVICE_REQUEST + "WHERE r.id = :id";
    private static final String SELECT_BY_ID_AND_CREATOR_SQL =
            SELECT_SERVICE_REQUEST + "WHERE r.id = :id AND r.created_by = :accountId";
    private static final String SELECT_BY_CREATOR_SQL =
            SELECT_SERVICE_REQUEST + "WHERE r.created_by = :accountId " + NEWEST_FIRST;
    private static final String SELECT_ALL_SQL = SELECT_SERVICE_REQUEST + NEWEST_FIRST;
    private static final String SELECT_OPEN_SQL = SELECT_OPEN_LISTING + NEWEST_FIRST;
    private static final String SELECT_OPEN_BY_ID_SQL = SELECT_OPEN_LISTING + "AND r.id = :id";

    private final JdbcClient jdbcClient;
    private final ServiceRequestRowMapper serviceRequestRowMapper;
    private final ServiceRequestListingRowMapper serviceRequestListingRowMapper;

    @Override
    public long create(final NewServiceRequest request) {
        final var client = request.client();
        final var clientId = jdbcClient.sql(INSERT_CLIENT_SQL)
                .param("firstName", client.firstName())
                .param("lastName", client.lastName())
                .param("phone", client.phone())
                .param("street", client.street())
                .param("houseNumber", client.houseNumber())
                .param("apartmentNumber", client.apartmentNumber(), Types.VARCHAR)
                .param("postalCode", client.postalCode())
                .param("districtId", client.districtId())
                .query(Long.class)
                .single();

        final var id = jdbcClient.sql(INSERT_SERVICE_REQUEST_SQL)
                .param("clientId", clientId)
                .param("categoryId", request.categoryId())
                .param("description", request.description())
                .param("preferredFrom", toOffsetDateTime(request.preferredFrom()), Types.TIMESTAMP_WITH_TIMEZONE)
                .param("preferredTo", toOffsetDateTime(request.preferredTo()), Types.TIMESTAMP_WITH_TIMEZONE)
                .param("notifyTarget", request.notifyTarget().name())
                .param("expiresAt", toOffsetDateTime(request.expiresAt()), Types.TIMESTAMP_WITH_TIMEZONE)
                .param("channel", request.channel().name())
                .param("createdBy", request.createdBy(), Types.BIGINT)
                .query(Long.class)
                .single();

        return Objects.requireNonNull(id, "Generated id cannot be null");
    }

    @Override
    public Optional<ServiceRequest> findById(final long id) {
        return jdbcClient.sql(SELECT_BY_ID_SQL)
                .param("id", id)
                .query(serviceRequestRowMapper)
                .optional();
    }

    @Override
    public Optional<ServiceRequest> findByIdAndCreator(final long id, final long accountId) {
        return jdbcClient.sql(SELECT_BY_ID_AND_CREATOR_SQL)
                .param("id", id)
                .param("accountId", accountId)
                .query(serviceRequestRowMapper)
                .optional();
    }

    @Override
    public List<ServiceRequest> findByCreator(final long accountId) {
        return jdbcClient.sql(SELECT_BY_CREATOR_SQL)
                .param("accountId", accountId)
                .query(serviceRequestRowMapper)
                .list();
    }

    @Override
    public List<ServiceRequest> findAll() {
        return jdbcClient.sql(SELECT_ALL_SQL)
                .query(serviceRequestRowMapper)
                .list();
    }

    @Override
    public List<ServiceRequestListing> findOpen(final Instant now) {
        return jdbcClient.sql(SELECT_OPEN_SQL)
                .param("now", toOffsetDateTime(now), Types.TIMESTAMP_WITH_TIMEZONE)
                .query(serviceRequestListingRowMapper)
                .list();
    }

    @Override
    public Optional<ServiceRequestListing> findOpenById(final long id, final Instant now) {
        return jdbcClient.sql(SELECT_OPEN_BY_ID_SQL)
                .param("now", toOffsetDateTime(now), Types.TIMESTAMP_WITH_TIMEZONE)
                .param("id", id)
                .query(serviceRequestListingRowMapper)
                .optional();
    }
}
