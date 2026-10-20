package com.showtime.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Rejects requests without a valid "Authorization: Bearer <token>" header and sets request attribute userId. */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenService tokens;

    public AuthInterceptor(TokenService tokens) {
        this.tokens = tokens;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String header = request.getHeader("Authorization");
        Long userId = (header != null && header.startsWith("Bearer "))
                ? tokens.userIdFor(header.substring(7).trim())
                : null;
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Please log in again\"}");
            return false;
        }
        request.setAttribute("userId", userId);
        return true;
    }
}
