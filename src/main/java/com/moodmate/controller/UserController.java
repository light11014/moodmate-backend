package com.moodmate.controller;

import com.moodmate.domain.user.UserRepository;
import com.moodmate.domain.user.dto.UserProfileRequest;
import com.moodmate.domain.user.dto.UserProfileResponse;
import com.moodmate.domain.user.entity.User;
import com.moodmate.domain.user.ouath.CustomOauth2User;
import com.moodmate.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "User-Controller", description = "회원 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "회원 정보 조회",
            security = @SecurityRequirement(name = "bearer-key"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원 조회 성공",
                            content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content)
            }
    )
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal CustomOauth2User oAuthUser) {
        if (oAuthUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        User user = oAuthUser.getUser();
        return ResponseEntity.ok(new UserProfileResponse(user));
    }

    @PatchMapping("/me")
    @Operation(summary = "회원 닉네임 수정",
            security = @SecurityRequirement(name = "bearer-key"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원 수정 성공",
                            content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content)
            }
    )
    public ResponseEntity<UserProfileResponse> updateUsername(
            @RequestBody @Schema(implementation = UserProfileRequest.class) UserProfileRequest request,
            @AuthenticationPrincipal CustomOauth2User customOauth2User) {
        User user = customOauth2User.getUser();
        user.setUsername(request.username()); // 닉네임 수정
        userRepository.save(user); // DB에 저장
        return ResponseEntity.ok(new UserProfileResponse(user));
    }

    @DeleteMapping("/me")
    @Operation(summary = "회원 삭제",
            security = @SecurityRequirement(name = "bearer-key"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원 삭제 성공", content = @Content),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content)
            }
    )
    public ResponseEntity<?> deleteAccount(@AuthenticationPrincipal CustomOauth2User customOauth2User) {
        User user = customOauth2User.getUser();

        // 사용자 삭제
        userRepository.delete(user);

        return ResponseEntity.ok(Map.of(
                "message", "회원 탈퇴가 완료되었습니다."
        ));
    }
}
