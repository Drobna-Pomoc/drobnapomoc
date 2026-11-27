package com.drobnapomoc.drobnapomoc.servicerequest.adapter.out.persistence;

import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequestListing;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.ServiceRequestStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import static com.drobnapomoc.drobnapomoc.shared.mapping.MappingTool.toInstant;

@Component
class ServiceRequestListingRowMapper implements RowMapper<ServiceRequestListing> {

    @Override
    public ServiceRequestListing mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        return new ServiceRequestListing(
                rs.getLong("id"),
                ServiceRequestStatus.valueOf(rs.getString("status")),
                rs.getLong("category_id"),
                rs.getString("description"),
                toInstant(rs.getObject("preferred_from", OffsetDateTime.class)),
                toInstant(rs.getObject("preferred_to", OffsetDateTime.class)),
                rs.getLong("district_id"),
                rs.getObject("estimate_min_minor", Long.class),
                rs.getObject("estimate_max_minor", Long.class),
                toInstant(rs.getObject("expires_at", OffsetDateTime.class)),
                toInstant(rs.getObject("created_at", OffsetDateTime.class))
        );
    }
}
