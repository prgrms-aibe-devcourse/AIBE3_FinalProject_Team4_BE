package com.back.global.config.redis;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheConfiguration cacheConfig() {

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer();

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(1))     // 기본 TTL 1분
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                );
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        Map<String, RedisCacheConfiguration> configMap = new HashMap<>();

        configMap.put("main:popular", cacheConfig().entryTtl(Duration.ofMinutes(1)));
        configMap.put("main:hashtags", cacheConfig().entryTtl(Duration.ofMinutes(5)));
        configMap.put("main:recommended", cacheConfig().entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .withInitialCacheConfigurations(configMap)
                .cacheDefaults(cacheConfig())
                .build();
    }
}
