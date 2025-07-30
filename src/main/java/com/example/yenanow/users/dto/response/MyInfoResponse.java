package com.example.yenanow.users.dto.response;

import com.example.yenanow.users.entity.Gender;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MyInfoResponse {

    private String email;
    private String name;
    private String nickname;
    private Gender gender;
    private LocalDate birthdate;
    private String phoneNumber;
    private String profileUrl;
}
