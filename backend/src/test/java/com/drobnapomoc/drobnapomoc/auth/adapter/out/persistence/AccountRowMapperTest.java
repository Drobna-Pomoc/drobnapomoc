package com.drobnapomoc.drobnapomoc.auth.adapter.out.persistence;

import com.drobnapomoc.drobnapomoc.auth.domain.AccountRole;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountStatus;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class AccountRowMapperTest {

    private final AccountRowMapper mapper = new AccountRowMapper();

    @Mock
    private ResultSet rs;

    @Mock
    private Array rolesArray;

    @Test
    void shouldMapFullRowWithRolesAndTimestamps(final SoftAssertions softly) throws SQLException {
        // given
        final var createdAt = OffsetDateTime.of(2026, 3, 15, 10, 30, 0, 0, ZoneOffset.UTC);
        final var anonymizedAt = OffsetDateTime.of(2026, 4, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        given(rs.getLong("id")).willReturn(10L);
        given(rs.getString("email")).willReturn("anna@example.com");
        given(rs.getString("password_hash")).willReturn("$2a$10$hash");
        given(rs.getString("first_name")).willReturn("Anna");
        given(rs.getString("last_name")).willReturn("Kowalska");
        given(rs.getString("phone")).willReturn("+48123456789");
        given(rs.getString("status")).willReturn("SUSPENDED");
        given(rs.getArray("roles")).willReturn(rolesArray);
        given(rolesArray.getArray()).willReturn(new String[]{"CLIENT", "ADMIN"});
        given(rs.getObject("created_at", OffsetDateTime.class)).willReturn(createdAt);
        given(rs.getObject("anonymized_at", OffsetDateTime.class)).willReturn(anonymizedAt);

        // when
        final var account = mapper.mapRow(rs, 0);

        // then
        softly.assertThat(account.id()).isEqualTo(10L);
        softly.assertThat(account.email()).isEqualTo("anna@example.com");
        softly.assertThat(account.passwordHash()).isEqualTo("$2a$10$hash");
        softly.assertThat(account.firstName()).isEqualTo("Anna");
        softly.assertThat(account.lastName()).isEqualTo("Kowalska");
        softly.assertThat(account.phone()).isEqualTo("+48123456789");
        softly.assertThat(account.status()).isEqualTo(AccountStatus.SUSPENDED);
        softly.assertThat(account.roles()).containsExactlyInAnyOrder(AccountRole.CLIENT, AccountRole.ADMIN);
        softly.assertThat(account.createdAt()).isEqualTo(createdAt.toInstant());
        softly.assertThat(account.anonymizedAt()).isEqualTo(anonymizedAt.toInstant());
        then(rolesArray).should().free();
    }

    @Test
    void shouldMapEmptyRolesAndNullTimestampsWhenDatabaseValuesAreNull(final SoftAssertions softly) throws SQLException {
        // given
        given(rs.getLong("id")).willReturn(11L);
        given(rs.getString("email")).willReturn("jan@example.com");
        given(rs.getString("password_hash")).willReturn("hash");
        given(rs.getString("first_name")).willReturn("Jan");
        given(rs.getString("last_name")).willReturn("Nowak");
        given(rs.getString("phone")).willReturn("+48987654321");
        given(rs.getString("status")).willReturn("ACTIVE");
        given(rs.getArray("roles")).willReturn(null);
        given(rs.getObject("created_at", OffsetDateTime.class)).willReturn(null);
        given(rs.getObject("anonymized_at", OffsetDateTime.class)).willReturn(null);

        // when
        final var account = mapper.mapRow(rs, 0);

        // then
        softly.assertThat(account.id()).isEqualTo(11L);
        softly.assertThat(account.status()).isEqualTo(AccountStatus.ACTIVE);
        softly.assertThat(account.roles()).isEmpty();
        softly.assertThat(account.createdAt()).isNull();
        softly.assertThat(account.anonymizedAt()).isNull();
    }
}
