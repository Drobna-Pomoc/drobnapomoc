package com.drobnapomoc.drobnapomoc.auth.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        if (accessDeniedException instanceof CsrfException) {
            response.getWriter().write("""
                    {
                        "code": "CSRF_INVALID",
                        "message": "Brakujący lub niepoprawny token CSRF."
                    }
                    """);
        } else {
            response.getWriter().write("""
                    {
                        "code": "ACCESS_DENIED",
                        "message": "Brak uprawnień do tej operacji."
                    }
                    """);
        }
    }
}
