package com.drobnapomoc.drobnapomoc.auth.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    @Test
    @DisplayName("Powinno utworzyć klienta za pomocą metody fabrycznej createClient")
    void shouldCreateClientCorrectly() {
        // when
        Account account = Account.createClient(
                "anna.kowalska@example.com",
                "hashedPass",
                "Anna",
                "Kowalska",
                "+48123456789"
        );

        // then
        assertThat(account.id()).isNull();
        assertThat(account.email()).isEqualTo("anna.kowalska@example.com");
        assertThat(account.firstName()).isEqualTo("Anna");
        assertThat(account.lastName()).isEqualTo("Kowalska");
        assertThat(account.phone()).isEqualTo("+48123456789");
        assertThat(account.status()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.roles()).containsExactly(AccountRole.CLIENT);
        assertThat(account.isActive()).isTrue();
        assertThat(account.isSuspended()).isFalse();
        assertThat(account.hasRole(AccountRole.CLIENT)).isTrue();
        assertThat(account.hasRole(AccountRole.ADMIN)).isFalse();
    }

    @Test
    @DisplayName("Powinno rzucić błąd gdy email jest pusty")
    void shouldThrowExceptionWhenEmailIsBlank() {
        assertThatThrownBy(() -> new Account(
                1L,
                "   ",
                "hashed",
                "Anna",
                "Kowalska",
                "+48123456789",
                AccountStatus.ACTIVE,
                Set.of(AccountRole.CLIENT),
                Instant.now(),
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email cannot be blank");
    }

    @Test
    @DisplayName("Powinno zapewnić niemutowalność zbioru ról")
    void shouldEnsureRolesImmutability() {
        Set<AccountRole> mutableRoles = new HashSet<>();
        mutableRoles.add(AccountRole.CLIENT);

        Account account = new Account(
                1L,
                "anna@example.com",
                "hashed",
                "Anna",
                "Kowalska",
                "+48123456789",
                AccountStatus.ACTIVE,
                mutableRoles,
                Instant.now(),
                null
        );

        // modifying original set should not affect account
        mutableRoles.add(AccountRole.ADMIN);
        assertThat(account.roles()).containsExactly(AccountRole.CLIENT);

        // modifying returned set should throw UnsupportedOperationException
        assertThatThrownBy(() -> account.roles().add(AccountRole.ADMIN))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
