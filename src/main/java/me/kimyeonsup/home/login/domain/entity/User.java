package me.kimyeonsup.home.login.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "nickname")
    private String nickname;

    @Column
    private String picture;

    @Builder
    public User(String email, String password, String role, String nickname, String picture) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.nickname = nickname;
        this.picture = picture;
    }

    public User update(String nickname) {
        this.nickname = nickname;
        return this;
    }
}
