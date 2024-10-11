package com.pingpong.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;


import static lombok.AccessLevel.*;

/**
 * 무효화 된 토큰을 Redis에서 관리하기 위한 엔티티
 */
@Builder
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@RedisHash(value = "InvalidatedTokens")
public class InvalidatedToken {

    @Id
    private String token;
}
