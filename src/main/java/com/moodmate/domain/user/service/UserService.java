package com.moodmate.domain.user.service;

import com.moodmate.domain.user.UserRepository;
import com.moodmate.domain.user.entity.Role;
import com.moodmate.domain.user.entity.User;
import com.moodmate.domain.user.ouath.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findOrCreateUser(OAuth2UserInfo userInfo, String provider) {
        String loginId = provider + "_" + userInfo.getProviderId();

        return userRepository.findByLoginId(loginId)
                .orElseGet(() -> userRepository.save(User.builder()
                        .loginId(loginId)
                        .provider(provider)
                        .providerId(userInfo.getProviderId())
                        .role(Role.USER)
                        .pictureUrl(userInfo.getPicture())
                        .email(userInfo.getEmail())
                        .username(null)
                        .build()));
    }
}

