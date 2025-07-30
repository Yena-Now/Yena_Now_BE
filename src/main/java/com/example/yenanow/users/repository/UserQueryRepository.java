package com.example.yenanow.users.repository;

import java.util.Optional;

public interface UserQueryRepository {

    Optional<String> findNicknameById(String userUuid);
}
