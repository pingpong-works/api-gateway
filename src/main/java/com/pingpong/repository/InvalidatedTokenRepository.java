package com.pingpong.repository;

import com.pingpong.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {

    /**
     * 주어진 토큰 문자열로 Redis에서 무효화된 토큰을 조회
     *
     * @param token 조회할 토큰 문자열
     * @return 조회된 InvalidatedToken 객체, 없으면 null
     */
    InvalidatedToken findByTokenInRedis(String token);
}
