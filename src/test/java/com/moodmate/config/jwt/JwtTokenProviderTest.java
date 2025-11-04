package com.moodmate.config.jwt;

import com.moodmate.api.TestUtils;
import com.moodmate.domain.user.UserRepository;
import com.moodmate.domain.user.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class JwtTokenProviderTest {
    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private TestUtils testUtils;

    @Test
    public void 리프레시_토큰_생성() {
        // given
        User testUser = testUtils.createUser(userRepository);
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));

        // when
        String token = tokenProvider.createRefreshToken(testUser.getId());


        // then
        Long userId = Long.parseLong(Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject());

        assertThat(userId).isEqualTo(testUser.getId());
    }
}
