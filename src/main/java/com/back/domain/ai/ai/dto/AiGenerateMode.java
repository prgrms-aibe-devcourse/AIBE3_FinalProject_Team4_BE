package com.back.domain.ai.ai.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AiGenerateMode {
    TITLE("title"),
    HASHTAG("hashtag"),
    SUMMARY("summary"),
    KEYWORD("keyword"),
    KEYWORD_FOR_UNSPLASH("keywordForUnsplash"),
    KEYWORD_FOR_GOOGLE("keywordForGoogle"),
    THUMBNAIL_TEXT("thumbnailText");

    private final String value;

    AiGenerateMode(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}