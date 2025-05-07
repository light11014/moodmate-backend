package com.moodmate.util;

import com.moodmate.oauth.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private final String SECRET_KEY = "your-secret-key-which-is-at-least-32-bytes-long"; // 반드시 길게!
    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24시간

    // 🔐 토큰 생성
    public String createToken(Long userId, Role role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(userId.toString()) // 사용자 ID 저장
                .claim("role", role.name())    // 사용자 역할 저장
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 🔍 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            getClaims(token); // 파싱만 성공하면 유효
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 🧾 사용자 ID 추출
    public Long getUserId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    // 📄 Claims 추출
    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }
}

