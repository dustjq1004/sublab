package me.kimyeonsup.home.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.global.exception.business.UserNotFoundException;
import me.kimyeonsup.home.domain.login.service.UserDetailService;
import me.kimyeonsup.home.util.CookieUtil;
import me.kimyeonsup.home.util.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final static String HEADER_AUTHORIZATION = "Authorization";
    private final static String TOKEN_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;
    private final UserDetailService userDetailService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = resolveAccessToken(request);

        if (!StringUtils.isNull(token)) {
            String email = tokenProvider.getSubject(token);
            try {
                PrincipalDetail principal = (PrincipalDetail) userDetailService.loadUserByUsername(email);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        principal, token, principal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (UserNotFoundException ignored) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getAccessToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
            return authorizationHeader.substring(TOKEN_PREFIX.length());
        }

        return null;
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = getAccessToken(request.getHeader(HEADER_AUTHORIZATION));
        if (bearerToken != null && tokenProvider.validToken(bearerToken)) {
            return bearerToken;
        }

        return CookieUtil.getCookieValue(request, CookieUtil.ACCESS_TOKEN_COOKIE_NAME)
                .filter(tokenProvider::validToken)
                .orElse(null);
    }
}
