package com.back.domain.ai.ai.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AiAssistMode {
    TITLE("title"),
    HASHTAG("hashtag"),
    SUMMARY("summary"),
    KEYWORD("keyword"),
    CHAT("chat");

    private final String value;

    AiAssistMode(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}