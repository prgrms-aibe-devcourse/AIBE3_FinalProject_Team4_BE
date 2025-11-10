package com.back.project.domain.shorlog.shorlogdraft.dto;

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
public class CreateDraftRequest {

    @Size(max = 800, message = "내용은 최대 800자까지 입력 가능합니다.")
    private String content;

    private String thumbnailUrl;

    @Size(max = 10, message = "해시태그는 최대 10개까지 가능합니다.")
    @Builder.Default
    private List<String> hashtags = new ArrayList<>();
}

