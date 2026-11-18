package com.drobnapomoc.drobnapomoc.auth.application.port.in;

import com.drobnapomoc.drobnapomoc.auth.application.command.CurrentUserDto;
import com.drobnapomoc.drobnapomoc.auth.application.command.RegisterUserCommand;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface RegisterUserUseCase {

    CurrentUserDto register(RegisterUserCommand command);
}
