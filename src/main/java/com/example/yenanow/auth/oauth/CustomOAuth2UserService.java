package com.example.yenanow.auth.oauth;

import com.example.yenanow.users.entity.User;
import com.example.yenanow.users.repository.UserRepository;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(request);

        User user = saveOrUpdate(request, oAuth2User);

        return new DefaultOAuth2User(
            null, // 권한 사용 안 함
            oAuth2User.getAttributes(),
            "email" // 기본 key
        );
    }

    private User saveOrUpdate(OAuth2UserRequest request, OAuth2User oAuth2User) {
        String registrationId = request.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = null;
        String name = null;
        String profileUrl = null;

        if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get(
                "kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            email = (String) kakaoAccount.get("email");
            name = (String) profile.get("nickname");
            profileUrl = (String) profile.get("profile_image_url");
        } else {
            // 기본은 Google
            email = (String) attributes.get("email");
            name = (String) attributes.getOrDefault("name", "소셜유저");
            profileUrl = (String) attributes.getOrDefault("picture", "");
        }

        String finalEmail = email;
        String finalName = name;
        String finalProfileUrl = profileUrl;
        return userRepository.findByEmail(email)
            .orElseGet(() -> {
                User newUser = User.builder()
                    .email(finalEmail)
                    .name(finalName)
                    .nickname("user_" + UUID.randomUUID().toString().substring(0, 8))
                    .password(UUID.randomUUID().toString()) // 소셜로그인한 사람 비밀번호는 걍 UUID 때려박음
                    .phoneNumber("010-0000-0000")
                    .profileUrl(finalProfileUrl)
                    .build();
                return userRepository.save(newUser);
            });
    }
}