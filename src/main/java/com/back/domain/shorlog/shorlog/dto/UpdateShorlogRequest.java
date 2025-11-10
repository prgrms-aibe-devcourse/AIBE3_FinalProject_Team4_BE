package com.back.domain.shorlog.shorlog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateShorlogRequest {

    @NotBlank(message = "내용은 필수입니다.")
    @Size(max = 800, message = "내용은 최대 800자까지 입력 가능합니다.")
    private String content;

    @NotBlank(message = "섬네일은 필수입니다.")
    private String thumbnailUrl;

    @NotBlank(message = "섬네일 타입은 필수입니다.")
    @Pattern(regexp = "^(upload|blog|ai)$", message = "섬네일 타입은 'upload', 'blog', 'ai' 중 하나여야 합니다.")
    private String thumbnailType;

    @Size(max = 10, message = "해시태그는 최대 10개까지 가능합니다.")
    @Builder.Default
    private List<String> hashtags = new ArrayList<>();
}

