package com.back.domain.ai.ai.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AiAssistContentType {
    BLOG("blog"),
    SHORLOG("shorlog");

    private final String value;

    AiAssistContentType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}