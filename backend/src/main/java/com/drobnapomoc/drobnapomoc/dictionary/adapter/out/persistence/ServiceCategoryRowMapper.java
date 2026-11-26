package com.drobnapomoc.drobnapomoc.dictionary.adapter.out.persistence;

import com.drobnapomoc.drobnapomoc.dictionary.domain.ServiceCategory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
class ServiceCategoryRowMapper implements RowMapper<ServiceCategory> {

    @Override
    public ServiceCategory mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        return new ServiceCategory(
                rs.getLong("id"),
                rs.getString("name")
        );
    }
}
