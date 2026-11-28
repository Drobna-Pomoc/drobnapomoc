package com.drobnapomoc.drobnapomoc.auth.adapter.in.web;

import com.drobnapomoc.drobnapomoc.api.AuthenticationApiDelegate;
import com.drobnapomoc.drobnapomoc.api.model.CurrentUserRest;
import com.drobnapomoc.drobnapomoc.api.model.LoginInputRest;
import com.drobnapomoc.drobnapomoc.api.model.RegisterInputRest;
import com.drobnapomoc.drobnapomoc.auth.application.port.in.GetCurrentUserUseCase;
import com.drobnapomoc.drobnapomoc.auth.application.port.in.LoginUserUseCase;
import com.drobnapomoc.drobnapomoc.auth.application.port.in.LogoutUserUseCase;
import com.drobnapomoc.drobnapomoc.auth.application.port.in.RegisterUserUseCase;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AuthenticationApiDelegateImpl implements AuthenticationApiDelegate {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final LogoutUserUseCase logoutUserUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final AuthWebMapper mapper;

    @Override
    public ResponseEntity<CurrentUserRest> register(final RegisterInputRest registerInputRest, final HttpServletRequest servletRequest) {
        Objects.requireNonNull(registerInputRest, "Dane rejestracji nie mogą być puste");
        final var command = mapper.toCommand(registerInputRest);
        final var created = registerUserUseCase.register(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toRest(created));
    }

    @Override
    public ResponseEntity<CurrentUserRest> login(final LoginInputRest loginInputRest, final HttpServletRequest servletRequest) {
        Objects.requireNonNull(loginInputRest, "Dane logowania nie mogą być puste");
        final var command = mapper.toCommand(loginInputRest);
        final var user = loginUserUseCase.login(command);
        return ResponseEntity.ok(mapper.toRest(user));
    }

    @Override
    public ResponseEntity<Void> logout(final HttpServletRequest servletRequest) {
        logoutUserUseCase.logout();
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<CurrentUserRest> getCurrentUser(final HttpServletRequest servletRequest) {
        final var user = getCurrentUserUseCase.getCurrentUser();
        return ResponseEntity.ok(mapper.toRest(user));
    }
}
