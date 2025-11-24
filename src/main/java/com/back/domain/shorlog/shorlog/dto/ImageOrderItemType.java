package com.back.domain.shorlog.shorlog.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ImageOrderItemType {
    FILE("file"),
    URL("url");

    private final String value;

    ImageOrderItemType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ImageOrderItemType from(String input) {
        if (input == null) return null;

        for (ImageOrderItemType type : values()) {
            if (type.value.equalsIgnoreCase(input)) return type;
        }

        return ImageOrderItemType.valueOf(input.toUpperCase());
    }
}