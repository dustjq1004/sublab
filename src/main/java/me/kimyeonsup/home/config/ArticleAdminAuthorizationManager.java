package me.kimyeonsup.home.config;

import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.config.oauth.AdminProperties;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleAdminAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final AdminProperties adminProperties;

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        Authentication auth = authentication.get();
        boolean granted = auth != null && auth.isAuthenticated()
                && auth.getPrincipal() instanceof PrincipalDetail principal
                && principal.getEmail().equals(adminProperties.getEmail());
        return new AuthorizationDecision(granted);
    }
}
