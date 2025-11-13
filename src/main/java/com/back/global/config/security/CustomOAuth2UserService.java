package com.back.global.config.security;

import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import com.back.domain.user.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserService userService;
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String oauthUserId = "";    // OAuth 제공자에서 제공하는 고유 사용자 ID
        String providerType = userRequest.getClientRegistration().getRegistrationId().toUpperCase();  // "kakao", "google", "naver" 등

        String nickname = "";       // 사용자 닉네임
        String profileImgUrl = "";  // 프로필 이미지 URL
        String username = "";       // 사용자 이름(닉네임과 동일하게 설정)

        switch (providerType) {
            case "GOOGLE" -> {
                oauthUserId =  oAuth2User.getName();
                nickname = (String) oAuth2User.getAttributes().get("name");
                profileImgUrl= (String) oAuth2User.getAttributes().get("picture");
            }
            case "KAKAO" -> {
                oauthUserId =  oAuth2User.getName();
                Map<String, Object> attributes = oAuth2User.getAttributes();
                Map<String, Object> attributesProperties = (Map<String, Object>) attributes.get("properties");

                nickname = (String) attributesProperties.get("nickname");
                profileImgUrl= (String) attributesProperties.get("profile_image");
            }
            case "NAVER" -> {
                Map<String, Object> attributes = oAuth2User.getAttributes();
                Map<String, Object> attributesProperties = (Map<String, Object>) attributes.get("response");

                oauthUserId = (String) attributesProperties.get("id");
                nickname = (String) attributesProperties.get("nickname");
                profileImgUrl= (String) attributesProperties.get("profile_image");
            }
        }

        System.out.println("oAuth2User.getAttributes() = " + oAuth2User.getAttributes());
        System.out.println("providerType = " + providerType);
        System.out.println("username = " + username);


        username = providerType + "__%s".formatted(oauthUserId);

        String password = "";

        User user = userRepository.findByUsername(username).orElse(null);

        if(user == null) {
            // 신규 사용자 회원가입 처리
            user = userService.joinOAuth2User(username, password, nickname, profileImgUrl);
        }

        return new SecurityUser(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getNickname()
        );
    }
}
