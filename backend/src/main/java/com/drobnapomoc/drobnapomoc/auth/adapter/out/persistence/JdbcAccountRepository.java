package com.drobnapomoc.drobnapomoc.auth.adapter.out.persistence;

import com.drobnapomoc.drobnapomoc.auth.application.port.out.AccountRepository;
import com.drobnapomoc.drobnapomoc.auth.domain.Account;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountRole;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountStatus;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
class JdbcAccountRepository implements AccountRepository {

    private static final String PARAM_ID = "id";
    private static final String PARAM_ACCOUNT_ID = "accountId";
    private static final String PARAM_EMAIL = "email";
    private static final String PARAM_PASSWORD_HASH = "passwordHash";
    private static final String PARAM_FIRST_NAME = "firstName";
    private static final String PARAM_LAST_NAME = "lastName";
    private static final String PARAM_PHONE = "phone";
    private static final String PARAM_STATUS = "status";
    private static final String PARAM_ANONYMIZED_AT = "anonymizedAt";
    private static final String PARAM_ROLE = "role";

    private final JdbcClient jdbcClient;

    private record AccountRow(
            long id,
            String email,
            String passwordHash,
            String firstName,
            String lastName,
            String phone,
            AccountStatus status,
            @Nullable Instant createdAt,
            @Nullable Instant anonymizedAt
    ) {}

    @Override
    public Optional<Account> findById(final long id) {
        final var accountData = jdbcClient.sql("""
                        SELECT id, email, password_hash, first_name, last_name, phone,
                               status, created_at, anonymized_at
                        FROM account
                        WHERE id = :id
                        """)
                .param(PARAM_ID, id)
                .query((rs, _) -> mapRow(rs))
                .optional();

        return accountData.map(this::mapToAccount);
    }

    @Override
    public Optional<Account> findByEmail(final String email) {
        final var accountData = jdbcClient.sql("""
                        SELECT id, email, password_hash, first_name, last_name, phone,
                               status, created_at, anonymized_at
                        FROM account
                        WHERE lower(email) = lower(:email)
                        """)
                .param(PARAM_EMAIL, email.trim())
                .query((rs, _) -> mapRow(rs))
                .optional();

        return accountData.map(this::mapToAccount);
    }

    @Override
    public boolean existsByEmail(final String email) {
        final var count = jdbcClient.sql("SELECT COUNT(*) FROM account WHERE lower(email) = lower(:email)")
                .param(PARAM_EMAIL, email.trim())
                .query(Long.class)
                .single();

        return count > 0;
    }

    @Override
    public long insert(final Account account) {
        Objects.requireNonNull(account, "Account cannot be null");

        final var sql = """
                INSERT INTO account (
                    email, password_hash, first_name, last_name, phone, status
                )
                VALUES (
                    :email, :passwordHash, :firstName, :lastName, :phone, CAST(:status AS account_status)
                )
                RETURNING id
                """;

        final var id = jdbcClient.sql(sql)
                .param(PARAM_EMAIL, account.email())
                .param(PARAM_PASSWORD_HASH, account.passwordHash())
                .param(PARAM_FIRST_NAME, account.firstName())
                .param(PARAM_LAST_NAME, account.lastName())
                .param(PARAM_PHONE, account.phone())
                .param(PARAM_STATUS, account.status().name())
                .query(Long.class)
                .single();

        Objects.requireNonNull(id, "Generated id cannot be null");
        insertRoles(id, account.roles());

        return id;
    }

    @Override
    public boolean update(final Account account) {
        Objects.requireNonNull(account.id(), "Cannot update account without ID");

        final var sql = """
                UPDATE account
                SET email = :email,
                    password_hash = :passwordHash,
                    first_name = :firstName,
                    last_name = :lastName,
                    phone = :phone,
                    status = CAST(:status AS account_status),
                    anonymized_at = :anonymizedAt
                WHERE id = :id
                """;

        final var updated = jdbcClient.sql(sql)
                .param(PARAM_ID, account.id())
                .param(PARAM_EMAIL, account.email())
                .param(PARAM_PASSWORD_HASH, account.passwordHash())
                .param(PARAM_FIRST_NAME, account.firstName())
                .param(PARAM_LAST_NAME, account.lastName())
                .param(PARAM_PHONE, account.phone())
                .param(PARAM_STATUS, account.status().name())
                .param(PARAM_ANONYMIZED_AT, toTimestampOrNull(account.anonymizedAt()))
                .update();

        if (updated == 0) {
            return false;
        }

        jdbcClient.sql("DELETE FROM account_role WHERE account_id = :accountId")
                .param(PARAM_ACCOUNT_ID, account.id())
                .update();

        insertRoles(account.id(), account.roles());

        return true;
    }

    private void insertRoles(final long accountId, final Set<AccountRole> roles) {
        for (final var role : roles) {
            jdbcClient.sql("""
                    INSERT INTO account_role (account_id, role)
                    VALUES (:accountId, CAST(:role AS account_role_type))
                    """)
                    .param(PARAM_ACCOUNT_ID, accountId)
                    .param(PARAM_ROLE, role.name())
                    .update();
        }
    }

    private AccountRow mapRow(final ResultSet rs) throws SQLException {
        return new AccountRow(
                rs.getLong(PARAM_ID),
                rs.getString(PARAM_EMAIL),
                rs.getString("password_hash"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString(PARAM_PHONE),
                AccountStatus.valueOf(rs.getString(PARAM_STATUS)),
                toInstantOrNull(rs.getTimestamp("created_at")),
                toInstantOrNull(rs.getTimestamp("anonymized_at"))
        );
    }

    private static @Nullable Instant toInstantOrNull(@Nullable final Timestamp timestamp) {
        return timestamp != null ? timestamp.toInstant() : null;
    }

    private static @Nullable Timestamp toTimestampOrNull(@Nullable final Instant instant) {
        return instant != null ? Timestamp.from(instant) : null;
    }

    private Account mapToAccount(final AccountRow row) {
        final var roles = findRolesByAccountId(row.id());

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

    private Set<AccountRole> findRolesByAccountId(final long accountId) {
        final var roleStrings = jdbcClient.sql("SELECT role FROM account_role WHERE account_id = :accountId")
                .param(PARAM_ACCOUNT_ID, accountId)
                .query(String.class)
                .list();

        return roleStrings.stream()
                .map(AccountRole::valueOf)
                .collect(Collectors.toUnmodifiableSet());
    }
}
