package com.back.domain.image.image.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class Urls {
    @JsonProperty("raw")
    private String raw;

    @JsonProperty("full")
    private String full;

    // Getter and Setter (생략)
    @Setter
    @Getter
    @JsonProperty("regular")
    private String regular; // 일반적으로 사용할 중간 크기 이미지 URL

    @JsonProperty("small")
    private String small;

}