package com.drobnapomoc.drobnapomoc.auth;

import com.drobnapomoc.drobnapomoc.TestcontainersConfiguration;
import com.drobnapomoc.drobnapomoc.api.model.AccountRoleRest;
import com.drobnapomoc.drobnapomoc.api.model.CurrentUserRest;
import com.drobnapomoc.drobnapomoc.api.model.ErrorResponseRest;
import com.drobnapomoc.drobnapomoc.api.model.LoginInputRest;
import com.drobnapomoc.drobnapomoc.api.model.RegisterInputRest;
import com.drobnapomoc.drobnapomoc.auth.application.port.out.AccountRepository;
import com.drobnapomoc.drobnapomoc.auth.domain.Account;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountStatus;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SoftAssertionsExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
class AuthIntegrationTest {

    private static final String REGISTER_PATH = "/v1/auth/register";
    private static final String LOGIN_PATH = "/v1/auth/login";
    private static final String LOGOUT_PATH = "/v1/auth/logout";
    private static final String ME_PATH = "/v1/auth/me";
    private static final String VALID_PHRASE = "Zielony-Rower-42";
    private static final String LAST_NAME = "Kowalska";
    private static final String PHONE = "+48123456789";
    private static final String ANNA_EMAIL = "anna@example.com";
    private static final String SESSION_COOKIE = "SESSION";
    private static final String SESSION_COOKIE_PREFIX = "SESSION=";
    private static final String XSRF_COOKIE = "XSRF-TOKEN";

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void cleanDatabase() {
        jdbcClient.sql("TRUNCATE TABLE account_role, account RESTART IDENTITY CASCADE").update();
    }

    private static String extractCookieValue(final List<String> setCookieHeaders, final String cookieName) {
        return setCookieHeaders.stream()
                .filter(header -> header.startsWith(cookieName + "="))
                .map(header -> header.substring((cookieName + "=").length()).split(";", 2)[0])
                .findFirst()
                .orElseThrow(() -> new AssertionError("Brak ciasteczka " + cookieName + " w nagłówkach: " + setCookieHeaders));
    }

    @Test
    void shouldCompleteFullAuthenticationLifecycle(final SoftAssertions softly) {
        // given
        final var registerInput = new RegisterInputRest(
                "Anna.Kowalska@Example.COM",
                VALID_PHRASE,
                " Anna ",
                " " + LAST_NAME + " ",
                PHONE
        );

        // when
        final var registerResult = restTestClient.post()
                .uri(REGISTER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(registerInput)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CurrentUserRest.class)
                .returnResult();

        // then
        final var registeredUser = Objects.requireNonNull(registerResult.getResponseBody());
        softly.assertThat(registeredUser.getEmail()).isEqualTo("anna.kowalska@example.com");
        softly.assertThat(registeredUser.getFirstName()).isEqualTo("Anna");
        softly.assertThat(registeredUser.getLastName()).isEqualTo(LAST_NAME);
        softly.assertThat(registeredUser.getRoles()).containsExactly(AccountRoleRest.CLIENT);

        final var registerCookies = registerResult.getResponseHeaders().getOrEmpty(HttpHeaders.SET_COOKIE);
        final var sessionId = extractCookieValue(registerCookies, SESSION_COOKIE);

        // when
        final var meResult = restTestClient.get()
                .uri(ME_PATH)
                .header(HttpHeaders.COOKIE, SESSION_COOKIE_PREFIX + sessionId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CurrentUserRest.class)
                .returnResult();

        // then
        final var meUser = Objects.requireNonNull(meResult.getResponseBody());
        final var xsrfCookies = meResult.getResponseHeaders().getOrEmpty(HttpHeaders.SET_COOKIE);
        final var xsrfToken = extractCookieValue(xsrfCookies, XSRF_COOKIE);
        softly.assertThat(meUser.getId()).isEqualTo(registeredUser.getId());
        softly.assertThat(xsrfToken).isNotBlank();

        // when
        restTestClient.post()
                .uri(LOGOUT_PATH)
                .header(HttpHeaders.COOKIE, SESSION_COOKIE_PREFIX + sessionId)
                .exchange()
                .expectStatus().isNoContent();

        // then
        final var afterLogoutError = Objects.requireNonNull(restTestClient.get()
                .uri(ME_PATH)
                .header(HttpHeaders.COOKIE, SESSION_COOKIE_PREFIX + sessionId)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponseRest.class)
                .returnResult()
                .getResponseBody());

        assertThat(afterLogoutError.getCode()).isEqualTo("UNAUTHENTICATED");

        // when
        final var loginResult = restTestClient.post()
                .uri(LOGIN_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginInputRest("ANNA.KOWALSKA@example.com", VALID_PHRASE))
                .exchange()
                .expectStatus().isOk()
                .expectBody(CurrentUserRest.class)
                .returnResult();

        // then
        final var loginCookies = loginResult.getResponseHeaders().getOrEmpty(HttpHeaders.SET_COOKIE);
        final var newSessionId = extractCookieValue(loginCookies, SESSION_COOKIE);

        restTestClient.get()
                .uri(ME_PATH)
                .header(HttpHeaders.COOKIE, SESSION_COOKIE_PREFIX + newSessionId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CurrentUserRest.class)
                .value(user -> {
                    assertThat(user).isNotNull();
                    assertThat(user.getEmail()).isEqualTo("anna.kowalska@example.com");
                });
    }

    @Test
    void shouldRejectDuplicateRegistrationCaseInsensitively() {
        // given
        final var first = new RegisterInputRest(
                "jan.nowak@example.com",
                VALID_PHRASE,
                "Jan",
                "Nowak",
                PHONE
        );
        restTestClient.post()
                .uri(REGISTER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(first)
                .exchange()
                .expectStatus().isCreated();

        final var duplicate = new RegisterInputRest(
                "JAN.NOWAK@example.com",
                "Inne-Haslo-99",
                "Jan",
                "Nowak",
                "+48987654321"
        );

        // when
        final var error = Objects.requireNonNull(restTestClient.post()
                .uri(REGISTER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(duplicate)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody(ErrorResponseRest.class)
                .returnResult()
                .getResponseBody());

        // then
        assertThat(error.getCode()).isEqualTo("EMAIL_TAKEN");
    }

    @Test
    void shouldRejectAndInvalidateSessionWhenAccountBecomesSuspended() {
        // given
        final var registerResult = restTestClient.post()
                .uri(REGISTER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RegisterInputRest(
                        ANNA_EMAIL,
                        VALID_PHRASE,
                        "Anna",
                        LAST_NAME,
                        PHONE
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CurrentUserRest.class)
                .returnResult();

        final var sessionId = extractCookieValue(
                registerResult.getResponseHeaders().getOrEmpty(HttpHeaders.SET_COOKIE),
                SESSION_COOKIE
        );

        final var account = accountRepository.findByEmail(ANNA_EMAIL).orElseThrow();
        accountRepository.update(new Account(
                account.id(),
                account.email(),
                account.passwordHash(),
                account.firstName(),
                account.lastName(),
                account.phone(),
                AccountStatus.SUSPENDED,
                account.roles(),
                account.createdAt(),
                account.anonymizedAt()
        ));

        // when
        final var suspendedError = Objects.requireNonNull(restTestClient.get()
                .uri(ME_PATH)
                .header(HttpHeaders.COOKIE, SESSION_COOKIE_PREFIX + sessionId)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(ErrorResponseRest.class)
                .returnResult()
                .getResponseBody());

        // then
        assertThat(suspendedError.getCode()).isEqualTo("ACCOUNT_SUSPENDED");

        final var unauthenticatedError = Objects.requireNonNull(restTestClient.get()
                .uri(ME_PATH)
                .header(HttpHeaders.COOKIE, SESSION_COOKIE_PREFIX + sessionId)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponseRest.class)
                .returnResult()
                .getResponseBody());

        assertThat(unauthenticatedError.getCode()).isEqualTo("UNAUTHENTICATED");

        final var loginError = Objects.requireNonNull(restTestClient.post()
                .uri(LOGIN_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginInputRest(ANNA_EMAIL, VALID_PHRASE))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(ErrorResponseRest.class)
                .returnResult()
                .getResponseBody());

        assertThat(loginError.getCode()).isEqualTo("ACCOUNT_SUSPENDED");
    }

    @Test
    void shouldEnforceCsrfProtectionOnNonIgnoredEndpoint() {
        // given
        final var registerResult = restTestClient.post()
                .uri(REGISTER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RegisterInputRest(
                        "csrf@example.com",
                        VALID_PHRASE,
                        "Anna",
                        LAST_NAME,
                        PHONE
                ))
                .exchange()
                .expectStatus().isCreated()
                .returnResult(CurrentUserRest.class);

        final var sessionId = extractCookieValue(
                registerResult.getResponseHeaders().getOrEmpty(HttpHeaders.SET_COOKIE),
                SESSION_COOKIE
        );

        final var meResult = restTestClient.get()
                .uri(ME_PATH)
                .header(HttpHeaders.COOKIE, SESSION_COOKIE_PREFIX + sessionId)
                .exchange()
                .expectStatus().isOk()
                .returnResult(CurrentUserRest.class);

        final var xsrfToken = extractCookieValue(
                meResult.getResponseHeaders().getOrEmpty(HttpHeaders.SET_COOKIE),
                XSRF_COOKIE
        );

        // when
        final var missingCsrfResponse = Objects.requireNonNull(restTestClient.post()
                .uri("/v1/protected-resource")
                .header(HttpHeaders.COOKIE, SESSION_COOKIE_PREFIX + sessionId + "; " + XSRF_COOKIE + "=" + xsrfToken)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(ErrorResponseRest.class)
                .returnResult()
                .getResponseBody());

        // then
        assertThat(missingCsrfResponse.getCode()).isEqualTo("CSRF_INVALID");

        restTestClient.post()
                .uri("/v1/protected-resource")
                .header(HttpHeaders.COOKIE, SESSION_COOKIE_PREFIX + sessionId + "; " + XSRF_COOKIE + "=" + xsrfToken)
                .header("X-XSRF-TOKEN", xsrfToken)
                .exchange()
                .expectStatus().isNotFound();
    }
}
