package me.kimyeonsup.home.domain.blog.admin.article.service;

import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.config.oauth.AdminProperties;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.global.exception.business.AuthenticationFailedException;
import me.kimyeonsup.home.global.exception.business.ReadAccessDeniedException;
import me.kimyeonsup.home.global.exception.business.UpdateAccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminArticleAuthorizationService {

    private final AdminProperties adminProperties;

    public void validateAdminReadAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (isAnonymous(authentication)) {
            throw new AuthenticationFailedException("인증이 필요합니다.");
        }
        if (!isAdmin(authentication)) {
            throw new ReadAccessDeniedException("관리자만 접근할 수 있습니다.");
        }
    }

    public void validateAdminWriteAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (isAnonymous(authentication)) {
            throw new AuthenticationFailedException("인증이 필요합니다.");
        }
        if (!isAdmin(authentication)) {
            throw new UpdateAccessDeniedException("관리자만 접근할 수 있습니다.");
        }
    }

    private boolean isAnonymous(Authentication authentication) {
        return authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName());
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication.getPrincipal() instanceof PrincipalDetail principalDetail) {
            return adminProperties.getEmail().equals(principalDetail.getEmail());
        }
        return false;
    }
}
