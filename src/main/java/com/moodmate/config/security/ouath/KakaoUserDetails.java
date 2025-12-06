package com.moodmate.config.security.ouath;

import lombok.AllArgsConstructor;

import java.util.Map;
@AllArgsConstructor
public class KakaoUserDetails implements OAuth2UserInfo {

    private Map<String, Object> attributes;


    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getEmail() {
        return (String) ((Map) attributes.get("kakao_account")).get("email");
    }

    @Override
    public String getName() {
        return (String) ((Map) attributes.get("properties")).get("nickname");
    }

    @Override
    public String getPicture() {
        // 카카오 API는 기본적으로 profile_image 정보를 주지 않기도 함
        Object profile = ((Map) attributes.get("properties")).get("profile_image");
        if (profile != null && profile instanceof String && !((String) profile).isEmpty()) {
            return (String) profile;
        }
        return "https://example.com/default-profile.png"; // 👈 기본 이미지
    }
}
