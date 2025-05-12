package com.moodmate.controller;

import com.moodmate.dto.UserProfileDto;
import com.moodmate.entity.Member;
import com.moodmate.oauth.CustomOauth2User;
import com.moodmate.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
//        System.out.println("[DEBUG] User lookup successful: " + member.getEmail());
//        System.out.println("[DEBUG] Username: " + member.getUsername());  // 추가된 로그
//        System.out.println("[DEBUG] Picture URL: " + member.getPictureUrl());  // 추가된 로그

        return ResponseEntity.ok(new UserProfileDto(member));
    }

    // 닉네임 수정 API
    @PatchMapping("/me")
    public ResponseEntity<UserProfileDto> updateUsername(@RequestBody Map<String, String> request, @AuthenticationPrincipal CustomOauth2User customOauth2User) {
        String newUsername = request.get("newUsername");
        Member member = customOauth2User.getMember();

        // 새로운 닉네임이 유효한지 검사
        if (newUsername == null || newUsername.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        member.setUsername(newUsername); // 닉네임 수정
        memberRepository.save(member); // DB에 저장

        return ResponseEntity.ok(new UserProfileDto(member));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> deleteAccount(@AuthenticationPrincipal CustomOauth2User customOauth2User) {
        Member member = customOauth2User.getMember();

        // 사용자 삭제
        memberRepository.delete(member);

//        // JWT 토큰 만료 처리를 클라이언트에서 할 수 있으므로, 삭제만 처리
////        return ResponseEntity.noContent().build();  // 204 No Content 응답

        return ResponseEntity.ok(Map.of(
                "message", "회원 탈퇴가 완료되었습니다."
        ));
    }
}
