package com.drobnapomoc.drobnapomoc.auth.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Account - testy modelu domenowego")
class AccountTest {

    private static final Instant NOW = Instant.parse("2026-10-20T12:00:00Z");
    private static final String EMAIL = "anna@example.com";
    private static final String PASSWORD_HASH = "hashed";
    private static final String FIRST_NAME = "Anna";
    private static final String LAST_NAME = "Kowalska";
    private static final String PHONE = "+48123456789";

    @Nested
    @DisplayName("Tworzenie konta")
    class CreationTests {

        @Test
        @DisplayName("Powinno utworzyć aktywnego klienta z rolą CLIENT za pomocą createClient")
        void shouldCreateClientCorrectly() {
            // given
            final var email = "anna.kowalska@example.com";
            final var passwordHash = "hashedPass";

            // when
            final var account = Account.createClient(email, passwordHash, FIRST_NAME, LAST_NAME, PHONE);

            // then
            assertThat(account.id()).isNull();
            assertThat(account.email()).isEqualTo(email);
            assertThat(account.passwordHash()).isEqualTo(passwordHash);
            assertThat(account.firstName()).isEqualTo(FIRST_NAME);
            assertThat(account.lastName()).isEqualTo(LAST_NAME);
            assertThat(account.phone()).isEqualTo(PHONE);
            assertThat(account.status()).isEqualTo(AccountStatus.ACTIVE);
            assertThat(account.roles()).containsExactly(AccountRole.CLIENT);
            assertThat(account.createdAt()).isNull();
            assertThat(account.anonymizedAt()).isNull();
        }

        @Test
        @DisplayName("Powinno zapewnić głęboką niemutowalność zbioru ról")
        void shouldEnsureRolesImmutability() {
            // given
            final var mutableRoles = new HashSet<AccountRole>();
            mutableRoles.add(AccountRole.CLIENT);

            // when
            final var account = new Account(
                    1L,
                    EMAIL,
                    PASSWORD_HASH,
                    FIRST_NAME,
                    LAST_NAME,
                    PHONE,
                    AccountStatus.ACTIVE,
                    mutableRoles,
                    NOW,
                    null
            );
            mutableRoles.add(AccountRole.ADMIN);
            final var accountRoles = account.roles();

            // then
            assertThat(accountRoles).containsExactly(AccountRole.CLIENT);
            assertThatThrownBy(() -> accountRoles.add(AccountRole.ADMIN))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Walidacja niezmienników")
    class ValidationTests {

        @ParameterizedTest
        @EmptySource
        @ValueSource(strings = {"   ", "\t\n"})
        @DisplayName("Powinno odrzucić pusty lub biały email")
        void shouldRejectBlankEmail(final String blankEmail) {
            // given
            final var roles = Set.of(AccountRole.CLIENT);

            // when & then
            assertThatThrownBy(() -> new Account(
                    1L,
                    blankEmail,
                    PASSWORD_HASH,
                    FIRST_NAME,
                    LAST_NAME,
                    PHONE,
                    AccountStatus.ACTIVE,
                    roles,
                    NOW,
                    null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email cannot be blank");
        }

        @ParameterizedTest
        @EmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Powinno odrzucić puste lub białe imię")
        void shouldRejectBlankFirstName(final String blankFirstName) {
            // given
            final var roles = Set.of(AccountRole.CLIENT);

            // when & then
            assertThatThrownBy(() -> new Account(
                    1L,
                    EMAIL,
                    PASSWORD_HASH,
                    blankFirstName,
                    LAST_NAME,
                    PHONE,
                    AccountStatus.ACTIVE,
                    roles,
                    NOW,
                    null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("FirstName cannot be blank");
        }

        @ParameterizedTest
        @EmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Powinno odrzucić puste lub białe nazwisko")
        void shouldRejectBlankLastName(final String blankLastName) {
            // given
            final var roles = Set.of(AccountRole.CLIENT);

            // when & then
            assertThatThrownBy(() -> new Account(
                    1L,
                    EMAIL,
                    PASSWORD_HASH,
                    FIRST_NAME,
                    blankLastName,
                    PHONE,
                    AccountStatus.ACTIVE,
                    roles,
                    NOW,
                    null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("LastName cannot be blank");
        }

        @ParameterizedTest
        @EmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Powinno odrzucić pusty lub biały numer telefonu")
        void shouldRejectBlankPhone(final String blankPhone) {
            // given
            final var roles = Set.of(AccountRole.CLIENT);

            // when & then
            assertThatThrownBy(() -> new Account(
                    1L,
                    EMAIL,
                    PASSWORD_HASH,
                    FIRST_NAME,
                    LAST_NAME,
                    blankPhone,
                    AccountStatus.ACTIVE,
                    roles,
                    NOW,
                    null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Phone cannot be blank");
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        @DisplayName("Powinno rzucić NullPointerException gdy wymagane pola są null")
        void shouldRejectNullRequiredFields() {
            // given
            final var roles = Set.of(AccountRole.CLIENT);

            // when & then
            assertThatThrownBy(() -> new Account(1L, null, PASSWORD_HASH, FIRST_NAME, LAST_NAME, PHONE, AccountStatus.ACTIVE, roles, NOW, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Email cannot be null");

            assertThatThrownBy(() -> new Account(1L, EMAIL, null, FIRST_NAME, LAST_NAME, PHONE, AccountStatus.ACTIVE, roles, NOW, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("PasswordHash cannot be null");

            assertThatThrownBy(() -> new Account(1L, EMAIL, PASSWORD_HASH, null, LAST_NAME, PHONE, AccountStatus.ACTIVE, roles, NOW, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("FirstName cannot be null");

            assertThatThrownBy(() -> new Account(1L, EMAIL, PASSWORD_HASH, FIRST_NAME, null, PHONE, AccountStatus.ACTIVE, roles, NOW, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("LastName cannot be null");

            assertThatThrownBy(() -> new Account(1L, EMAIL, PASSWORD_HASH, FIRST_NAME, LAST_NAME, null, AccountStatus.ACTIVE, roles, NOW, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Phone cannot be null");

            assertThatThrownBy(() -> new Account(1L, EMAIL, PASSWORD_HASH, FIRST_NAME, LAST_NAME, PHONE, null, roles, NOW, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Status cannot be null");

            assertThatThrownBy(() -> new Account(1L, EMAIL, PASSWORD_HASH, FIRST_NAME, LAST_NAME, PHONE, AccountStatus.ACTIVE, null, NOW, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Roles cannot be null");
        }
    }

    @Nested
    @DisplayName("Status i role konta")
    class BehaviorTests {

        @Test
        @DisplayName("Powinno poprawnie rozpoznawać status ACTIVE i SUSPENDED")
        void shouldCheckAccountStatus() {
            // given
            final var activeAccount = Account.createClient(EMAIL, PASSWORD_HASH, FIRST_NAME, LAST_NAME, PHONE);
            final var suspendedAccount = new Account(
                    2L,
                    "suspended@example.com",
                    PASSWORD_HASH,
                    "Jan",
                    "Nowak",
                    "+48987654321",
                    AccountStatus.SUSPENDED,
                    Set.of(AccountRole.CLIENT),
                    NOW,
                    null
            );

            // when & then
            assertThat(activeAccount.isActive()).isTrue();
            assertThat(activeAccount.isSuspended()).isFalse();
            assertThat(suspendedAccount.isActive()).isFalse();
            assertThat(suspendedAccount.isSuspended()).isTrue();
        }

        @Test
        @DisplayName("Powinno poprawnie sprawdzać posiadane role")
        void shouldCheckRoles() {
            // given
            final var account = new Account(
                    1L,
                    EMAIL,
                    PASSWORD_HASH,
                    FIRST_NAME,
                    LAST_NAME,
                    PHONE,
                    AccountStatus.ACTIVE,
                    Set.of(AccountRole.CLIENT, AccountRole.ADMIN),
                    NOW,
                    null
            );

            // when & then
            assertThat(account.hasRole(AccountRole.CLIENT)).isTrue();
            assertThat(account.hasRole(AccountRole.ADMIN)).isTrue();
            assertThat(account.hasRole(AccountRole.CONTRACTOR)).isFalse();
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        @DisplayName("Powinno rzucić NullPointerException gdy przekazana rola do hasRole jest null")
        void shouldRejectNullRoleInHasRole() {
            // given
            final var account = Account.createClient(EMAIL, PASSWORD_HASH, FIRST_NAME, LAST_NAME, PHONE);

            // when & then
            assertThatThrownBy(() -> account.hasRole(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Role cannot be null");
        }
    }
}
