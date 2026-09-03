package me.kimyeonsup.home.config.oauth;

import me.kimyeonsup.home.login.domain.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class PrincipalDetail implements UserDetails, OAuth2User {

    User user;
    OAuthAttributes oAuthAttributes;

    public PrincipalDetail(User user) {
        this.user = user;
    }

    public PrincipalDetail(User user, OAuthAttributes oAuthAttributes) {
        this.user = user;
        this.oAuthAttributes = oAuthAttributes;
    }

    @Override
    public String getName() {
        String email = user.getEmail();
        return email.substring(0, email.indexOf("@"));
    }

    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuthAttributes.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collect = new ArrayList<>();
        collect.add((GrantedAuthority) () -> user.getRole());
        return collect;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getNickname();
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
