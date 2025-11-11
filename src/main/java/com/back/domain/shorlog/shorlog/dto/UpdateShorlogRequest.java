package com.back.domain.shorlog.shorlog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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

    @NotEmpty(message = "섬네일은 최소 1개 이상 필수입니다.")
    @Size(min = 1, max = 10, message = "섬네일은 최소 1개, 최대 10개까지 가능합니다.")
    private List<String> thumbnailUrls;

    @Size(max = 10, message = "해시태그는 최대 10개까지 가능합니다.")
    @Builder.Default
    private List<String> hashtags = new ArrayList<>();
}

