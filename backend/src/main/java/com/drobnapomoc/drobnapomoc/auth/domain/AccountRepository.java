package com.drobnapomoc.drobnapomoc.auth.domain;

import org.jspecify.annotations.NullMarked;

import java.util.Optional;

@NullMarked
public interface AccountRepository {

    Optional<Account> findById(long id);

    Optional<Account> findByEmail(String email);

    boolean existsByEmail(String email);

    long insert(Account account);

    boolean update(Account account);
}
