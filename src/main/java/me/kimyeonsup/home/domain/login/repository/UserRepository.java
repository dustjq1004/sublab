package me.kimyeonsup.home.domain.login.repository;

import java.util.Optional;

import me.kimyeonsup.home.domain.login.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
