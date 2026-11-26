package com.drobnapomoc.drobnapomoc.auth.application.port.in;

import com.drobnapomoc.drobnapomoc.auth.application.dto.CurrentUserDto;
import com.drobnapomoc.drobnapomoc.auth.application.command.RegisterUserCommand;

public interface RegisterUserUseCase {

    CurrentUserDto register(RegisterUserCommand command);
}
