package com.back.domain.recommend.recommend.util;

import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ElasticsearchDtoMapper {
    private final MapConverter mapConverter;

    public <T> T fromHit(Hit<Map> hit, Class<T> targetClass) {
        Map<String, Object> source = hit.source();

        if (source == null) {
            return null;
        }
        return mapConverter.convert(source, targetClass);
    }

    public <T> T fromSource(Map<String, Object> source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }

        return mapConverter.convert(source, targetClass);
    }
}
