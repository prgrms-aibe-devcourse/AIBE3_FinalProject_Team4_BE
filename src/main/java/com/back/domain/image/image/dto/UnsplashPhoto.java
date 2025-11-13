package com.back.domain.image.image.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnsplashPhoto {
    private String id;
    private String description;
    // API 응답 구조에 맞게 URLs 객체를 포함
    private Urls urls;
}