package com.drobnapomoc.drobnapomoc.auth.application.port.in;

import com.drobnapomoc.drobnapomoc.auth.application.dto.CurrentUserDto;
import com.drobnapomoc.drobnapomoc.auth.application.command.LoginUserCommand;

public interface LoginUserUseCase {

    CurrentUserDto login(LoginUserCommand command);
}
