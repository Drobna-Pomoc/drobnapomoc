package com.drobnapomoc.drobnapomoc;

import com.drobnapomoc.drobnapomoc.api.AuthenticationApi;
import com.drobnapomoc.drobnapomoc.api.model.CurrentUserRest;
import com.drobnapomoc.drobnapomoc.api.model.RegisterInputRest;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Base for end-to-end tests through the API: the whole application on a random port with the Testcontainers
 * database. Browsers carry their own session and CSRF cookies; every registered user gets a unique e-mail,
 * so tests don't depend on each other or on the database state.
 */
@ExtendWith(SoftAssertionsExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
public abstract class BaseIntegrationTest {

    protected static final String BASE_PATH = TestApiPaths.BASE_PATH;

    private static final String REGISTER_PATH = BASE_PATH + AuthenticationApi.PATH_REGISTER;
    private static final String ME_PATH = BASE_PATH + AuthenticationApi.PATH_GET_CURRENT_USER;
    private static final String SESSION_COOKIE = "SESSION";
    private static final String XSRF_COOKIE = "XSRF-TOKEN";
    private static final String XSRF_HEADER = "X-XSRF-TOKEN";

    @Autowired
    protected RestTestClient restTestClient;

    /** A visitor without a session; any API response, also a 401, gives them the CSRF cookie. */
    protected Browser anonymousBrowser() {
        final var response = restTestClient.get().uri(ME_PATH)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(String.class)
                .returnResult();
        return new Browser(null, cookieValue(response.getResponseHeaders().getOrEmpty(HttpHeaders.SET_COOKIE), XSRF_COOKIE));
    }

    /** Registers a new client with a unique e-mail and returns their logged-in browser. */
    protected RegisteredUser registeredUser() {
        final var registered = restTestClient.post().uri(REGISTER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RegisterInputRest(UUID.randomUUID() + "@example.com", "Zielony-Rower-42", "Anna", "Kowalska", "+48123456789"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CurrentUserRest.class)
                .returnResult();
        final var session = cookieValue(registered.getResponseHeaders().getOrEmpty(HttpHeaders.SET_COOKIE), SESSION_COOKIE);
        final var me = restTestClient.get().uri(ME_PATH)
                .header(HttpHeaders.COOKIE, SESSION_COOKIE + "=" + session)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CurrentUserRest.class)
                .returnResult();
        final var xsrf = cookieValue(me.getResponseHeaders().getOrEmpty(HttpHeaders.SET_COOKIE), XSRF_COOKIE);
        return new RegisteredUser(Objects.requireNonNull(registered.getResponseBody()).getId(), new Browser(session, xsrf));
    }

    protected RestTestClient.ResponseSpec get(final Browser browser, final String uri, final Object... uriVariables) {
        return restTestClient.get().uri(uri, uriVariables)
                .header(HttpHeaders.COOKIE, browser.cookieHeader())
                .exchange();
    }

    /** Sends a JSON body with the browser's cookies and its CSRF token. */
    protected RestTestClient.ResponseSpec post(final Browser browser, final String uri, final byte[] jsonBody) {
        return restTestClient.post().uri(uri)
                .header(HttpHeaders.COOKIE, browser.cookieHeader())
                .header(XSRF_HEADER, browser.xsrf())
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonBody)
                .exchange();
    }

    private static String cookieValue(final List<String> setCookieHeaders, final String name) {
        return setCookieHeaders.stream()
                .filter(header -> header.startsWith(name + "="))
                .map(header -> header.substring(name.length() + 1).split(";", 2)[0])
                .findFirst()
                .orElseThrow(() -> new AssertionError("Brak ciasteczka " + name + " w " + setCookieHeaders));
    }

    /** Cookies of one browser: session (when logged in) and CSRF token. */
    protected record Browser(@Nullable String session, String xsrf) {

        String cookieHeader() {
            return (session != null ? SESSION_COOKIE + "=" + session + "; " : "") + XSRF_COOKIE + "=" + xsrf;
        }
    }

    protected record RegisteredUser(long id, Browser browser) {
    }
}
