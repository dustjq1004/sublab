package me.kimyeonsup.home.config;

import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.config.jwt.TokenAuthenticationFilter;
import me.kimyeonsup.home.config.jwt.TokenProvider;
import me.kimyeonsup.home.config.oauth.OAuth2AuthorizationRequestBasedOnCookieRepository;
import me.kimyeonsup.home.config.oauth.OAuth2Properties;
import me.kimyeonsup.home.config.oauth.OAuth2SuccessHandler;
import me.kimyeonsup.home.config.oauth.OAuth2UserCustomService;
import me.kimyeonsup.home.domain.login.repository.RefreshTokenRepository;
import me.kimyeonsup.home.domain.login.service.UserDetailService;
import me.kimyeonsup.home.domain.login.service.UserService;
import me.kimyeonsup.home.util.CookieUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@RequiredArgsConstructor
@Configuration
@Profile({"test"})
public class TestWebOAuthSecurityConfig {
    private final OAuth2UserCustomService oAuth2UserCustomService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;
    private final UserDetailService userDetailService;
    private final OAuth2Properties oauth2Properties;
    private final ArticleAdminAuthorizationManager articleAdminAuthorizationManager;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        CsrfSecurityConfigurer.configure(http);

        http.httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable());

        http.authorizeHttpRequests(authorization -> authorization
                .requestMatchers(HttpMethod.POST, "/api/token", "/api/md/render").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/articles", "/api/navigate", "/api/articles/{menuId}",
                        "/api/main/**"
                ).permitAll()
                .requestMatchers("/api/token/validate").authenticated()
                .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()
                .requestMatchers("/actuator/**").authenticated()
                .requestMatchers("/api/admin/articles", "/api/admin/articles/**")
                .access(articleAdminAuthorizationManager)
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll());

        http.oauth2Login(oAuth2Login -> oAuth2Login
                .loginPage("/oauth2/authorization/google")
                .authorizationEndpoint(authorizationEndpointConfig -> authorizationEndpointConfig
                        .authorizationRequestRepository(oAuth2AuthorizationRequestBasedOnCookieRepository()))
                .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                        .userService(oAuth2UserCustomService))
                .failureUrl(oauth2Properties.getLoginFailureRedirectUri())
                .successHandler(oAuth2AuthorizationSuccessHandler()));

        http.sessionManagement((session) ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        http.logout(logout ->
                logout.logoutUrl("/api/logout")
                        .deleteCookies(CookieUtil.ACCESS_TOKEN_COOKIE_NAME,
                                OAuth2SuccessHandler.REFRESH_TOKEN_COOKIE_NAME)
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpStatus.NO_CONTENT.value())));

        http.exceptionHandling(exceptionHandler ->
                exceptionHandler.defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        new AntPathRequestMatcher("/api/**")));

        return http.build();
    }

    @Bean
    public OAuth2SuccessHandler oAuth2AuthorizationSuccessHandler() {
        return new OAuth2SuccessHandler(tokenProvider,
                refreshTokenRepository,
                oAuth2AuthorizationRequestBasedOnCookieRepository(),
                userService,
                oauth2Properties);
    }

    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider, userDetailService);
    }

    @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
        return new OAuth2AuthorizationRequestBasedOnCookieRepository();
    }

}
