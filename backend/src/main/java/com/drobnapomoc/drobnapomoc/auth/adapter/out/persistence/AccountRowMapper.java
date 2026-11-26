package com.drobnapomoc.drobnapomoc.auth.adapter.out.persistence;

import com.drobnapomoc.drobnapomoc.auth.domain.Account;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountRole;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountStatus;
import jakarta.annotation.Nullable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
class AccountRowMapper implements RowMapper<Account> {

    @Override
    public Account mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        return new Account(
                rs.getLong("id"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("phone"),
                AccountStatus.valueOf(rs.getString("status")),
                mapRoles(rs),
                getInstant(rs, "created_at"),
                getInstant(rs, "anonymized_at")
        );
    }

    private Set<AccountRole> mapRoles(final ResultSet rs) throws SQLException {
        final var sqlArray = rs.getArray("roles");
        if (sqlArray == null) {
            return Set.of();
        }
        try {
            final var rawRoles = (String[]) sqlArray.getArray();
            return Arrays.stream(rawRoles)
                    .map(AccountRole::valueOf)
                    .collect(Collectors.toUnmodifiableSet());
        } finally {
            sqlArray.free();
        }
    }

    @Nullable
    private static Instant getInstant(final ResultSet rs, final String column) throws SQLException {
        final var dateTime = rs.getObject(column, OffsetDateTime.class);
        return dateTime != null ? dateTime.toInstant() : null;
    }
}
