package com.drobnapomoc.drobnapomoc.auth.application.port.out;

import com.drobnapomoc.drobnapomoc.auth.domain.Account;

public interface SessionManagerPort {

    void createSession(Account account);

    void invalidateSession();

    long getAuthenticatedAccountId();
}
