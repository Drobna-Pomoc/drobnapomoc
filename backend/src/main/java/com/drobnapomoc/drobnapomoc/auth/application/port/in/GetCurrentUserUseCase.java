package com.drobnapomoc.drobnapomoc.auth.application.port.in;

import com.drobnapomoc.drobnapomoc.auth.application.command.CurrentUserDto;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface GetCurrentUserUseCase {

    CurrentUserDto getCurrentUser();
}
