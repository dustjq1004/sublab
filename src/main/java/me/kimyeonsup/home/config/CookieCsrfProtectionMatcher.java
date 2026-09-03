package me.kimyeonsup.home.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class CookieCsrfProtectionMatcher implements RequestMatcher {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "TRACE", "OPTIONS");
    private static final Set<String> AUTHENTICATION_COOKIES =
            Set.of("JSESSIONID", "access_token", "refresh_token");

    @Override
    public boolean matches(HttpServletRequest request) {
        if (SAFE_METHODS.contains(request.getMethod())) {
            return false;
        }

        if (hasAuthenticationCookie(request)) {
            return true;
        }

        return !hasBearerToken(request.getHeader(HttpHeaders.AUTHORIZATION));
    }

    private boolean hasAuthenticationCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        return cookies != null && Arrays.stream(cookies)
                .anyMatch(cookie -> AUTHENTICATION_COOKIES.contains(cookie.getName()));
    }

    private boolean hasBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return false;
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim();
        return !token.isEmpty() && !"null".equalsIgnoreCase(token);
    }
}
