package com.moodmate.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UserProfileRequest (
        @NotBlank
        @Schema(description = "변경할 닉네임", example = "new-username")
        String username
){
}