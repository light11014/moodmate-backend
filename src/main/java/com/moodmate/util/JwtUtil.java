package com.moodmate.util;

import com.moodmate.oauth.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

@Component
public class JwtUtil {
    private final String SECRET_KEY = "your-secret-key-which-is-at-least-32-bytes-long"; // 반드시 길게!
    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 24L;

    private final long ACCESS_TOKEN_EXPIRATION_TIME = 1000 * 60 * 15; // 15분
    private final long REFRESH_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7; // 7일

    // 🔐 토큰 생성
    public String createToken(Long userId, String email, Role role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(userId.toString()) // 사용자 ID 저장
                .claim("email", email)         // 이메일 추가
                .claim("role", role.name())    // 사용자 역할 저장
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 🔍 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            System.out.println("[DEBUG] Token to Verify: " + token);

            getClaims(token); // 파싱만 성공하면 유효
            return true;
        } catch (Exception e) {
            // 예외가 발생했을 때 로깅
            System.out.println("[ERROR] JWT validation failed: " + e.getMessage());
            return false;
        }
    }



    public Collection<? extends GrantedAuthority> getAuthorities(String role) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }


    // 🧾 사용자 ID 추출
    public Long getUserId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    // ✉️ 이메일 추출
    public String extractEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    // 📄 Claims 추출
    private Claims getClaims(String token) {
        System.out.println("[DEBUG] token received: " + token);
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();
    }

    // 🧾 사용자 ID 추출
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return Long.parseLong(claims.getSubject());  // 토큰에서 사용자 ID를 반환
    }

    // 📄 사용자 역할 추출
    public String getUserRoleFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("role", String.class);  // 토큰에서 사용자 역할 반환
    }

}

