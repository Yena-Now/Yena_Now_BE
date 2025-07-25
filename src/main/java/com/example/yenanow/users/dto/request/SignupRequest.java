package com.example.yenanow.users.dto.request;

import com.example.yenanow.users.entity.Gender;
import com.example.yenanow.users.entity.User;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    private String email;
    private String password;
    private String name;
    private String nickname;
    private String gender; // MALE FEMALE
    private String birthdate;
    private String phoneNumber;
    private String profileUrl;

    public User toEntity() {
        return User.builder()
            .email(email)
            .password(password)
            .name(name)
            .nickname(nickname)
            .gender(Gender.valueOf(gender))
            .birthdate(LocalDate.parse(birthdate))
            .phoneNumber(phoneNumber)
            .profileUrl(profileUrl)
            .build();
    }
}

