package com.back.domain.image.image.dto;


import java.util.List;

public class GoogleSearchResponse {

    // 이미지 결과 목록 (핵심)
    private List<GoogleImageItem> items;

    // Getter and Setter (생략)
    public List<GoogleImageItem> getItems() { return items; }
}
