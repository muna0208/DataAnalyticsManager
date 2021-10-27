package com.daumsoft.dataAnalyticsManager.config;

// *** redis session clustering
// @Configuration
// @EnableRedisHttpSession
public class RedisConfig {

	/*
	 *** redis session clustering
	 * 
	 * @Value("${spring.redis.host}") private String redisHost;
	 * 
	 * @Value("${spring.redis.port}") private int port;
	 * 
	 * @Bean public RedisConnectionFactory redisConnectionFactory() {
	 * RedisStandaloneConfiguration redisStandaloneConfiguration = new
	 * RedisStandaloneConfiguration();
	 * redisStandaloneConfiguration.setHostName(redisHost);
	 * redisStandaloneConfiguration.setPort(port); // 패스워드 있으면 설정 //
	 * redisStandaloneConfiguration.setPassword(redisPwd); LettuceConnectionFactory
	 * lettuceConnectionFactory = new
	 * LettuceConnectionFactory(redisStandaloneConfiguration); return
	 * lettuceConnectionFactory; }
	 * 
	 * 
	 * @Bean public RedisTemplate<String, Object> redisTemplate(){
	 * RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
	 * redisTemplate.setConnectionFactory(redisConnectionFactory());
	 * redisTemplate.setKeySerializer(new StringRedisSerializer()); return
	 * redisTemplate; }
	 */
}
