package com.back.global.config.security;

import com.back.domain.user.user.entity.User;
import com.back.domain.user.auth.service.AuthService;
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
    private final AuthService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);        // 소셜 로그인을 통해 들어온 사용자 정보

        String oauthUserId = "";    // userId에 사용 할 Oauth2 제공자 고유 ID
        String profileImgUrl = "";  // 프로필 이미지 URL

        String providerType = userRequest.getClientRegistration().getRegistrationId().toUpperCase();  // GOOGLE, KAKAO, NAVER
        switch (providerType) {
            case "GOOGLE" -> {
                oauthUserId =  oAuth2User.getName();
                profileImgUrl= (String) oAuth2User.getAttributes().get("picture");
            }
            case "KAKAO" -> {
                Map<String, Object> attributes = oAuth2User.getAttributes();
                Map<String, Object> attributesProperties = (Map<String, Object>) attributes.get("properties");

                oauthUserId =  oAuth2User.getName();
                profileImgUrl= (String) attributesProperties.get("profile_image");
            }
            case "NAVER" -> {
                Map<String, Object> attributes = oAuth2User.getAttributes();
                Map<String, Object> attributesProperties = (Map<String, Object>) attributes.get("response");

                oauthUserId = (String) attributesProperties.get("id");
                profileImgUrl= (String) attributesProperties.get("profile_image");
            }
        }

        String username = providerType + "__%s".formatted(oauthUserId);

        User user = userService.joinOrLoginOAuth2User(username, profileImgUrl);

        return new SecurityUser(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getNickname()
        );
    }
}
