package com.drobnapomoc.drobnapomoc.auth.domain;

import com.drobnapomoc.drobnapomoc.shared.domain.AccountRole;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SoftAssertionsExtension.class)
class AccountTest {

    private static final Instant NOW = Instant.parse("2026-10-20T12:00:00Z");
    private static final String EMAIL = "anna@example.com";
    private static final String ENCODED_HASH = "hashed-value";
    private static final String FIRST_NAME = "Anna";
    private static final String LAST_NAME = "Kowalska";
    private static final String PHONE = "+48123456789";

    @InjectSoftAssertions
    private SoftAssertions softly;

    @Nested
    class CreationTests {

        @Test
        void shouldCreateClientCorrectly() {
            // given
            final var email = "anna.kowalska@example.com";
            final var encodedHash = "hashedValue";

            // when
            final var account = Account.createClient(email, encodedHash, FIRST_NAME, LAST_NAME, PHONE);

            // then
            softly.assertThat(account.id()).isNull();
            softly.assertThat(account.email()).isEqualTo(email);
            softly.assertThat(account.passwordHash()).isEqualTo(encodedHash);
            softly.assertThat(account.firstName()).isEqualTo(FIRST_NAME);
            softly.assertThat(account.lastName()).isEqualTo(LAST_NAME);
            softly.assertThat(account.phone()).isEqualTo(PHONE);
            softly.assertThat(account.status()).isEqualTo(AccountStatus.ACTIVE);
            softly.assertThat(account.roles()).containsExactly(AccountRole.CLIENT);
            softly.assertThat(account.createdAt()).isNull();
            softly.assertThat(account.anonymizedAt()).isNull();
        }

        @Test
        void shouldEnsureRolesImmutability() {
            // given
            final var mutableRoles = new HashSet<AccountRole>();
            mutableRoles.add(AccountRole.CLIENT);

            // when
            final var account = new Account(
                    1L,
                    EMAIL,
                    ENCODED_HASH,
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
    class ValidationTests {

        @ParameterizedTest
        @EmptySource
        @ValueSource(strings = {"   ", "\t\n"})
        void shouldRejectBlankEmail(final String blankEmail) {
            // given
            final var roles = Set.of(AccountRole.CLIENT);

            // when & then
            assertThatThrownBy(() -> new Account(
                    1L,
                    blankEmail,
                    ENCODED_HASH,
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
        void shouldRejectBlankFirstName(final String blankFirstName) {
            // given
            final var roles = Set.of(AccountRole.CLIENT);

            // when & then
            assertThatThrownBy(() -> new Account(
                    1L,
                    EMAIL,
                    ENCODED_HASH,
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
        void shouldRejectBlankLastName(final String blankLastName) {
            // given
            final var roles = Set.of(AccountRole.CLIENT);

            // when & then
            assertThatThrownBy(() -> new Account(
                    1L,
                    EMAIL,
                    ENCODED_HASH,
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
        void shouldRejectBlankPhone(final String blankPhone) {
            // given
            final var roles = Set.of(AccountRole.CLIENT);

            // when & then
            assertThatThrownBy(() -> new Account(
                    1L,
                    EMAIL,
                    ENCODED_HASH,
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
        void shouldRejectNullRequiredFields() {
            // given
            final var roles = Set.of(AccountRole.CLIENT);

            // when & then
            softly.assertThatThrownBy(() -> new Account(1L, null, ENCODED_HASH, FIRST_NAME, LAST_NAME, PHONE, AccountStatus.ACTIVE, roles, NOW, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Email cannot be null");

            softly.assertThatThrownBy(() -> new Account(1L, EMAIL, null, FIRST_NAME, LAST_NAME, PHONE, AccountStatus.ACTIVE, roles, NOW, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("PasswordHash cannot be null");

            softly.assertThatThrownBy(() -> new Account(1L, EMAIL, ENCODED_HASH, null, LAST_NAME, PHONE, AccountStatus.ACTIVE, roles, NOW, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("FirstName cannot be null");

            softly.assertThatThrownBy(() -> new Account(1L, EMAIL, ENCODED_HASH, FIRST_NAME, null, PHONE, AccountStatus.ACTIVE, roles, NOW, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("LastName cannot be null");

            softly.assertThatThrownBy(() -> new Account(1L, EMAIL, ENCODED_HASH, FIRST_NAME, LAST_NAME, null, AccountStatus.ACTIVE, roles, NOW, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Phone cannot be null");

            softly.assertThatThrownBy(() -> new Account(1L, EMAIL, ENCODED_HASH, FIRST_NAME, LAST_NAME, PHONE, null, roles, NOW, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Status cannot be null");

            softly.assertThatThrownBy(() -> new Account(1L, EMAIL, ENCODED_HASH, FIRST_NAME, LAST_NAME, PHONE, AccountStatus.ACTIVE, null, NOW, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Roles cannot be null");
        }
    }

    @Nested
    class BehaviorTests {

        @Test
        void shouldCheckAccountStatus() {
            // given
            final var activeAccount = Account.createClient(EMAIL, ENCODED_HASH, FIRST_NAME, LAST_NAME, PHONE);
            final var suspendedAccount = new Account(
                    2L,
                    "suspended@example.com",
                    ENCODED_HASH,
                    "Jan",
                    "Nowak",
                    "+48987654321",
                    AccountStatus.SUSPENDED,
                    Set.of(AccountRole.CLIENT),
                    NOW,
                    null
            );

            // when & then
            softly.assertThat(activeAccount.isActive()).isTrue();
            softly.assertThat(activeAccount.isSuspended()).isFalse();
            softly.assertThat(suspendedAccount.isActive()).isFalse();
            softly.assertThat(suspendedAccount.isSuspended()).isTrue();
        }

        @Test
        void shouldCheckRoles() {
            // given
            final var account = new Account(
                    1L,
                    EMAIL,
                    ENCODED_HASH,
                    FIRST_NAME,
                    LAST_NAME,
                    PHONE,
                    AccountStatus.ACTIVE,
                    Set.of(AccountRole.CLIENT, AccountRole.ADMIN),
                    NOW,
                    null
            );

            // when & then
            softly.assertThat(account.hasRole(AccountRole.CLIENT)).isTrue();
            softly.assertThat(account.hasRole(AccountRole.ADMIN)).isTrue();
            softly.assertThat(account.hasRole(AccountRole.CONTRACTOR)).isFalse();
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void shouldRejectNullRoleInHasRole() {
            // given
            final var account = Account.createClient(EMAIL, ENCODED_HASH, FIRST_NAME, LAST_NAME, PHONE);

            // when & then
            assertThatThrownBy(() -> account.hasRole(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Role cannot be null");
        }
    }
}
