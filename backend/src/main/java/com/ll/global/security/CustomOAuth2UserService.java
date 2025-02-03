package com.ll.global.security;

import com.ll.domain.member.member.entity.Member;
import com.ll.domain.member.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final MemberService memberService;

    // 소셜 로그인이 성공할 때마다 이 함수가 실행된다.
    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String providerTypeCode = userRequest
                .getClientRegistration()
                .getRegistrationId()
                .toUpperCase(Locale.getDefault());

        String oauthId = switch (providerTypeCode) {
            case "NAVER" -> ((Map<String, String>) oAuth2User.getAttributes().get("response")).get("id");
            default -> oAuth2User.getName();
        };

        Map<String, Object> attributes = oAuth2User.getAttributes();

        String username = providerTypeCode + "__" + oauthId;
        String nickname = null;
        String profileImgUrl = null;

        switch ( providerTypeCode ) {
            case "NAVER" -> {
                Map<String, String> attributesProperties = (Map<String, String>) attributes.get("response");
                nickname = attributesProperties.get("nickname");
                profileImgUrl = attributesProperties.get("profile_image");
            }
            default -> {
                Map<String, String> attributesProperties = (Map<String, String>) attributes.get("properties");
                nickname = attributesProperties.get("nickname");
                profileImgUrl = attributesProperties.get("profile_image");
            }
        }

        Member member = memberService.modifyOrJoin(username, nickname, profileImgUrl);

        return new SecurityUser(
                member.getId(),
                member.getUsername(),
                "",
                member.getNickname(),
                member.getAuthorities()
        );
    }
}