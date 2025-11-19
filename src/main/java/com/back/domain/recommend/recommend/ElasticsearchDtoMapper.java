package com.back.domain.recommend.recommend;

import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ElasticsearchDtoMapper {
    private final MapConverter converter;

    public <T> T fromHit(Hit<Map> hit, Class<T> targetClass) {
        Map<String, Object> source = hit.source();

        if (source == null) {
            return null;
        }
        return converter.convert(source, targetClass);
    }
}
