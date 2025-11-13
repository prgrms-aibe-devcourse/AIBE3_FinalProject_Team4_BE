package com.back.domain.ai.ai.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AiGenerateMode {
    TITLE("title"),
    HASHTAG("hashtag"),
    SUMMARY("summary"),
    KEYWORD("keyword");

    private final String value;

    AiGenerateMode(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}