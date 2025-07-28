package com.example.yenanow.users.repository;

import com.example.yenanow.users.entity.User;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findById(String uuid);

    Optional<User> findByEmail(String email);

}
