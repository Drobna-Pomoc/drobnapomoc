package com.drobnapomoc.drobnapomoc.auth.application.port.in;

import com.drobnapomoc.drobnapomoc.auth.application.command.CurrentUserDto;
import com.drobnapomoc.drobnapomoc.auth.application.command.LoginUserCommand;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface LoginUserUseCase {

    CurrentUserDto login(LoginUserCommand command);
}
