package com.moodmate.domain.user.controller;

import com.moodmate.domain.user.UserRepository;
import com.moodmate.domain.user.entity.Role;
import com.moodmate.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/check")
    public ResponseEntity<Void> checkAdmin(@RequestParam Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (user.getRole() == Role.ADMIN) {
            return ResponseEntity.ok().build(); // 200
        } else {
            return ResponseEntity.notFound().build(); // 404
        }
    }
}
