package com.example.yenanow.users.repository;

import java.util.List;

public interface FollowQueryRepository {

    List<String> findFollowingUuids(String userUuid);
}
