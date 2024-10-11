package com.pingpong.entity;

import lombok.*;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import javax.persistence.Id;

import static lombok.AccessLevel.*;

/**
 * 토큰 재발급을 위한 RefreshToken Entity
 *
 * timeToLive: RefreshToken이 Redis에서 유지되는 시간을 초 단위로 설정
 * 여기서는 21,600초(6시간)로 설정
 * 이 시간이 지나면 해당 RefreshToken은 자동으로 만료되어 삭제
 */
@Builder
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@RedisHash(value = "RefreshTokens", timeToLive = 21600)
public class RefreshToken {

    @Id
    private String token;

    @Indexed
    private String uuid;
}
