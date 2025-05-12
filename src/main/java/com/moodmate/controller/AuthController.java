package com.moodmate.controller;

import com.moodmate.entity.Member;
import com.moodmate.oauth.CustomOauth2User;
import com.moodmate.repository.MemberRepository;
import com.moodmate.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final JwtUtil jwtUtil;

    @PostMapping("/api/auth/oauth/callback")
    public ResponseEntity<?> oauthLogin(@AuthenticationPrincipal CustomOauth2User oAuthUser) {
        Member member = oAuthUser.getMember();

        String token = jwtUtil.createToken(member.getId(), member.getEmail(), member.getRole());

        boolean nicknameRequired = (member.getUsername() == null || member.getUsername().isBlank());


        return ResponseEntity.ok(Map.of(
                "token", token,
                "nicknameRequired", nicknameRequired
        ));

    }



}