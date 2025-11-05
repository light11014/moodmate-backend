package com.moodmate.domain.user.service;

import com.moodmate.config.encryption.EncryptionKeyService;
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
    private final EncryptionKeyService keyService;

    @Transactional
    public User findOrCreateUser(OAuth2UserInfo userInfo, String provider) {
        try {
            String loginId = provider + "_" + userInfo.getProviderId();

            // 사용자별 고유 암호화 키 생성
            String key = keyService.createAndEncryptDek();

            return userRepository.findByLoginId(loginId)
                    .orElseGet(() -> userRepository.save(User.createOAuthUser(
                            loginId,
                            provider,
                            userInfo.getProviderId(),
                            Role.USER,
                            userInfo.getPicture(),
                            userInfo.getEmail(),
                            key
                    )));

        } catch (Exception e) {
            throw new RuntimeException("회원가입 중 오류 발생", e);
        }
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("unexpected User"));
    }
}

