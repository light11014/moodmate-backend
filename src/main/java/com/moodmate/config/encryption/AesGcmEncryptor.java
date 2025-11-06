package com.moodmate.config.encryption;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AesGcmEncryptor {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    // 암호화
    public String encrypt(String plainText, String base64Key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        SecretKey key = new SecretKeySpec(keyBytes, "AES");

        // IV 생성
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // IV + 암호문을 합쳐서 저장
        byte[] encryptedWithIv = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, encryptedWithIv, iv.length, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(encryptedWithIv);
    }

    // 복호화
    public String decrypt(String encryptedText, String base64Key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        SecretKey key = new SecretKeySpec(keyBytes, "AES");

        byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedText);

        // IV 추출
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(encryptedWithIv, 0, iv, 0, iv.length);

        // 암호문 추출
        byte[] encryptedBytes = new byte[encryptedWithIv.length - GCM_IV_LENGTH];
        System.arraycopy(encryptedWithIv, GCM_IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
