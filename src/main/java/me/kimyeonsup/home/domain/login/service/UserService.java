package me.kimyeonsup.home.domain.login.service;

import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.global.exception.business.UserNotFoundException;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import me.kimyeonsup.home.global.exception.message.constants.LoginExceptionMessage;
import me.kimyeonsup.home.domain.login.domain.entity.User;
import me.kimyeonsup.home.domain.login.repository.UserRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final MessageSourceResolver messageSourceResolver;

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(messageSourceResolver.resolve(
                        LoginExceptionMessage.LOGIN_USER_NOT_FOUND, new Object[]{userId}
                )));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(messageSourceResolver.resolve(
                        LoginExceptionMessage.LOGIN_EMAIL_INVALID
                )));
    }
}
