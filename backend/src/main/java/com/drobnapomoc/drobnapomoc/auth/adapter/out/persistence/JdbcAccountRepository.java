package com.drobnapomoc.drobnapomoc.auth.adapter.out.persistence;

import com.drobnapomoc.drobnapomoc.auth.application.port.out.AccountRepository;
import com.drobnapomoc.drobnapomoc.auth.domain.Account;
import com.drobnapomoc.drobnapomoc.shared.domain.AccountRole;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
class JdbcAccountRepository implements AccountRepository {

    private static final String SELECT_ACCOUNT_SQL = """
            SELECT a.id, a.email, a.password_hash, a.first_name, a.last_name, a.phone,
                   a.status, a.created_at, a.anonymized_at,
                   COALESCE(array_agg(ar.role::text) FILTER (WHERE ar.role IS NOT NULL), '{}') AS roles
            FROM account a
            LEFT JOIN account_role ar ON ar.account_id = a.id
            """;

    private static final String PARAM_ID = "id";
    private static final String PARAM_ACCOUNT_ID = "accountId";
    private static final String PARAM_EMAIL = "email";
    private static final String PARAM_PASSWORD_HASH = "passwordHash";
    private static final String PARAM_FIRST_NAME = "firstName";
    private static final String PARAM_LAST_NAME = "lastName";
    private static final String PARAM_PHONE = "phone";
    private static final String PARAM_STATUS = "status";
    private static final String PARAM_ANONYMIZED_AT = "anonymizedAt";
    private static final String PARAM_ROLES = "roles";

    private final JdbcClient jdbcClient;
    private final AccountRowMapper accountRowMapper;

    @Override
    public Optional<Account> findById(final long id) {
        return jdbcClient.sql(SELECT_ACCOUNT_SQL + """
                        WHERE a.id = :id
                        GROUP BY a.id
                        """)
                .param(PARAM_ID, id)
                .query(accountRowMapper)
                .optional();
    }

    @Override
    public Optional<Account> findByEmail(final String email) {
        return jdbcClient.sql(SELECT_ACCOUNT_SQL + """
                        WHERE lower(a.email) = lower(:email)
                        GROUP BY a.id
                        """)
                .param(PARAM_EMAIL, email.trim())
                .query(accountRowMapper)
                .optional();
    }

    @Override
    public boolean existsByEmail(final String email) {
        return jdbcClient.sql("SELECT EXISTS(SELECT 1 FROM account WHERE lower(email) = lower(:email))")
                .param(PARAM_EMAIL, email.trim())
                .query(Boolean.class)
                .single();
    }

    @Override
    public long insert(final Account account) {
        Objects.requireNonNull(account, "Account cannot be null");

        final var id = jdbcClient.sql("""
                        INSERT INTO account (
                            email, password_hash, first_name, last_name, phone, status
                        )
                        VALUES (
                            :email, :passwordHash, :firstName, :lastName, :phone, CAST(:status AS account_status)
                        )
                        RETURNING id
                        """)
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

        final var anonymizedAt = account.anonymizedAt() != null
                ? account.anonymizedAt().atOffset(ZoneOffset.UTC)
                : null;

        final var updated = jdbcClient.sql("""
                        UPDATE account
                        SET email = :email,
                            password_hash = :passwordHash,
                            first_name = :firstName,
                            last_name = :lastName,
                            phone = :phone,
                            status = CAST(:status AS account_status),
                            anonymized_at = :anonymizedAt
                        WHERE id = :id
                        """)
                .param(PARAM_ID, account.id())
                .param(PARAM_EMAIL, account.email())
                .param(PARAM_PASSWORD_HASH, account.passwordHash())
                .param(PARAM_FIRST_NAME, account.firstName())
                .param(PARAM_LAST_NAME, account.lastName())
                .param(PARAM_PHONE, account.phone())
                .param(PARAM_STATUS, account.status().name())
                .param(PARAM_ANONYMIZED_AT, anonymizedAt, Types.TIMESTAMP_WITH_TIMEZONE)
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
        if (roles.isEmpty()) {
            return;
        }

        final var roleNames = roles.stream()
                .map(AccountRole::name)
                .toArray(String[]::new);

        jdbcClient.sql("""
                        INSERT INTO account_role (account_id, role)
                        SELECT :accountId, CAST(role AS account_role_type)
                        FROM unnest(CAST(:roles AS text[])) AS role
                        """)
                .param(PARAM_ACCOUNT_ID, accountId)
                .param(PARAM_ROLES, roleNames)
                .update();
    }
}
