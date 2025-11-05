package com.moodmate.config.encryption;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EncryptionService {
    private final AesGcmEncryptor encryptor;

    /**
     * 사용자 데이터 암호화
     */
    public String encrypt(String plainText, String dek) {
        try {
            return encryptor.encrypt(plainText, dek);
        } catch (Exception e) {
            throw new RuntimeException("데이터 암호화 실패", e);
        }
    }

    /**
     * 사용자 데이터 복호화
     */
    public String decrypt(String cipherText, String dek) {
        try {
            return encryptor.decrypt(cipherText, dek);
        } catch (Exception e) {
            throw new RuntimeException("데이터 복호화 실패", e);
        }
    }
}
