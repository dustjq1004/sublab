package me.kimyeonsup.home.login.service;

import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.global.exception.business.UserNotFoundException;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import me.kimyeonsup.home.global.exception.message.constants.LoginExceptionMessage;
import me.kimyeonsup.home.login.domain.dto.AddUserRequest;
import me.kimyeonsup.home.login.domain.entity.User;
import me.kimyeonsup.home.login.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final MessageSourceResolver messageSourceResolver;

    public Long save(AddUserRequest dto) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return userRepository.save(User.builder()
                .email(dto.getEmail())
                .password(encoder.encode(dto.getPassword()))
                .build()).getId();
    }

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
