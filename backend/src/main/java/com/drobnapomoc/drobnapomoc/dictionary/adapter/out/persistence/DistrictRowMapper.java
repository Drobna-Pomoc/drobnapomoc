package com.drobnapomoc.drobnapomoc.dictionary.adapter.out.persistence;

import com.drobnapomoc.drobnapomoc.dictionary.domain.District;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
class DistrictRowMapper implements RowMapper<District> {

    @Override
    public District mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        return new District(rs.getLong("id"), rs.getLong("city_id"), rs.getString("name"));
    }
}
