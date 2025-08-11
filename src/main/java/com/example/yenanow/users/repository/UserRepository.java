package com.example.yenanow.users.repository;

import com.example.yenanow.users.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String>, UserQueryRepository {

    Optional<User> findByUserUuid(String uuid);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndProvider(String email, String provider);

    Optional<User> findByEmailAndProviderIsNull(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}