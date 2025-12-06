package com.moodmate.config.security.ouath;

public interface OAuth2UserInfo {
    String getProvider();
    String getProviderId();
    String getEmail();
    String getName();

    String getPicture();
}
