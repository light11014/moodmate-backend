package com.moodmate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "Auth API")
@RestController
@RequestMapping("/api/auth")
public class AuthDocController {

    @Operation(
            summary = "소셜 로그인 시작",
            description = "구글/카카오 등 소셜 로그인을 시작합니다. 실제 요청 시 Spring Security OAuth2 Login이 처리합니다."
    )
    @GetMapping("/login/{provider}")
    public ResponseEntity<Void> login(
            @Parameter(description = "로그인 제공자 (google, kakao)")
            @PathVariable String provider
    ) {
        // Swagger 문서용 dummy 엔드포인트
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "로그아웃", description = "로그아웃 요청을 처리합니다. 실제 요청 시 Spring Security OAuth2 Login이 처리합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Swagger 문서용 dummy 엔드포인트
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}

