package com.back.domain.ai.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AiModel {
    GPT_4O_MINI("gpt-4o-mini"),
    GPT_4_1_MINI("gpt-4.1-mini"),
    GPT_5_MINI("gpt-5-mini");

    private final String value;

    AiModel(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AiModel from(String input) {
        if (input == null) return null;

        for (AiModel m : values()) {
            if (m.value.equalsIgnoreCase(input)) return m;
        }

        throw new IllegalArgumentException("유효하지 않은 AI 모델: " + input);
    }
}
