package com.drobnapomoc.drobnapomoc.shared.domain;

/**
 * Roles assigned to a user account. Lives in the shared domain kernel because the session principal
 * ({@code shared.security.UserPrincipal}) and every module's access rules use it, not only {@code auth}.
 *
 * <p>Constant names must stay strictly 1:1 in sync with:
 * <ul>
 *     <li>the {@code AccountRole} schema in {@code api/auth.yaml} (generated {@code AccountRoleRest}
 *         enum mapped by {@code AuthWebMapper}),</li>
 *     <li>the PostgreSQL {@code account_role_type} enum defined in the database migration ({@code V1__init.sql}).</li>
 * </ul>
 */
public enum AccountRole {
    CLIENT,
    CAREGIVER,
    CONTRACTOR,
    CONSULTANT,
    ADMIN
}
