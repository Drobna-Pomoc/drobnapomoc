package com.drobnapomoc.drobnapomoc.auth.adapter.in.web;

import com.drobnapomoc.drobnapomoc.api.AuthenticationApiController;
import com.drobnapomoc.drobnapomoc.api.model.AccountRoleRest;
import com.drobnapomoc.drobnapomoc.api.model.CurrentUserRest;
import com.drobnapomoc.drobnapomoc.api.model.ErrorResponseRest;
import com.drobnapomoc.drobnapomoc.api.model.LoginInputRest;
import com.drobnapomoc.drobnapomoc.api.model.RegisterInputRest;
import com.drobnapomoc.drobnapomoc.shared.security.UserPrincipal;
import com.drobnapomoc.drobnapomoc.auth.application.command.LoginUserCommand;
import com.drobnapomoc.drobnapomoc.auth.application.command.RegisterUserCommand;
import com.drobnapomoc.drobnapomoc.auth.application.dto.CurrentUserDto;
import com.drobnapomoc.drobnapomoc.auth.application.port.in.GetCurrentUserUseCase;
import com.drobnapomoc.drobnapomoc.auth.application.port.in.LoginUserUseCase;
import com.drobnapomoc.drobnapomoc.auth.application.port.in.LogoutUserUseCase;
import com.drobnapomoc.drobnapomoc.auth.application.port.in.RegisterUserUseCase;
import com.drobnapomoc.drobnapomoc.shared.domain.AccountRole;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.AccountSuspendedException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.EmailAlreadyTakenException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.InvalidCredentialsException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.UnauthenticatedException;
import com.drobnapomoc.drobnapomoc.shared.security.RestAccessDeniedHandler;
import com.drobnapomoc.drobnapomoc.shared.security.RestAuthenticationEntryPoint;
import com.drobnapomoc.drobnapomoc.shared.security.SecurityConfig;
import com.drobnapomoc.drobnapomoc.shared.web.GlobalExceptionHandler;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

@ExtendWith(SoftAssertionsExtension.class)
@WebMvcTest(AuthenticationApiController.class)
@Import({
        AuthenticationApiDelegateImpl.class,
        AuthenticationControllerTest.MapperTestConfig.class,
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        AuthExceptionHandler.class,
        GlobalExceptionHandler.class
})
class AuthenticationControllerTest {

    @InjectSoftAssertions
    private SoftAssertions softly;

    private static final String REGISTER_PATH = "/v1/auth/register";
    private static final String LOGIN_PATH = "/v1/auth/login";
    private static final String LOGOUT_PATH = "/v1/auth/logout";
    private static final String ME_PATH = "/v1/auth/me";
    private static final String EMAIL = "anna.kowalska@example.com";
    private static final String VALID_PHRASE = "Zielony-Rower-42";
    private static final String FIRST_NAME = "Anna";
    private static final String LAST_NAME = "Kowalska";
    private static final String PHONE = "+48123456789";

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
    class RegisterEndpointTests {

        @Test
        void shouldRegisterAndReturnCreated() {
            // given
            final var input = new RegisterInputRest(EMAIL, VALID_PHRASE, FIRST_NAME, LAST_NAME, PHONE);
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
                    .convertTo(CurrentUserRest.class)
                    .satisfies(body -> {
                        softly.assertThat(body.getId()).isEqualTo(123L);
                        softly.assertThat(body.getEmail()).isEqualTo(EMAIL);
                        softly.assertThat(body.getFirstName()).isEqualTo(FIRST_NAME);
                        softly.assertThat(body.getLastName()).isEqualTo(LAST_NAME);
                        softly.assertThat(body.getPhone()).isEqualTo(PHONE);
                        softly.assertThat(body.getRoles()).containsExactly(AccountRoleRest.CLIENT);
                    });

            then(registerUserUseCase).should().register(
                    new RegisterUserCommand(EMAIL, VALID_PHRASE, FIRST_NAME, LAST_NAME, PHONE)
            );
        }

        @Test
        void shouldReturnConflictWhenEmailTaken() {
            // given
            final var input = new RegisterInputRest(EMAIL, VALID_PHRASE, FIRST_NAME, LAST_NAME, PHONE);
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
                    .convertTo(ErrorResponseRest.class)
                    .satisfies(error -> {
                        softly.assertThat(error.getCode()).isEqualTo("EMAIL_TAKEN");
                        softly.assertThat(error.getMessage()).isEqualTo("Konto z tym adresem email już istnieje.");
                    });
        }

        @Test
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
                    .convertTo(ErrorResponseRest.class)
                    .satisfies(error -> {
                        softly.assertThat(error.getCode()).isEqualTo("VALIDATION_FAILED");
                        softly.assertThat(error.getErrors()).isNotEmpty();
                    });

            then(registerUserUseCase).should(never()).register(any());
        }

        @Test
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
                    .convertTo(ErrorResponseRest.class)
                    .satisfies(error -> assertThat(error.getCode()).isEqualTo("MALFORMED_REQUEST"));
        }
    }

    @Nested
    class LoginEndpointTests {

        @Test
        void shouldLoginAndReturnCurrentUser() {
            // given
            final var input = new LoginInputRest(EMAIL, VALID_PHRASE);
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
                    .convertTo(CurrentUserRest.class)
                    .satisfies(body -> {
                        softly.assertThat(body.getId()).isEqualTo(123L);
                        softly.assertThat(body.getEmail()).isEqualTo(EMAIL);
                        softly.assertThat(body.getRoles()).containsExactly(AccountRoleRest.CLIENT);
                    });

            then(loginUserUseCase).should().login(new LoginUserCommand(EMAIL, VALID_PHRASE));
        }

        @Test
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
                    .convertTo(ErrorResponseRest.class)
                    .satisfies(error -> {
                        softly.assertThat(error.getCode()).isEqualTo("INVALID_CREDENTIALS");
                        softly.assertThat(error.getMessage()).isEqualTo("Niepoprawny email lub hasło.");
                    });
        }

        @Test
        void shouldReturnForbiddenWhenAccountSuspended() {
            // given
            final var input = new LoginInputRest("zawieszony@example.com", VALID_PHRASE);
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
                    .convertTo(ErrorResponseRest.class)
                    .satisfies(error -> {
                        softly.assertThat(error.getCode()).isEqualTo("ACCOUNT_SUSPENDED");
                        softly.assertThat(error.getMessage()).isEqualTo("Konto zostało zawieszone.");
                    });
        }
    }

    @Nested
    class LogoutEndpointTests {

        @Test
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
    class CurrentUserEndpointTests {

        @Test
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
                    .convertTo(CurrentUserRest.class)
                    .satisfies(body -> {
                        softly.assertThat(body.getId()).isEqualTo(123L);
                        softly.assertThat(body.getEmail()).isEqualTo(EMAIL);
                        softly.assertThat(body.getRoles()).containsExactly(AccountRoleRest.CLIENT);
                    });
        }

        @Test
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
                    .convertTo(ErrorResponseRest.class)
                    .satisfies(error -> {
                        softly.assertThat(error.getCode()).isEqualTo("UNAUTHENTICATED");
                        softly.assertThat(error.getMessage()).isEqualTo("Wymagane zalogowanie.");
                    });

            then(getCurrentUserUseCase).shouldHaveNoInteractions();
        }

        @Test
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
                    .convertTo(ErrorResponseRest.class)
                    .satisfies(error -> assertThat(error.getCode()).isEqualTo("UNAUTHENTICATED"));
        }
    }
}
