package me.kimyeonsup.home.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CsrfSecurityConfigurer {

    public static void configure(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        tokenRepository.setCookieCustomizer(cookie -> cookie
                .path("/")
                .sameSite("Lax"));

        CookieCsrfTokenRequestHandler requestHandler = new CookieCsrfTokenRequestHandler();

        http.csrf(csrf -> csrf
                .csrfTokenRepository(tokenRepository)
                .csrfTokenRequestHandler(requestHandler)
                .requireCsrfProtectionMatcher(new CookieCsrfProtectionMatcher()));
    }
}
