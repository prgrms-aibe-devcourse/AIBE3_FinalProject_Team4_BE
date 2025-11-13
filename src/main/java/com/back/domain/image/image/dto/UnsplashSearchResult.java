package com.back.domain.image.image.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class UnsplashSearchResult {
    private int total;

    @JsonProperty("total_pages") // JSON 필드명과 자바 필드명이 다를 때 사용
    private int totalPages;

    @Getter
    @Setter
    // 핵심: UnsplashPhoto 목록을 담는 필드
    private List<UnsplashPhoto> results;
}
