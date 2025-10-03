package com.moodmate.config.jwt;

import com.moodmate.domain.token.TokenType;
import com.moodmate.domain.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.function.Consumer;

@Service
public class JwtTokenProvider {
    private final SecretKey key;
    private final JwtProperties jwtProperties;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(User user) {
        return createToken(user.getId(), TokenType.ACCESS, b -> {
            b.claim("email", user.getEmail())
                    .claim("role", user.getRole().name())
                    .claim("token-type", TokenType.ACCESS);
        });
    }

    public String createRefreshToken(Long userId) {
        return createToken(userId, TokenType.REFRESH, b -> {
            b.claim("token-type", TokenType.REFRESH);
        });
    }

    public String generateExpiredToken(Long userId, TokenType tokenType) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() - 10000);  // 10초 전에 만료

        return Jwts.builder()
                .setIssuer(jwtProperties.getIssuer())
                .setSubject(userId.toString())
                .setIssuedAt(new Date(now.getTime() - tokenType.getExpiration()))
                .setExpiration(expiry)
                .claim("token-type", tokenType.name())
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 생성
    private String createToken(Long userId, TokenType tokenType, Consumer<JwtBuilder> claimsAdder) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + tokenType.getExpiration());

        JwtBuilder builder = Jwts.builder()
                .setIssuer(jwtProperties.getIssuer())
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expiry);

        claimsAdder.accept(builder);
        return builder.signWith(key, SignatureAlgorithm.HS256).compact();
    }

    // 토큰 유효성 검증
    public void validateToken(String token, TokenType expectedType) {
        Claims claims = getClaims(token);

        // 토큰 타입 확인
        String tokenType = claims.get("token-type", String.class);
        if (!expectedType.name().equals(tokenType)) {
            throw new IllegalArgumentException("Wrong token type: expected " + expectedType);
        }
    }

    public Collection<? extends GrantedAuthority> getAuthorities(String role) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    // 사용자 ID 추출
    public Long getUserId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    // 이메일 추출
    public String extractEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    // Claims 추출
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)  // key는 SecretKey 또는 byte[]
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

