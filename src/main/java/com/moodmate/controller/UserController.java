package com.moodmate.controller;

import com.moodmate.entity.Member;
import com.moodmate.oauth.CustomOauth2User;
import com.moodmate.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final MemberRepository memberRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal CustomOauth2User oAuthUser) {
        if (oAuthUser == null) {
            System.out.println("[DEBUG] No authenticated user: Token authentication failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        Member member = oAuthUser.getMember();
        System.out.println("[DEBUG] User lookup successful: " + member.getEmail());
        System.out.println("[DEBUG] Username: " + member.getUsername());  // 추가된 로그
        System.out.println("[DEBUG] Picture URL: " + member.getPictureUrl());  // 추가된 로그

        return ResponseEntity.ok(Map.of(
                "username", member.getUsername(),
                "email", member.getEmail(),
                "pictureUrl", member.getPictureUrl()
        ));
    }

    // 닉네임 수정 API
    @PatchMapping("/api/users/me")
    public ResponseEntity<Member> updateNickname(@RequestBody Map<String, String> request, @AuthenticationPrincipal CustomOauth2User customOauth2User) {
        String newNickname = request.get("nickname");
        Member member = customOauth2User.getMember();

        // 새로운 닉네임이 유효한지 검사
        if (newNickname == null || newNickname.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        member.setUsername(newNickname); // 닉네임 수정
        memberRepository.save(member); // DB에 저장

        return ResponseEntity.ok(member); // 수정된 사용자 정보 반환
    }

    @DeleteMapping("/api/users/me")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal CustomOauth2User customOauth2User) {
        Member member = customOauth2User.getMember();

        // 사용자 삭제
        memberRepository.delete(member);

        // JWT 토큰 만료 처리를 클라이언트에서 할 수 있으므로, 삭제만 처리
        return ResponseEntity.noContent().build();  // 204 No Content 응답
    }

}
