package com.drobnapomoc.drobnapomoc.servicerequest.adapter.out.persistence;

import com.drobnapomoc.drobnapomoc.servicerequest.domain.NotifyTarget;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.RequestChannel;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequest;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequestClient;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequestStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import static com.drobnapomoc.drobnapomoc.shared.mapping.MappingTool.toInstant;

@Component
class ServiceRequestRowMapper implements RowMapper<ServiceRequest> {

    @Override
    public ServiceRequest mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        return new ServiceRequest(
                rs.getLong("id"),
                ServiceRequestStatus.valueOf(rs.getString("status")),
                RequestChannel.valueOf(rs.getString("channel")),
                rs.getLong("category_id"),
                rs.getString("description"),
                toInstant(rs.getObject("preferred_from", OffsetDateTime.class)),
                toInstant(rs.getObject("preferred_to", OffsetDateTime.class)),
                NotifyTarget.valueOf(rs.getString("notify_target")),
                mapClient(rs),
                rs.getObject("estimate_min_minor", Long.class),
                rs.getObject("estimate_max_minor", Long.class),
                toInstant(rs.getObject("estimated_at", OffsetDateTime.class)),
                rs.getObject("expires_at", OffsetDateTime.class).toInstant(),
                rs.getObject("created_by", Long.class),
                rs.getString("cancel_reason"),
                rs.getObject("created_at", OffsetDateTime.class).toInstant(),
                rs.getObject("updated_at", OffsetDateTime.class).toInstant()
        );
    }

    private static ServiceRequestClient mapClient(final ResultSet rs) throws SQLException {
        return new ServiceRequestClient(
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("phone"),
                rs.getString("street"),
                rs.getString("house_number"),
                rs.getString("apartment_number"),
                rs.getString("postal_code"),
                rs.getLong("district_id")
        );
    }
}
