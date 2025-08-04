package com.example.yenanow.users.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchResponse {

    private int totalPages;
    List<UserSearchResponseItem> userSearches;
}