package com.drobnapomoc.drobnapomoc.auth.web;

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
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@NullMarked
public class AuthenticationApiDelegateImpl implements AuthenticationApiDelegate {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final LogoutUserUseCase logoutUserUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final AuthWebMapper mapper;

    @Override
    public ResponseEntity<CurrentUserRest> register(RegisterInputRest registerInputRest, HttpServletRequest servletRequest) {
        Objects.requireNonNull(registerInputRest, "Dane rejestracji nie mogą być puste");
        var command = mapper.toCommand(registerInputRest);
        var created = registerUserUseCase.register(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toRest(created));
    }

    @Override
    public ResponseEntity<CurrentUserRest> login(LoginInputRest loginInputRest, HttpServletRequest servletRequest) {
        Objects.requireNonNull(loginInputRest, "Dane logowania nie mogą być puste");
        var command = mapper.toCommand(loginInputRest);
        var user = loginUserUseCase.login(command);
        return ResponseEntity.ok(mapper.toRest(user));
    }

    @Override
    public ResponseEntity<Void> logout(HttpServletRequest servletRequest) {
        logoutUserUseCase.logout();
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<CurrentUserRest> getCurrentUser(HttpServletRequest servletRequest) {
        var user = getCurrentUserUseCase.getCurrentUser();
        return ResponseEntity.ok(mapper.toRest(user));
    }
}
