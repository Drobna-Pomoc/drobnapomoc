package com.drobnapomoc.drobnapomoc.dictionary.adapter.out.persistence;

import com.drobnapomoc.drobnapomoc.dictionary.domain.City;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Maps a city row without its districts; the repository attaches them.
 */
@Component
class CityRowMapper implements RowMapper<City> {

    @Override
    public City mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        return new City(rs.getLong("id"), rs.getString("name"), List.of());
    }
}
