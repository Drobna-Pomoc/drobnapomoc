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
import com.drobnapomoc.drobnapomoc.auth.infrastructure.security.RestAccessDeniedHandler;
import com.drobnapomoc.drobnapomoc.auth.infrastructure.security.RestAuthenticationEntryPoint;
import com.drobnapomoc.drobnapomoc.auth.infrastructure.security.SecurityConfig;
import com.drobnapomoc.drobnapomoc.auth.infrastructure.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(AuthenticationApiController.class)
@Import({
        AuthenticationApiDelegateImpl.class,
        AuthenticationControllerTest.MapperTestConfig.class,
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
@DisplayName("AuthenticationController - testy warstwy WebMVC i Security")
class AuthenticationControllerTest {

    private static final String REGISTER_PATH = "/v1/auth/register";
    private static final String LOGIN_PATH = "/v1/auth/login";
    private static final String LOGOUT_PATH = "/v1/auth/logout";
    private static final String ME_PATH = "/v1/auth/me";
    private static final String EMAIL = "anna.kowalska@example.com";
    private static final String PASSWORD = "Zielony-Rower-42";
    private static final String FIRST_NAME = "Anna";
    private static final String LAST_NAME = "Kowalska";
    private static final String PHONE = "+48123456789";
    private static final String ROLE_CLIENT_VALUE = "CLIENT";
    private static final String JSON_EMAIL_PATH = "$.email";
    private static final String JSON_ROLES_FIRST_PATH = "$.roles[0]";
    private static final String JSON_CODE_PATH = "$.code";
    private static final String JSON_MESSAGE_PATH = "$.message";

    @TestConfiguration(proxyBeanMethods = false)
    static class MapperTestConfig {
        @Bean
        AuthWebMapper authWebMapper() {
            return Mappers.getMapper(AuthWebMapper.class);
        }
    }

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;

    @MockitoBean
    private LoginUserUseCase loginUserUseCase;

    @MockitoBean
    private LogoutUserUseCase logoutUserUseCase;

    @MockitoBean
    private GetCurrentUserUseCase getCurrentUserUseCase;

    private static CurrentUserDto sampleUser() {
        return new CurrentUserDto(
                123L,
                EMAIL,
                FIRST_NAME,
                LAST_NAME,
                PHONE,
                Set.of(AccountRole.CLIENT)
        );
    }

    @Nested
    @DisplayName("POST /v1/auth/register")
    class RegisterEndpointTests {

        @Test
        @DisplayName("Powinno zwrócić 201 Created i dane zarejestrowanego użytkownika")
        void shouldRegisterAndReturnCreated() {
            // given
            final var input = new RegisterInputRest(EMAIL, PASSWORD, FIRST_NAME, LAST_NAME, PHONE);
            given(registerUserUseCase.register(any(RegisterUserCommand.class))).willReturn(sampleUser());

            // when
            final var result = mockMvc.post()
                    .uri(REGISTER_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input))
                    .exchange();

            // then
            assertThat(result)
                    .hasStatus(HttpStatus.CREATED)
                    .bodyJson()
                    .satisfies(json -> {
                        assertThat(json).extractingPath("$.id").isEqualTo(123);
                        assertThat(json).extractingPath(JSON_EMAIL_PATH).isEqualTo(EMAIL);
                        assertThat(json).extractingPath("$.firstName").isEqualTo(FIRST_NAME);
                        assertThat(json).extractingPath("$.lastName").isEqualTo(LAST_NAME);
                        assertThat(json).extractingPath("$.phone").isEqualTo(PHONE);
                        assertThat(json).extractingPath(JSON_ROLES_FIRST_PATH).isEqualTo(ROLE_CLIENT_VALUE);
                    });

            then(registerUserUseCase).should().register(
                    new RegisterUserCommand(EMAIL, PASSWORD, FIRST_NAME, LAST_NAME, PHONE)
            );
        }

        @Test
        @DisplayName("Powinno zwrócić 409 Conflict gdy email jest już zajęty")
        void shouldReturnConflictWhenEmailTaken() {
            // given
            final var input = new RegisterInputRest(EMAIL, PASSWORD, FIRST_NAME, LAST_NAME, PHONE);
            given(registerUserUseCase.register(any(RegisterUserCommand.class)))
                    .willThrow(new EmailAlreadyTakenException(EMAIL));

            // when
            final var result = mockMvc.post()
                    .uri(REGISTER_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input))
                    .exchange();

            // then
            assertThat(result)
                    .hasStatus(HttpStatus.CONFLICT)
                    .bodyJson()
                    .satisfies(json -> {
                        assertThat(json).extractingPath(JSON_CODE_PATH).isEqualTo("EMAIL_TAKEN");
                        assertThat(json).extractingPath(JSON_MESSAGE_PATH).isEqualTo("Konto z tym adresem email już istnieje.");
                    });
        }

        @Test
        @DisplayName("Powinno zwrócić 422 Unprocessable Content gdy dane wejściowe nie przechodzą walidacji")
        void shouldReturnUnprocessableEntityWhenValidationFails() {
            // given
            final var invalidInput = new RegisterInputRest(
                    "niepoprawny-email",
                    "krotkie",
                    "",
                    LAST_NAME,
                    "123"
            );

            // when
            final var result = mockMvc.post()
                    .uri(REGISTER_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidInput))
                    .exchange();

            // then
            assertThat(result)
                    .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                    .bodyJson()
                    .satisfies(json -> {
                        assertThat(json).extractingPath(JSON_CODE_PATH).isEqualTo("VALIDATION_FAILED");
                        assertThat(json).extractingPath("$.errors").asArray().isNotEmpty();
                    });

            then(registerUserUseCase).should(never()).register(any());
        }

        @Test
        @DisplayName("Powinno zwrócić 400 Bad Request dla niepoprawnego JSON")
        void shouldReturnBadRequestWhenJsonIsMalformed() {
            // given
            final var malformedJson = "{ \"email\": ";

            // when
            final var result = mockMvc.post()
                    .uri(REGISTER_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(malformedJson)
                    .exchange();

            // then
            assertThat(result)
                    .hasStatus(HttpStatus.BAD_REQUEST)
                    .bodyJson()
                    .extractingPath(JSON_CODE_PATH).isEqualTo("MALFORMED_REQUEST");
        }
    }

    @Nested
    @DisplayName("POST /v1/auth/login")
    class LoginEndpointTests {

        @Test
        @DisplayName("Powinno zwrócić 200 OK i dane zalogowanego użytkownika")
        void shouldLoginAndReturnCurrentUser() {
            // given
            final var input = new LoginInputRest(EMAIL, PASSWORD);
            given(loginUserUseCase.login(any(LoginUserCommand.class))).willReturn(sampleUser());

            // when
            final var result = mockMvc.post()
                    .uri(LOGIN_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input))
                    .exchange();

            // then
            assertThat(result)
                    .hasStatusOk()
                    .bodyJson()
                    .satisfies(json -> {
                        assertThat(json).extractingPath("$.id").isEqualTo(123);
                        assertThat(json).extractingPath(JSON_EMAIL_PATH).isEqualTo(EMAIL);
                        assertThat(json).extractingPath(JSON_ROLES_FIRST_PATH).isEqualTo(ROLE_CLIENT_VALUE);
                    });

            then(loginUserUseCase).should().login(new LoginUserCommand(EMAIL, PASSWORD));
        }

        @Test
        @DisplayName("Powinno zwrócić 401 Unauthorized przy błędnych danych logowania")
        void shouldReturnUnauthorizedWhenInvalidCredentials() {
            // given
            final var input = new LoginInputRest(EMAIL, "ZleHaslo");
            given(loginUserUseCase.login(any(LoginUserCommand.class)))
                    .willThrow(new InvalidCredentialsException());

            // when
            final var result = mockMvc.post()
                    .uri(LOGIN_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input))
                    .exchange();

            // then
            assertThat(result)
                    .hasStatus(HttpStatus.UNAUTHORIZED)
                    .bodyJson()
                    .satisfies(json -> {
                        assertThat(json).extractingPath(JSON_CODE_PATH).isEqualTo("INVALID_CREDENTIALS");
                        assertThat(json).extractingPath(JSON_MESSAGE_PATH).isEqualTo("Niepoprawny email lub hasło.");
                    });
        }

        @Test
        @DisplayName("Powinno zwrócić 403 Forbidden gdy konto jest zawieszone")
        void shouldReturnForbiddenWhenAccountSuspended() {
            // given
            final var input = new LoginInputRest("zawieszony@example.com", PASSWORD);
            given(loginUserUseCase.login(any(LoginUserCommand.class)))
                    .willThrow(new AccountSuspendedException());

            // when
            final var result = mockMvc.post()
                    .uri(LOGIN_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input))
                    .exchange();

            // then
            assertThat(result)
                    .hasStatus(HttpStatus.FORBIDDEN)
                    .bodyJson()
                    .satisfies(json -> {
                        assertThat(json).extractingPath(JSON_CODE_PATH).isEqualTo("ACCOUNT_SUSPENDED");
                        assertThat(json).extractingPath(JSON_MESSAGE_PATH).isEqualTo("Konto zostało zawieszone.");
                    });
        }
    }

    @Nested
    @DisplayName("POST /v1/auth/logout")
    class LogoutEndpointTests {

        @Test
        @DisplayName("Powinno zwrócić 204 No Content i wywołać LogoutUserUseCase")
        void shouldLogoutAndReturnNoContent() {
            // given
            willDoNothing().given(logoutUserUseCase).logout();

            // when
            final var result = mockMvc.post()
                    .uri(LOGOUT_PATH)
                    .exchange();

            // then
            assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
            then(logoutUserUseCase).should().logout();
        }
    }

    @Nested
    @DisplayName("GET /v1/auth/me")
    class CurrentUserEndpointTests {

        @Test
        @DisplayName("Powinno zwrócić 200 OK i dane zalogowanego użytkownika oraz ustawić cookie XSRF-TOKEN")
        void shouldGetCurrentUserWhenAuthenticated() {
            // given
            final var principal = new UserPrincipal(123L, EMAIL, Set.of(AccountRole.CLIENT));
            given(getCurrentUserUseCase.getCurrentUser()).willReturn(sampleUser());

            // when
            final var result = mockMvc.get()
                    .uri(ME_PATH)
                    .with(user(principal))
                    .exchange();

            // then
            assertThat(result)
                    .hasStatusOk()
                    .satisfies(mvcResult -> assertThat(mvcResult.getResponse().getCookie("XSRF-TOKEN")).isNotNull())
                    .bodyJson()
                    .satisfies(json -> {
                        assertThat(json).extractingPath("$.id").isEqualTo(123);
                        assertThat(json).extractingPath(JSON_EMAIL_PATH).isEqualTo(EMAIL);
                        assertThat(json).extractingPath(JSON_ROLES_FIRST_PATH).isEqualTo(ROLE_CLIENT_VALUE);
                    });
        }

        @Test
        @DisplayName("Powinno zwrócić 401 UNAUTHENTICATED przez Spring Security gdy żądanie nie jest uwierzytelnione")
        void shouldReturnUnauthorizedBySecurityFilterChainWhenAnonymous() {
            // given
            // brak uwierzytelnionego użytkownika w żądaniu

            // when
            final var result = mockMvc.get()
                    .uri(ME_PATH)
                    .exchange();

            // then
            assertThat(result)
                    .hasStatus(HttpStatus.UNAUTHORIZED)
                    .bodyJson()
                    .satisfies(json -> {
                        assertThat(json).extractingPath(JSON_CODE_PATH).isEqualTo("UNAUTHENTICATED");
                        assertThat(json).extractingPath(JSON_MESSAGE_PATH).isEqualTo("Wymagane zalogowanie.");
                    });

            then(getCurrentUserUseCase).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("Powinno zwrócić 401 UNAUTHENTICATED gdy use case rzuci UnauthenticatedException")
        void shouldReturnUnauthorizedWhenUseCaseThrowsUnauthenticated() {
            // given
            final var principal = new UserPrincipal(123L, EMAIL, Set.of(AccountRole.CLIENT));
            given(getCurrentUserUseCase.getCurrentUser()).willThrow(new UnauthenticatedException());

            // when
            final var result = mockMvc.get()
                    .uri(ME_PATH)
                    .with(user(principal))
                    .exchange();

            // then
            assertThat(result)
                    .hasStatus(HttpStatus.UNAUTHORIZED)
                    .bodyJson()
                    .extractingPath(JSON_CODE_PATH).isEqualTo("UNAUTHENTICATED");
        }
    }
}
