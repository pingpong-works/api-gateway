package com.pingpong.property;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Redis 연결을 위한 설정 정보를 담고 있는 클래스
 * Redis 서버의 호스트, 포트, 데이터베이스 번호, 비밀번호
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RedisProperties {
    private String host;
    private Integer port;
    private Integer database;
    private String password;
}
