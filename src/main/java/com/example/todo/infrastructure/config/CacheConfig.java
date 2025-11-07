package com.example.todo.infrastructure.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

/**
 * CacheConfig
 *
 * Configuration du cache Redis pour optimiser les performances.
 * Définit les stratégies de cache par type de données.
 *
 * @author Todo Team
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // Configuration par défaut du cache
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .entryTtl(Duration.ofMinutes(10)) // TTL par défaut : 10 minutes
            .disableCachingNullValues();

        // Configurations spécifiques par cache
        Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
            // Cache pour les Todos individuels - TTL court car données fréquemment modifiées
            "todos", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)),

            // Cache pour les listes de Todos - TTL plus long car pagination stable
            "todoLists", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)),

            // Cache pour les listes filtrées par statut
            "todoListsByStatus", defaultCacheConfig.entryTtl(Duration.ofMinutes(10)),

            // Cache pour les statistiques - TTL long car calculs coûteux
            "statistics", defaultCacheConfig.entryTtl(Duration.ofHours(1)),

            // Cache pour les données de référence (configurations, etc.)
            "reference", defaultCacheConfig.entryTtl(Duration.ofHours(24))
        );

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaultCacheConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
}
