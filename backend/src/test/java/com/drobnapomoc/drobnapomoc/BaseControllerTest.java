package com.drobnapomoc.drobnapomoc;

import com.drobnapomoc.drobnapomoc.api.model.ErrorResponseRest;
import com.drobnapomoc.drobnapomoc.shared.security.RestAccessDeniedHandler;
import com.drobnapomoc.drobnapomoc.shared.security.RestAuthenticationEntryPoint;
import com.drobnapomoc.drobnapomoc.shared.security.SecurityConfig;
import com.drobnapomoc.drobnapomoc.shared.web.GlobalExceptionHandler;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@ExtendWith(SoftAssertionsExtension.class)
@Import({
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
public abstract class BaseControllerTest {

    protected static final String BASE_PATH = TestApiPaths.BASE_PATH;

    @Autowired
    protected MockMvcTester mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected static RequestPostProcessor loggedInAs(final String role) {
        return user("user@example.com").roles(role);
    }

    protected static HttpStatus status(final MvcTestResult result) {
        return HttpStatus.valueOf(result.getResponse().getStatus());
    }

    protected static String json(final MvcTestResult result) throws IOException {
        return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    protected <T> T body(final MvcTestResult result, final Class<T> type) throws IOException {
        return objectMapper.readValue(json(result), type);
    }

    protected ErrorResponseRest error(final MvcTestResult result) throws IOException {
        return body(result, ErrorResponseRest.class);
    }
}
