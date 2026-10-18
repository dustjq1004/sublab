package me.kimyeonsup.home.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import me.kimyeonsup.home.config.oauth.OAuth2SuccessHandler;
import me.kimyeonsup.home.util.CookieUtil;
import me.kimyeonsup.home.util.JwtUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class CookieCsrfProtectionMatcher implements RequestMatcher {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "TRACE", "OPTIONS");

    @Override
    public boolean matches(HttpServletRequest request) {
        if (SAFE_METHODS.contains(request.getMethod())) {
            return false;
        }

        if (hasAuthenticationCookie(request)) {
            return true;
        }

        String bearerToken = JwtUtils.extractBearerToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        return bearerToken == null;
    }

    private boolean hasAuthenticationCookie(HttpServletRequest request) {
        return CookieUtil.getCookieValue(request, CookieUtil.ACCESS_TOKEN_COOKIE_NAME).isPresent()
                || CookieUtil.getCookieValue(request, OAuth2SuccessHandler.REFRESH_TOKEN_COOKIE_NAME).isPresent();
    }
}
