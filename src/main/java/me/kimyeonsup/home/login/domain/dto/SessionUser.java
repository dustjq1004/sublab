package me.kimyeonsup.home.login.domain.dto;

import lombok.Getter;
import me.kimyeonsup.home.login.domain.entity.User;

import java.io.Serializable;

@Getter
public class SessionUser implements Serializable {

    private String name;
    private String email;
    private String picture;

    public SessionUser(User user) {
        this.name = user.getNickname();
        this.email = user.getEmail();
        this.picture = user.getPicture();
    }
}
