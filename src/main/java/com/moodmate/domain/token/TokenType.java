package com.moodmate.domain.token;

import lombok.Getter;

@Getter
public enum TokenType {
    ACCESS(1000L * 60 * 15), // 15분
    REFRESH(1000L * 60 * 60 * 24 * 7), // 7일
    TEST(1000L * 60 * 60 * 24 * 7); // 7일

    private final long expiration;
    TokenType(long expiration) { this.expiration = expiration; }
    public long getExpiration() { return expiration; }
}
