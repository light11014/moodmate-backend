package com.moodmate.config.jwt;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Builder;
import lombok.Getter;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Getter
public class JwtFactory {
    private String sub = "1";
    private Date issuedAt = new Date();

    private Date expiration = new Date(new Date().getTime() + Duration.ofDays(14).toMillis());
    private Map<String, Object> claims = Collections.emptyMap();

    @Builder
    public JwtFactory(String sub, Date issuedAt, Date expiration, Map<String, Object> claims) {
        this.sub = sub != null? sub : this.sub;
        this.issuedAt = issuedAt != null? issuedAt : this.issuedAt;
        this.expiration = expiration != null? expiration : this.expiration;
        this.claims = claims != null? claims : this.claims;
    }

    public String createToken(JwtProperties jwtProperties) {
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setIssuer(jwtProperties.getIssuer())
                .setSubject(sub)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .addClaims(claims)
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }
}
