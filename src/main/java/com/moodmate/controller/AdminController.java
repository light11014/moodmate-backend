package com.moodmate.controller;

import com.moodmate.domain.user.UserRepository;
import com.moodmate.domain.user.entity.Role;
import com.moodmate.domain.user.entity.User;
import com.moodmate.domain.user.ouath.CustomOauth2User;
import com.moodmate.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/check")
    public ResponseEntity<Void> checkAdmin() {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof CustomOauth2User customUser)) {
            return ResponseEntity.status(401).build(); // 인증되지 않은 사용자
        }

        Long userId = customUser.getUser().getId(); // getUser() 이용
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (user.getRole() == Role.ADMIN) {
            return ResponseEntity.ok().build(); // 200
        } else {
            return ResponseEntity.notFound().build(); // 404
        }
    }
}
