package com.drobnapomoc.drobnapomoc.auth.adapter.out.persistence;

import com.drobnapomoc.drobnapomoc.TestcontainersConfiguration;
import com.drobnapomoc.drobnapomoc.auth.domain.Account;
import com.drobnapomoc.drobnapomoc.shared.domain.AccountRole;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountStatus;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import({TestcontainersConfiguration.class, JdbcAccountRepository.class, AccountRowMapper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ExtendWith(SoftAssertionsExtension.class)
class JdbcAccountRepositoryTest {

    private static final String ANNA_FIRST_NAME = "Anna";
    private static final String KOWALSKA_LAST_NAME = "Kowalska";
    private static final String NOWAK_LAST_NAME = "Nowak";
    private static final String PHONE = "+48123456789";
    private static final String HASH = "hash";

    @InjectSoftAssertions
    private SoftAssertions softly;

    @Autowired
    private JdbcAccountRepository repository;

    @Nested
    class InsertAndFindByIdTests {

        @Test
        void shouldInsertAndFindById() {
            // given
            final var email = "anna.kowalska@example.com";
            final var encodedHash = "$2a$10$hash";
            final var account = new Account(
                    null,
                    email,
                    encodedHash,
                    ANNA_FIRST_NAME,
                    KOWALSKA_LAST_NAME,
                    PHONE,
                    AccountStatus.ACTIVE,
                    Set.of(AccountRole.CLIENT, AccountRole.CAREGIVER),
                    null,
                    null
            );

            // when
            final var generatedId = repository.insert(account);
            final var found = repository.findById(generatedId);

            // then
            assertThat(generatedId).isPositive();
            assertThat(found).hasValueSatisfying(loaded -> {
                softly.assertThat(loaded.id()).isEqualTo(generatedId);
                softly.assertThat(loaded.email()).isEqualTo(email);
                softly.assertThat(loaded.passwordHash()).isEqualTo(encodedHash);
                softly.assertThat(loaded.firstName()).isEqualTo(ANNA_FIRST_NAME);
                softly.assertThat(loaded.lastName()).isEqualTo(KOWALSKA_LAST_NAME);
                softly.assertThat(loaded.phone()).isEqualTo(PHONE);
                softly.assertThat(loaded.status()).isEqualTo(AccountStatus.ACTIVE);
                softly.assertThat(loaded.roles()).containsExactlyInAnyOrder(AccountRole.CLIENT, AccountRole.CAREGIVER);
                softly.assertThat(loaded.createdAt()).isNotNull();
                softly.assertThat(loaded.anonymizedAt()).isNull();
            });
        }

        @Test
        void shouldInsertAndFindAccountWithoutRoles() {
            // given
            final var account = new Account(
                    null,
                    "no.roles@example.com",
                    HASH,
                    ANNA_FIRST_NAME,
                    KOWALSKA_LAST_NAME,
                    PHONE,
                    AccountStatus.ACTIVE,
                    Set.of(),
                    null,
                    null
            );

            // when
            final var generatedId = repository.insert(account);
            final var found = repository.findById(generatedId);

            // then
            assertThat(generatedId).isPositive();
            assertThat(found).hasValueSatisfying(loaded -> {
                softly.assertThat(loaded.id()).isEqualTo(generatedId);
                softly.assertThat(loaded.roles()).isEmpty();
            });
        }

        @Test
        void shouldReturnEmptyWhenIdNotFound() {
            // given
            final var nonExistingId = 999_999L;

            // when
            final var found = repository.findById(nonExistingId);

            // then
            assertThat(found).isEmpty();
        }

        @Test
        void shouldEnforceCaseInsensitiveUniqueEmailConstraint() {
            // given
            final var first = Account.createClient("unique.user@example.com", "hash1", ANNA_FIRST_NAME, KOWALSKA_LAST_NAME, "+48111222333");
            final var duplicate = Account.createClient("UNIQUE.USER@example.com", "hash2", "Jan", NOWAK_LAST_NAME, "+48444555666");
            repository.insert(first);

            // when & then
            assertThatThrownBy(() -> repository.insert(duplicate))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    class EmailLookupTests {

        @Test
        void shouldFindByEmailCaseInsensitiveAndTrimmed() {
            // given
            final var email = "jan.nowak@example.com";
            final var account = Account.createClient(email, HASH, "Jan", NOWAK_LAST_NAME, "+48123123123");
            final var id = repository.insert(account);

            // when
            final var found = repository.findByEmail("  JAN.Nowak@EXAMPLE.com  ");

            // then
            assertThat(found).hasValueSatisfying(loaded -> {
                softly.assertThat(loaded.id()).isEqualTo(id);
                softly.assertThat(loaded.email()).isEqualTo(email);
                softly.assertThat(loaded.roles()).containsExactly(AccountRole.CLIENT);
            });
        }

        @Test
        void shouldReturnEmptyWhenEmailNotFound() {
            // given
            final var unknownEmail = "brak@example.com";

            // when
            final var found = repository.findByEmail(unknownEmail);

            // then
            assertThat(found).isEmpty();
        }

        @Test
        void shouldCheckExistsByEmailCaseInsensitively() {
            // given
            repository.insert(Account.createClient("exists@example.com", HASH, "Ewa", "Lis", "+48555666777"));

            // when
            final var existsExact = repository.existsByEmail("exists@example.com");
            final var existsMixedCase = repository.existsByEmail("  EXISTS@Example.COM ");
            final var doesNotExist = repository.existsByEmail("missing@example.com");

            // then
            softly.assertThat(existsExact).isTrue();
            softly.assertThat(existsMixedCase).isTrue();
            softly.assertThat(doesNotExist).isFalse();
        }
    }

    @Nested
    class UpdateTests {

        @Test
        void shouldUpdateAccountAndReplaceRoles() {
            // given
            final var id = repository.insert(
                    Account.createClient("anna@example.com", "oldHash", ANNA_FIRST_NAME, KOWALSKA_LAST_NAME, "+48111222333")
            );
            final var updatedEmail = "anna.updated@example.com";
            final var updatedHash = "newHash";
            final var updatedPhone = "+48999888777";
            final var anonymizedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
            final var updatedAccount = new Account(
                    id,
                    updatedEmail,
                    updatedHash,
                    "Ania",
                    NOWAK_LAST_NAME,
                    updatedPhone,
                    AccountStatus.SUSPENDED,
                    Set.of(AccountRole.CONTRACTOR, AccountRole.ADMIN),
                    null,
                    anonymizedAt
            );

            // when
            final var updated = repository.update(updatedAccount);
            final var reloaded = repository.findById(id);

            // then
            assertThat(updated).isTrue();
            assertThat(reloaded).hasValueSatisfying(loaded -> {
                softly.assertThat(loaded.email()).isEqualTo(updatedEmail);
                softly.assertThat(loaded.passwordHash()).isEqualTo(updatedHash);
                softly.assertThat(loaded.firstName()).isEqualTo("Ania");
                softly.assertThat(loaded.lastName()).isEqualTo(NOWAK_LAST_NAME);
                softly.assertThat(loaded.phone()).isEqualTo(updatedPhone);
                softly.assertThat(loaded.status()).isEqualTo(AccountStatus.SUSPENDED);
                softly.assertThat(loaded.roles()).containsExactlyInAnyOrder(AccountRole.CONTRACTOR, AccountRole.ADMIN);
                softly.assertThat(loaded.anonymizedAt()).isEqualTo(anonymizedAt);
            });
        }

        @Test
        void shouldReturnFalseWhenUpdatingNonExistingAccount() {
            // given
            final var nonExisting = new Account(
                    888_888L,
                    "ghost@example.com",
                    HASH,
                    "Ghost",
                    "User",
                    "+48000000000",
                    AccountStatus.ACTIVE,
                    Set.of(AccountRole.CLIENT),
                    Instant.now(),
                    null
            );

            // when
            final var updated = repository.update(nonExisting);

            // then
            assertThat(updated).isFalse();
        }

        @Test
        void shouldThrowWhenUpdatingAccountWithoutId() {
            // given
            final var accountWithoutId = Account.createClient("a@b.pl", HASH, ANNA_FIRST_NAME, KOWALSKA_LAST_NAME, PHONE);

            // when & then
            assertThatThrownBy(() -> repository.update(accountWithoutId))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Cannot update account without ID");
        }
    }
}
