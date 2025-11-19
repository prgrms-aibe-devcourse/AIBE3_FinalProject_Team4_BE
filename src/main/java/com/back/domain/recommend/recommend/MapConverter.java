package com.back.domain.recommend.recommend;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class MapConverter {

    private final ObjectMapper objectMapper;

    public <T> T convert(Map<String, Object> source, Class<T> targetClass) {
        try {
            return objectMapper.convertValue(source, targetClass);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Map → " + targetClass.getSimpleName() + " 변환 실패", e);
        }
    }
}
