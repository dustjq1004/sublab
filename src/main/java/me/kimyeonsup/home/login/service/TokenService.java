package me.kimyeonsup.home.login.service;


import java.time.Duration;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.config.jwt.TokenProvider;
import me.kimyeonsup.home.global.exception.business.InvalidTokenException;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import me.kimyeonsup.home.global.exception.message.constants.LoginExceptionMessage;
import me.kimyeonsup.home.login.domain.entity.User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TokenService {

    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final MessageSourceResolver messageSourceResolver;

    public String createNewAccessToken(String refreshToken) {
        if (!tokenProvider.validToken(refreshToken)) {
            throw new InvalidTokenException(messageSourceResolver.resolve(
                    LoginExceptionMessage.REFRESH_TOKEN_EXPIRED, new Object[]{refreshToken}
            ));
        }

        Long userId = refreshTokenService.findByRefreshToken(refreshToken).getUserId();
        User user = userService.findById(userId);
        return tokenProvider.generateToken(user, Duration.ofHours(2));
    }
}
