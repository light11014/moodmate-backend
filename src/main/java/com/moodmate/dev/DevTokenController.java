package com.moodmate.dev;

import com.moodmate.config.jwt.JwtTokenProvider;
import com.moodmate.domain.token.RefreshToken;
import com.moodmate.domain.token.RefreshTokenRepository;
import com.moodmate.domain.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Dev-Token-Controller", description = "편의를 위한 token 발급")
@RestController
@RequestMapping("/api/auth")
@Profile("dev")
@RequiredArgsConstructor
public class DevTokenController {

    private final JwtTokenProvider jwtProvider;
    private final UserRepository userRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    @GetMapping("/dev-token")
    @Operation(summary = "테스트 유저 토큰 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "토큰 조회",
                            content = @Content(
                                    mediaType = "text/plain",
                                    examples = @ExampleObject(value = "\"test_jwt_token\""))),
            }
    )
    public ResponseEntity<String> getDevToken() {
        return userRepository.findByLoginId("dummy_1")
                .map(user -> {
                    String refreshToken = jwtProvider.createRefreshToken(user.getId());
                    refreshTokenRepository.save(new RefreshToken(user, refreshToken));
                    return ResponseEntity.ok(refreshToken);
                })
                .orElseGet(() -> ResponseEntity.badRequest().body("테스트 유저 없음"));
    }
}
