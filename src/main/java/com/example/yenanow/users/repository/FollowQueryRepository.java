package com.example.yenanow.users.repository;

import com.example.yenanow.users.dto.response.FollowingResponseItem;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FollowQueryRepository {

    List<String> findFollowingUuids(String userUuid);

    Page<FollowingResponseItem> findFollowings(String targetUserUuid, String currentUseruuid,
        Pageable pageable);
}
