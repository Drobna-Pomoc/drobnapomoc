package com.drobnapomoc.drobnapomoc.auth.application.port.in;

import com.drobnapomoc.drobnapomoc.auth.application.dto.CurrentUserDto;

public interface GetCurrentUserUseCase {

    CurrentUserDto getCurrentUser();
}
