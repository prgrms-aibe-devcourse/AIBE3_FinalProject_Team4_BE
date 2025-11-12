package com.back.domain.ai.ai.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AiGenerateContentType {
    BLOG("blog"),
    SHORLOG("shorlog");

    private final String value;

    AiGenerateContentType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}