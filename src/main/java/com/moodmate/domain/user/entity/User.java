package com.moodmate.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
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

    public void setUsername(String username) {
        this.username = username;
    }

}
