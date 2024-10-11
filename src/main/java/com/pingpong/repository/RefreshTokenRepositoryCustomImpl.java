package com.pingpong.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pingpong.entity.RefreshToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/**
 * RefreshTokenRepositoryCustom 인터페이스의 구현 클래스
 * Redis를 사용하여 RefreshToken을 저장, 조회, 삭제하는 기능을 제공
 */
@Repository
public class RefreshTokenRepositoryCustomImpl implements RefreshTokenRepositoryCustom {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final long expirationSeconds;

    public RefreshTokenRepositoryCustomImpl(RedisTemplate<String, String> redisTemplate,
                                            ObjectMapper objectMapper,
                                            @Value("${jwt.refresh-token-expiration-minutes}") long expirationSeconds) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.expirationSeconds = expirationSeconds;
    }

    @Override
    public void saveWithExpiration(RefreshToken refreshToken) {
        String key = "RefreshTokens:" + refreshToken.getToken();
        try {
            String value = objectMapper.writeValueAsString(refreshToken);
            redisTemplate.opsForValue().set(key, value, expirationSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save refresh token", e);
        }
    }

    @Override
    public RefreshToken findByTokenInRedis(String token) {
        String key = "RefreshTokens:" + token;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.readValue(value, RefreshToken.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse refresh token", e);
        }
    }

    @Override
    public void deleteByTokenInRedis(String token) {
        String key = "RefreshTokens:" + token;
        redisTemplate.delete(key);
    }
}