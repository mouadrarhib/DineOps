package com.mouad.dineops.dineOps.common.config;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

@Configuration
@EnableCaching
public class CacheConfig {

	public static final String MENU_CATEGORIES_BY_BRANCH = "menuCategoriesByBranch";
	public static final String MENU_ITEMS_BY_BRANCH = "menuItemsByBranch";
	public static final String BRANCH_DETAILS = "branchDetails";

	@Value("${app.cache.ttl.menu-categories-minutes:10}")
	private long menuCategoriesTtlMinutes;

	@Value("${app.cache.ttl.menu-items-minutes:5}")
	private long menuItemsTtlMinutes;

	@Value("${app.cache.ttl.branch-details-minutes:30}")
	private long branchDetailsTtlMinutes;

	@Bean
	@ConditionalOnProperty(name = "app.cache.enabled", havingValue = "false", matchIfMissing = false)
	CacheManager noOpCacheManager() {
		return new NoOpCacheManager();
	}

	@Bean
	@ConditionalOnProperty(
			name = { "app.cache.enabled", "app.cache.redis-enabled" },
			havingValue = "true")
	CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
		RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
				.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

		return RedisCacheManager.builder(redisConnectionFactory)
				.cacheDefaults(defaultConfig)
				.withCacheConfiguration(MENU_CATEGORIES_BY_BRANCH, defaultConfig.entryTtl(Duration.ofMinutes(menuCategoriesTtlMinutes)))
				.withCacheConfiguration(MENU_ITEMS_BY_BRANCH, defaultConfig.entryTtl(Duration.ofMinutes(menuItemsTtlMinutes)))
				.withCacheConfiguration(BRANCH_DETAILS, defaultConfig.entryTtl(Duration.ofMinutes(branchDetailsTtlMinutes)))
				.build();
	}

	@Bean
	@ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true", matchIfMissing = true)
	@ConditionalOnProperty(name = "app.cache.redis-enabled", havingValue = "false", matchIfMissing = true)
	CacheManager inMemoryCacheManager() {
		return new ConcurrentMapCacheManager(MENU_CATEGORIES_BY_BRANCH, MENU_ITEMS_BY_BRANCH, BRANCH_DETAILS);
	}

	@Bean
	@ConditionalOnProperty(name = "app.cache.enabled", matchIfMissing = true, havingValue = "true")
	List<String> cacheNames() {
		return List.of(MENU_CATEGORIES_BY_BRANCH, MENU_ITEMS_BY_BRANCH, BRANCH_DETAILS);
	}
}
