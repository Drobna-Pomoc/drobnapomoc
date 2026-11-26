package com.drobnapomoc.drobnapomoc.auth.application.port.out;

import com.drobnapomoc.drobnapomoc.auth.domain.Account;

import java.util.Optional;

public interface AccountRepository {

    Optional<Account> findById(long id);

    Optional<Account> findByEmail(String email);

    boolean existsByEmail(String email);

    long insert(Account account);

    boolean update(Account account);
}
