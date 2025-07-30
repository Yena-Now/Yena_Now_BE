package com.example.yenanow.users.dto.request;

import com.example.yenanow.users.entity.Gender;
import com.example.yenanow.users.entity.User;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
    private String gender;
    private String birthdate;
    private String phoneNumber;
    private String profileUrl;

    public User toEntity() {
        return User.builder()
            .email(email)
            .password(password)
            .name(blankToNull(name))
            .nickname(nickname)
            .gender(Gender.from(gender))
            .birthdate(parseBirthdate(birthdate))
            .phoneNumber(blankToNull(phoneNumber))
            .profileUrl(blankToNull(profileUrl))
            .build();
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private LocalDate parseBirthdate(String birthdate) {
        if (birthdate == null || birthdate.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(birthdate);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}