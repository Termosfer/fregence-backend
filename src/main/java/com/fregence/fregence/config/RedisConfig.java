package com.fregence.fregence.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Bean
    RedisCacheConfiguration cacheConfiguration() {
        // Jackson üçün təmiz bir ObjectMapper yaradırıq
        ObjectMapper mapper = new ObjectMapper();
        
        // Java 8 Tarix/Vaxt dəstəyini əlavə edirik
        mapper.registerModule(new JavaTimeModule());
        
        // Məlumatların JSON kimi yazılması üçün serializer hazırlayırıq
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // 1 saatlıq keş ömrü
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(serializer));
    }
}