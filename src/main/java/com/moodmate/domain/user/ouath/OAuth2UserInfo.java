package com.moodmate.domain.user.ouath;

public interface OAuth2UserInfo {
    String getProvider();
    String getProviderId();
    String getEmail();
    String getName();

    String getPicture();
}
