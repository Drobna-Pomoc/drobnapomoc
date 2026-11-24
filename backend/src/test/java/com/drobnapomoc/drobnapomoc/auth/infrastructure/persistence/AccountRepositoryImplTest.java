package com.drobnapomoc.drobnapomoc.auth.infrastructure.persistence;

import com.drobnapomoc.drobnapomoc.TestcontainersConfiguration;
import com.drobnapomoc.drobnapomoc.auth.domain.Account;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountRole;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
@Import({TestcontainersConfiguration.class, AccountRepositoryImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("AccountRepositoryImpl - testy integracyjne z PostgreSQL i Flyway")
class AccountRepositoryImplTest {

    private static final String ANNA_FIRST_NAME = "Anna";
    private static final String KOWALSKA_LAST_NAME = "Kowalska";
    private static final String NOWAK_LAST_NAME = "Nowak";
    private static final String PHONE = "+48123456789";
    private static final String HASH = "hash";

    @Autowired
    private AccountRepositoryImpl repository;

    @Nested
    @DisplayName("Zapis i odczyt po ID")
    class InsertAndFindByIdTests {

        @Test
        @DisplayName("Powinno zapisać konto z rolami i odczytać je po wygenerowanym ID")
        void shouldInsertAndFindById() {
            // given
            final var email = "anna.kowalska@example.com";
            final var passwordHash = "$2a$10$hash";
            final var account = new Account(
                    null,
                    email,
                    passwordHash,
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
                assertThat(loaded.id()).isEqualTo(generatedId);
                assertThat(loaded.email()).isEqualTo(email);
                assertThat(loaded.passwordHash()).isEqualTo(passwordHash);
                assertThat(loaded.firstName()).isEqualTo(ANNA_FIRST_NAME);
                assertThat(loaded.lastName()).isEqualTo(KOWALSKA_LAST_NAME);
                assertThat(loaded.phone()).isEqualTo(PHONE);
                assertThat(loaded.status()).isEqualTo(AccountStatus.ACTIVE);
                assertThat(loaded.roles()).containsExactlyInAnyOrder(AccountRole.CLIENT, AccountRole.CAREGIVER);
                assertThat(loaded.createdAt()).isNotNull();
                assertThat(loaded.anonymizedAt()).isNull();
            });
        }

        @Test
        @DisplayName("Powinno zwrócić Optional.empty gdy konto o podanym ID nie istnieje")
        void shouldReturnEmptyWhenIdNotFound() {
            // given
            final var nonExistingId = 999_999L;

            // when
            final var found = repository.findById(nonExistingId);

            // then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Powinno wymusić unikalność adresu email niezależnie od wielkości liter")
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
    @DisplayName("Wyszukiwanie i sprawdzanie po email")
    class EmailLookupTests {

        @Test
        @DisplayName("Powinno odnaleźć konto po emailu niezależnie od wielkości liter i białych znaków")
        void shouldFindByEmailCaseInsensitiveAndTrimmed() {
            // given
            final var email = "jan.nowak@example.com";
            final var account = Account.createClient(email, HASH, "Jan", NOWAK_LAST_NAME, "+48123123123");
            final var id = repository.insert(account);

            // when
            final var found = repository.findByEmail("  JAN.Nowak@EXAMPLE.com  ");

            // then
            assertThat(found).hasValueSatisfying(loaded -> {
                assertThat(loaded.id()).isEqualTo(id);
                assertThat(loaded.email()).isEqualTo(email);
                assertThat(loaded.roles()).containsExactly(AccountRole.CLIENT);
            });
        }

        @Test
        @DisplayName("Powinno zwrócić Optional.empty gdy konto o podanym emailu nie istnieje")
        void shouldReturnEmptyWhenEmailNotFound() {
            // given
            final var unknownEmail = "brak@example.com";

            // when
            final var found = repository.findByEmail(unknownEmail);

            // then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Powinno zwrócić true w existsByEmail niezależnie od wielkości liter i false dla nieistniejącego")
        void shouldCheckExistsByEmailCaseInsensitively() {
            // given
            repository.insert(Account.createClient("exists@example.com", HASH, "Ewa", "Lis", "+48555666777"));

            // when
            final var existsExact = repository.existsByEmail("exists@example.com");
            final var existsMixedCase = repository.existsByEmail("  EXISTS@Example.COM ");
            final var doesNotExist = repository.existsByEmail("missing@example.com");

            // then
            assertThat(existsExact).isTrue();
            assertThat(existsMixedCase).isTrue();
            assertThat(doesNotExist).isFalse();
        }
    }

    @Nested
    @DisplayName("Aktualizacja konta")
    class UpdateTests {

        @Test
        @DisplayName("Powinno zaktualizować dane konta, status, anonymizedAt oraz role")
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
                assertThat(loaded.email()).isEqualTo(updatedEmail);
                assertThat(loaded.passwordHash()).isEqualTo(updatedHash);
                assertThat(loaded.firstName()).isEqualTo("Ania");
                assertThat(loaded.lastName()).isEqualTo(NOWAK_LAST_NAME);
                assertThat(loaded.phone()).isEqualTo(updatedPhone);
                assertThat(loaded.status()).isEqualTo(AccountStatus.SUSPENDED);
                assertThat(loaded.roles()).containsExactlyInAnyOrder(AccountRole.CONTRACTOR, AccountRole.ADMIN);
                assertThat(loaded.anonymizedAt()).isEqualTo(anonymizedAt);
            });
        }

        @Test
        @DisplayName("Powinno zwrócić false gdy aktualizowane konto nie istnieje")
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
        @DisplayName("Powinno rzucić NullPointerException przy próbie aktualizacji konta bez ID")
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
