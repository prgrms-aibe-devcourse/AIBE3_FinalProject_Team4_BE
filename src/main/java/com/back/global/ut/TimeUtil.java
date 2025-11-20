package com.back.global.ut;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TimeUtil {

    public static String toRelativeTime(LocalDateTime time) {
        long seconds = Duration.between(time, LocalDateTime.now()).getSeconds();

        if (seconds < 60) return "방금 전";
        long minutes = seconds / 60;

        if (minutes < 60) return minutes + "분 전";
        long hours = minutes / 60;

        if (hours < 24) return hours + "시간 전";
        long days = hours / 24;

        if (days < 7) return days + "일 전";
        long weeks = days / 7;

        if (weeks < 4) return weeks + "주 전";
        long months = days / 30;

        if (months < 12) return months + "개월 전";
        long years = days / 365;

        return years + "년 전";
    }

    public static class JsonUtil {

        private static final ObjectMapper objectMapper = new ObjectMapper();

        /**
         * JSON 문자열을 List<String>으로 변환
         */
        public static List<String> toStringList(String json) {
            if (json == null || json.isEmpty()) {
                return new ArrayList<>();
            }
            try {
                return objectMapper.readValue(json, new TypeReference<List<String>>() {});
            } catch (JsonProcessingException e) {
                return new ArrayList<>();
            }
        }

        /**
         * List<String>을 JSON 문자열로 변환
         */
        public static String toJson(List<String> list) {
            try {
                return objectMapper.writeValueAsString(list);
            } catch (JsonProcessingException e) {
                return "[]";
            }
        }
    }
}
