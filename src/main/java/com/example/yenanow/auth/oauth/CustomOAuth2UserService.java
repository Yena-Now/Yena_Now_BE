package com.example.yenanow.auth.oauth;

import com.example.yenanow.users.entity.User;
import com.example.yenanow.users.repository.UserRepository;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(request);
        saveOrUpdate(request, oAuth2User);

        String registrationId = request.getClientRegistration().getRegistrationId();
        Map<String, Object> originalAttributes = oAuth2User.getAttributes();

        Map<String, Object> attributes = new java.util.HashMap<>(originalAttributes);

        // 카카오는 받아오는 형식이 구글과 달라 flatten 처리
        if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get(
                "kakao_account");
            if (kakaoAccount != null && kakaoAccount.get("email") != null) {
                attributes.put("email", kakaoAccount.get("email"));
            }
        }

        // provider 정보를 attributes에 추가하여 Authentication 객체로 전달
        attributes.put("provider", registrationId);

        return new DefaultOAuth2User(
            null,
            attributes,
            "email"
        );
    }

    private User saveOrUpdate(OAuth2UserRequest request, OAuth2User oAuth2User) {
        String registrationId = request.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email;
        String name;
        String profileUrl;

        if (registrationId.equals("kakao")) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get(
                "kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            email = (String) kakaoAccount.get("email");
            name = (String) profile.getOrDefault("nickname", "소셜유저");
            profileUrl = (String) profile.getOrDefault("profile_image_url", "");
        } else {
            // 기본은 Google
            email = (String) attributes.get("email");
            name = (String) attributes.getOrDefault("name", "소셜유저");
            profileUrl = (String) attributes.getOrDefault("picture", "");
        }

        String finalEmail = email;
        String finalName = name;
        String finalProfileUrl = profileUrl;
        return userRepository.findByEmailAndProvider(email, registrationId)
            .orElseGet(() -> {
                User user = User.builder()
                    .email(finalEmail)
                    .name(finalName)
                    .nickname("user_" + UUID.randomUUID().toString().substring(0, 8))
                    .password(UUID.randomUUID().toString()) // 소셜로그인 시 비밀번호는 필요없기 때문에 UUID생성해서 저장
                    .phoneNumber(null)
                    .profileUrl(finalProfileUrl)
                    .provider(registrationId)
                    .build();

                User savedUser = userRepository.save(user);

                // Redis에 팔로워, 팔로잉 수 및 게시글(N컷) 수 초기값 0 저장
                String key = "user:" + savedUser.getUserUuid();
                redisTemplate.opsForHash().put(key, "follower_count", "0");
                redisTemplate.opsForHash().put(key, "following_count", "0");
                redisTemplate.opsForHash().put(key, "total_cut", "0");

                return savedUser;
            });
    }
}