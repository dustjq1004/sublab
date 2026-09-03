package me.kimyeonsup.home.config.oauth;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.login.domain.dto.SessionUser;
import me.kimyeonsup.home.login.domain.entity.User;
import me.kimyeonsup.home.login.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OAuth2UserCustomService extends DefaultOAuth2UserService {

    public static final String SESSION_USER_KEY = "user";
    private static final int INTERVAL_TIME = 30 * 60;
    private static final String EXCEPTION_MESSAGE = "관리자만 로그인 가능합니다.";

    private final UserRepository userRepository;
    private final HttpSession httpSession;
    private final AdminProperties adminProperties;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getClientId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes oAuthAttributes = OAuthAttributes.of(registrationId, userNameAttributeName,
                oAuth2User.getAttributes());

        // 관리자만 로그인 가능
        if (!adminProperties.getEmail().equals(oAuthAttributes.getEmail())) {
            throw new AuthenticationServiceException(EXCEPTION_MESSAGE);
        }

        User user = saveOrUpdate(oAuthAttributes);

        httpSession.setMaxInactiveInterval(INTERVAL_TIME);
        httpSession.setAttribute(SESSION_USER_KEY, new SessionUser(user));
        return new PrincipalDetail(user, oAuthAttributes);
    }

    private User saveOrUpdate(OAuthAttributes attributes) {
        String email = attributes.getEmail();
        User user = userRepository.findByEmail(email)
                .map(entity -> entity.update(attributes.getName()))
                .orElse(attributes.toEntity());
        return userRepository.save(user);
    }
}
