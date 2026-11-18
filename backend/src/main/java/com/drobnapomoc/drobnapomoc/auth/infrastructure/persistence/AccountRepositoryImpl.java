package com.drobnapomoc.drobnapomoc.auth.infrastructure.persistence;

import com.drobnapomoc.drobnapomoc.auth.domain.Account;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountRepository;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountRole;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountStatus;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
@NullMarked
public class AccountRepositoryImpl implements AccountRepository {

    private static final String BASE_SELECT = """
            SELECT id, email, password_hash, first_name, last_name, phone,
                   status, created_at, anonymized_at
            FROM account
            """;

    private final JdbcClient jdbcClient;

    private record AccountRow(
            long id,
            String email,
            String passwordHash,
            String firstName,
            String lastName,
            String phone,
            AccountStatus status,
            Instant createdAt,
            Instant anonymizedAt
    ) {}

    @Override
    public Optional<Account> findById(long id) {
        var accountData = jdbcClient.sql(BASE_SELECT + " WHERE id = :id")
                .param("id", id)
                .query((rs, rowNum) -> new AccountRow(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("phone"),
                        AccountStatus.valueOf(rs.getString("status")),
                        rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant() : null,
                        rs.getTimestamp("anonymized_at") != null ? rs.getTimestamp("anonymized_at").toInstant() : null
                ))
                .optional();

        return accountData.map(this::mapToAccount);
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        var accountData = jdbcClient.sql(BASE_SELECT + " WHERE lower(email) = lower(:email)")
                .param("email", email.trim())
                .query((rs, rowNum) -> new AccountRow(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("phone"),
                        AccountStatus.valueOf(rs.getString("status")),
                        rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant() : null,
                        rs.getTimestamp("anonymized_at") != null ? rs.getTimestamp("anonymized_at").toInstant() : null
                ))
                .optional();

        return accountData.map(this::mapToAccount);
    }

    @Override
    public boolean existsByEmail(String email) {
        Long count = jdbcClient.sql("SELECT COUNT(*) FROM account WHERE lower(email) = lower(:email)")
                .param("email", email.trim())
                .query(Long.class)
                .single();

        return count != null && count > 0;
    }

    @Override
    public long insert(Account account) {
        Objects.requireNonNull(account, "Account cannot be null");

        var sql = """
                INSERT INTO account (
                    email, password_hash, first_name, last_name, phone, status
                )
                VALUES (
                    :email, :passwordHash, :firstName, :lastName, :phone, :status::account_status
                )
                RETURNING id
                """;

        Long id = jdbcClient.sql(sql)
                .param("email", account.email())
                .param("passwordHash", account.passwordHash())
                .param("firstName", account.firstName())
                .param("lastName", account.lastName())
                .param("phone", account.phone())
                .param("status", account.status().name())
                .query(Long.class)
                .single();

        Objects.requireNonNull(id, "Generated id cannot be null");

        for (AccountRole role : account.roles()) {
            jdbcClient.sql("""
                    INSERT INTO account_role (account_id, role)
                    VALUES (:accountId, :role::account_role_type)
                    """)
                    .param("accountId", id)
                    .param("role", role.name())
                    .update();
        }

        return id;
    }

    @Override
    public boolean update(Account account) {
        Objects.requireNonNull(account.id(), "Cannot update account without ID");

        var sql = """
                UPDATE account
                SET email = :email,
                    password_hash = :passwordHash,
                    first_name = :firstName,
                    last_name = :lastName,
                    phone = :phone,
                    status = :status::account_status,
                    anonymized_at = :anonymizedAt
                WHERE id = :id
                """;

        int updated = jdbcClient.sql(sql)
                .param("id", account.id())
                .param("email", account.email())
                .param("passwordHash", account.passwordHash())
                .param("firstName", account.firstName())
                .param("lastName", account.lastName())
                .param("phone", account.phone())
                .param("status", account.status().name())
                .param("anonymizedAt", account.anonymizedAt() != null ? Timestamp.from(account.anonymizedAt()) : null)
                .update();

        jdbcClient.sql("DELETE FROM account_role WHERE account_id = :accountId")
                .param("accountId", account.id())
                .update();

        for (AccountRole role : account.roles()) {
            jdbcClient.sql("""
                    INSERT INTO account_role (account_id, role)
                    VALUES (:accountId, :role::account_role_type)
                    """)
                    .param("accountId", account.id())
                    .param("role", role.name())
                    .update();
        }

        return updated > 0;
    }

    private Account mapToAccount(AccountRow row) {
        Set<AccountRole> roles = findRolesByAccountId(row.id());

        return new Account(
                row.id(),
                row.email(),
                row.passwordHash(),
                row.firstName(),
                row.lastName(),
                row.phone(),
                row.status(),
                roles,
                row.createdAt(),
                row.anonymizedAt()
        );
    }

    private Set<AccountRole> findRolesByAccountId(long accountId) {
        List<String> roleStrings = jdbcClient.sql("SELECT role FROM account_role WHERE account_id = :accountId")
                .param("accountId", accountId)
                .query(String.class)
                .list();

        return roleStrings.stream()
                .map(AccountRole::valueOf)
                .collect(Collectors.toSet());
    }
}
