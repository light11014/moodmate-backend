package com.moodmate.config.encryption;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeyManagementService {
    @Value("${master.encryption.key}")
    private String masterKey;

    private final EncryptionUtil encryptionUtil;

    public String createAndEncryptDek() throws Exception {
        // 랜덤 DEK 생성
        String dek = encryptionUtil.generateKey();

        // 마스터 키로 DEK 암호화
        return encryptionUtil.encrypt(dek, masterKey);
    }

    public String decryptDek(String encryptedDek) throws Exception {
        return encryptionUtil.decrypt(encryptedDek, masterKey);
    }
}
