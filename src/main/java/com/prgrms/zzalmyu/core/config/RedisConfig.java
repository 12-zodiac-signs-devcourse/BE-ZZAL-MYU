package com.prgrms.zzalmyu.core.config;

import com.prgrms.zzalmyu.domain.image.presentation.dto.res.ImageResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis1.host}")
    private String host;

    @Value("${spring.data.redis1.port}")
    private int port1;

    @Value("${spring.data.redis2.port}")
    private int port2;

    // RedisProperties로 yaml에 저장한 host, post를 연결
    @Bean
    public RedisConnectionFactory defaultRedisConnectionFactory() {
        return new LettuceConnectionFactory(host, port1);
    }

    @Bean
    public RedisConnectionFactory chatRedisConnectionFactory() {
        return new LettuceConnectionFactory(host, port2);
    }

    // serializer 설정으로 redis-cli를 통해 직접 데이터를 조회할 수 있도록 설정
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(defaultRedisConnectionFactory());

        return redisTemplate;
    }
    //이미지 저장용
    @Bean
    public RedisTemplate<String, ImageResponseDto> imageRedisTemplate() {
        RedisTemplate<String, ImageResponseDto> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer(ImageResponseDto.class));
        redisTemplate.setConnectionFactory(defaultRedisConnectionFactory());

        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, String> chatRedisTemplate() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(chatRedisConnectionFactory());

        return redisTemplate;
    }
}
