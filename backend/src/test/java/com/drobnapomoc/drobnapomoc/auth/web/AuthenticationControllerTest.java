package com.drobnapomoc.drobnapomoc.auth.web;

import com.drobnapomoc.drobnapomoc.api.AuthenticationApiController;
import com.drobnapomoc.drobnapomoc.api.model.LoginInputRest;
import com.drobnapomoc.drobnapomoc.api.model.RegisterInputRest;
import com.drobnapomoc.drobnapomoc.auth.application.command.CurrentUserDto;
import com.drobnapomoc.drobnapomoc.auth.application.command.LoginUserCommand;
import com.drobnapomoc.drobnapomoc.auth.application.command.RegisterUserCommand;
import com.drobnapomoc.drobnapomoc.auth.application.port.in.GetCurrentUserUseCase;
import com.drobnapomoc.drobnapomoc.auth.application.port.in.LoginUserUseCase;
import com.drobnapomoc.drobnapomoc.auth.application.port.in.LogoutUserUseCase;
import com.drobnapomoc.drobnapomoc.auth.application.port.in.RegisterUserUseCase;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountRole;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.AccountSuspendedException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.EmailAlreadyTakenException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.InvalidCredentialsException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.UnauthenticatedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private RegisterUserUseCase registerUserUseCase;

    @Mock
    private LoginUserUseCase loginUserUseCase;

    @Mock
    private LogoutUserUseCase logoutUserUseCase;

    @Mock
    private GetCurrentUserUseCase getCurrentUserUseCase;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        final var authWebMapper = Mappers.getMapper(AuthWebMapper.class);
        final var delegate = new AuthenticationApiDelegateImpl(
                registerUserUseCase,
                loginUserUseCase,
                logoutUserUseCase,
                getCurrentUserUseCase,
                authWebMapper
        );
        final var controller = new AuthenticationApiController(delegate);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /v1/auth/register powinna zwrócić 201 i zarejestrowanego użytkownika")
    void shouldRegisterAndReturnCreated() throws Exception {
        // given
        final var input = new RegisterInputRest(
                "anna.kowalska@example.com",
                "Zielony-Rower-42",
                "Anna",
                "Kowalska",
                "+48123456789"
        );

        final var currentUserDto = new CurrentUserDto(
                123L,
                "anna.kowalska@example.com",
                "Anna",
                "Kowalska",
                "+48123456789",
                Set.of(AccountRole.CLIENT)
        );

        when(registerUserUseCase.register(any(RegisterUserCommand.class))).thenReturn(currentUserDto);

        // when & then
        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(123))
                .andExpect(jsonPath("$.email").value("anna.kowalska@example.com"))
                .andExpect(jsonPath("$.firstName").value("Anna"))
                .andExpect(jsonPath("$.lastName").value("Kowalska"))
                .andExpect(jsonPath("$.phone").value("+48123456789"))
                .andExpect(jsonPath("$.roles[0]").value("CLIENT"));
    }

    @Test
    @DisplayName("POST /v1/auth/register powinna zwrócić 409 gdy email jest zajęty")
    void shouldReturnConflictWhenEmailTaken() throws Exception {
        // given
        final var input = new RegisterInputRest(
                "anna.kowalska@example.com",
                "Zielony-Rower-42",
                "Anna",
                "Kowalska",
                "+48123456789"
        );

        when(registerUserUseCase.register(any(RegisterUserCommand.class)))
                .thenThrow(new EmailAlreadyTakenException("anna.kowalska@example.com"));

        // when & then
        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_TAKEN"))
                .andExpect(jsonPath("$.message").value("Konto z tym adresem email już istnieje."));
    }

    @Test
    @DisplayName("POST /v1/auth/login powinna zwrócić 200 i zalogowanego użytkownika")
    void shouldLoginAndReturnCurrentUser() throws Exception {
        // given
        final var input = new LoginInputRest("anna.kowalska@example.com", "Zielony-Rower-42");
        final var currentUserDto = new CurrentUserDto(
                123L,
                "anna.kowalska@example.com",
                "Anna",
                "Kowalska",
                "+48123456789",
                Set.of(AccountRole.CLIENT)
        );

        when(loginUserUseCase.login(any(LoginUserCommand.class))).thenReturn(currentUserDto);

        // when & then
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(123))
                .andExpect(jsonPath("$.email").value("anna.kowalska@example.com"));
    }

    @Test
    @DisplayName("POST /v1/auth/login powinna zwrócić 401 przy błędnych danych logowania")
    void shouldReturnUnauthorizedWhenInvalidCredentials() throws Exception {
        // given
        final var input = new LoginInputRest("anna.kowalska@example.com", "ZleHaslo");
        when(loginUserUseCase.login(any(LoginUserCommand.class)))
                .thenThrow(new InvalidCredentialsException());

        // when & then
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Niepoprawny email lub hasło."));
    }

    @Test
    @DisplayName("POST /v1/auth/login powinna zwrócić 403 gdy konto jest zawieszone")
    void shouldReturnForbiddenWhenAccountSuspended() throws Exception {
        // given
        final var input = new LoginInputRest("zawieszony@example.com", "Zielony-Rower-42");
        when(loginUserUseCase.login(any(LoginUserCommand.class)))
                .thenThrow(new AccountSuspendedException());

        // when & then
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_SUSPENDED"))
                .andExpect(jsonPath("$.message").value("Konto zostało zawieszone."));
    }

    @Test
    @DisplayName("POST /v1/auth/logout powinna zwrócić 204 No Content")
    void shouldLogoutAndReturnNoContent() throws Exception {
        // given
        doNothing().when(logoutUserUseCase).logout();

        // when & then
        mockMvc.perform(post("/v1/auth/logout"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /v1/auth/me powinna zwrócić 200 i dane zalogowanego użytkownika")
    void shouldGetCurrentUser() throws Exception {
        // given
        final var currentUserDto = new CurrentUserDto(
                123L,
                "anna.kowalska@example.com",
                "Anna",
                "Kowalska",
                "+48123456789",
                Set.of(AccountRole.CLIENT)
        );

        when(getCurrentUserUseCase.getCurrentUser()).thenReturn(currentUserDto);

        // when & then
        mockMvc.perform(get("/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(123))
                .andExpect(jsonPath("$.email").value("anna.kowalska@example.com"));
    }

    @Test
    @DisplayName("GET /v1/auth/me powinna zwrócić 401 gdy użytkownik nie jest zalogowany")
    void shouldReturnUnauthorizedWhenUnauthenticated() throws Exception {
        // given
        when(getCurrentUserUseCase.getCurrentUser()).thenThrow(new UnauthenticatedException());

        // when & then
        mockMvc.perform(get("/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.message").value("Wymagane zalogowanie."));
    }
}
