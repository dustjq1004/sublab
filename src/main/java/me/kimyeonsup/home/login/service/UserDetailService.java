package me.kimyeonsup.home.login.service;

import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.global.exception.business.UserNotFoundException;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import me.kimyeonsup.home.global.exception.message.constants.LoginExceptionMessage;
import me.kimyeonsup.home.login.domain.entity.User;
import me.kimyeonsup.home.login.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;
    private final MessageSourceResolver messageSourceResolver;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(messageSourceResolver.resolve(
                        LoginExceptionMessage.LOGIN_EMAIL_INVALID, new Object[]{email}
                )));
        return new PrincipalDetail(user);
    }
}
