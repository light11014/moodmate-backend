package com.moodmate.domain.user.service;

import com.moodmate.domain.user.UserRepository;
import com.moodmate.domain.user.entity.Role;
import com.moodmate.domain.user.entity.User;
import com.moodmate.domain.user.ouath.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User findOrCreateUser(OAuth2UserInfo userInfo, String provider) {
        String loginId = provider + "_" + userInfo.getProviderId();

        return userRepository.findByLoginId(loginId)
                .orElseGet(() -> userRepository.save(User.createOAuthUser(
                        loginId,
                        provider,
                        userInfo.getProviderId(),
                        Role.USER,
                        userInfo.getPicture(),
                        userInfo.getEmail()
                )));
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("unexpected User"));
    }
}

