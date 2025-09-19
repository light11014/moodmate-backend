package com.moodmate.domain.user.entity;

import com.moodmate.domain.diary.entity.Diary;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String username;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "login_id", nullable = false)
    private String loginId; // loginId = provider + "_" + provider_id

    @Column(nullable = false)
    private String provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(nullable = false)
    private String email;

    private String pictureUrl;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Diary> diaries = new ArrayList<>();

    // 편의 메서드
    public void addDiary(Diary diary) {
        diaries.add(diary);
        diary.setUser(this);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public static User createOAuthUser(String loginId, String provider, String providerId,
                                       Role role, String pictureUrl, String email) {
        User user = new User();
        user.loginId = loginId;
        user.provider = provider;
        user.providerId = providerId;
        user.role = role;
        user.pictureUrl = pictureUrl;
        user.email = email;
        user.username = null;
        return user;
    }

}
