package com.bankaya.pokemon.infrastructure.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.extern.log4j.Log4j2;

import jakarta.annotation.Nonnull;

/**
 * Cache Configuration
 * Cache Strategy:
 * - pokemonByName: Cache Pokemon data by name (case-insensitive)
 * - pokemonById: Cache Pokemon data by ID
 * Configuration:
 * - TTL: 10 minutes
 * - Max size: 1000 entries per cache
 * - Scheduled cache clear: Every hour
 */
@Log4j2
@Configuration
@EnableScheduling
public class CacheConfig {

    private static final int CACHE_MAX_SIZE = 1000;
    private static final int CACHE_TTL_MINUTES = 10;

    /**
     * Caffeine cache manager
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("pokemonByName", "pokemonById") {
            @Override
            protected org.springframework.cache.Cache createConcurrentMapCache(@Nonnull String name) {
                return new org.springframework.cache.caffeine.CaffeineCache(
                        name,
                        Caffeine.newBuilder()
                                .maximumSize(CACHE_MAX_SIZE)
                                .expireAfterWrite(Duration.ofMinutes(CACHE_TTL_MINUTES))
                                .recordStats()
                                .build()
                );
            }
        };
    }

    /**
     * Clear all caches every hour to ensure fresh data
     * Runs every hour
     */
    @Scheduled(cron = "0 0 * * * *")
    @CacheEvict(value = {"pokemonByName", "pokemonById"}, allEntries = true)
    public void clearCacheScheduled() {
        log.info("Scheduled cache eviction executed - all caches cleared");
    }

}
