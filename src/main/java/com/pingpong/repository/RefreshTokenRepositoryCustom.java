package com.pingpong.repository;

import com.pingpong.entity.RefreshToken;

/**
 * RefreshToken을 Redis에 저장, 조회, 삭제하는 Custom Repository
 * JPA Repository와 함께 사용되어 RefreshToken의 Redis 관련 작업을 처리합니다.
 */
public interface RefreshTokenRepositoryCustom {
    void saveWithExpiration(RefreshToken refreshToken);
    RefreshToken findByTokenInRedis(String token);
    void deleteByTokenInRedis(String token);
}
