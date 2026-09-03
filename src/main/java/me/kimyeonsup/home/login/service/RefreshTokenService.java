package me.kimyeonsup.home.login.service;

import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.global.exception.business.AuthenticationFailedException;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import me.kimyeonsup.home.global.exception.message.constants.LoginExceptionMessage;
import me.kimyeonsup.home.login.domain.entity.RefreshToken;
import me.kimyeonsup.home.login.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final MessageSourceResolver messageSourceResolver;

    public RefreshToken findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new AuthenticationFailedException(messageSourceResolver.resolve(
                        LoginExceptionMessage.REFRESH_TOKEN_NOT_FOUND, new Object[]{refreshToken}
                )));
    }
}
