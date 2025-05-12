package com.moodmate.dto;

import com.moodmate.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfileDto {
    private String email;
    private String username;
    private String pictureUrl;

    public UserProfileDto(Member member) {
        this.email = member.getEmail();
        this.username = member.getUsername();
        this.pictureUrl = member.getPictureUrl();
    }
}
