package com.pingpong.filter;

import com.pingpong.common.JwtUtils;
import com.pingpong.exception.BusinessLogicException;
import com.pingpong.exception.ExceptionCode;
import com.pingpong.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;

/**
 * JWT 토큰 재발급을 위한 글로벌 필터
 * 이 필터는 다음과 같은 경우에 토큰을 재발급
 * 1. 토큰 만료 시간이 5분 이내로 남은 경우
 * 2. 토큰이 이미 만료된 경우
 *
 * 재발급 조건을 만족하고 유효한 Refresh Token이 있는 경우, "/auth/refresh" 엔드포인트로 요청을 보내 새 토큰을 발급 받음
 * 새로 발급받은 토큰은 원래 요청의 Authorization 헤더에 설정되어 다음 Filter로 전달됨
 */
@RequiredArgsConstructor
@Component
public class JwtTokenRefreshFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;
    private final TokenService tokenService;

    @Value("${spring.auth.host}")
    private String host;
    @Value("${spring.auth.port}")
    private int port;
    @Value("${jwt.refresh-threshold-minutes:5}")
    private long refreshThresholdMinutes;

    /**
     * JWT 토큰을 처리하고 필요한 경우 재발급하는 필터 메서드
     * @param exchange 현재의 서버 교환
     * @param chain 필터 체인
     * @return 처리된 교환에 대한 Mono
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);
        return processToken(token, exchange, chain);
    }

    /**
     * 주어진 토큰을 처리하고 필요한 경우 재발급을 수행
     * @param token JWT 토큰
     * @param exchange 현재의 서버 교환
     * @param chain 필터 체인
     * @return 처리된 교환에 대한 Mono
     */
    private Mono<Void> processToken(String token, ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!jwtUtils.validateToken(token)) {
            return Mono.error(new BusinessLogicException(ExceptionCode.TOKEN_NOT_AUTHENTICATED));
        }

        if (jwtUtils.isTokenExpired(token)) {
            return refreshTokenIfPossible(token, exchange, chain);
        }

        if (isTokenNearExpiration(token)) {
            return refreshTokenIfPossible(token, exchange, chain);
        }

        return chain.filter(exchange);
    }

    /**
     * 토큰의 만료 시간이 임계값에 가까운지 확인
     * @param token 검사할 JWT 토큰
     * @return 토큰 만료 시간이 임계값보다 작으면 true, 그렇지 않으면 false
     */
    private boolean isTokenNearExpiration(String token) {
        Date expirationDate = jwtUtils.getExpirationDateFromToken(token);
        if (expirationDate == null) {
            return false;
        }
        long timeUntilExpiration = expirationDate.getTime() - System.currentTimeMillis();
        return timeUntilExpiration < (refreshThresholdMinutes * 60 * 1000);
    }

    /**
     * 토큰 재발급이 가능한지 확인하고, 가능하다면 재발급 수행
     * @param token 현재 JWT 토큰
     * @param exchange 현재의 서버 교환
     * @param chain 필터 체인
     * @return 처리된 교환에 대한 Mono
     */
    private Mono<Void> refreshTokenIfPossible(String token, ServerWebExchange exchange, GatewayFilterChain chain) {
        String username = jwtUtils.getUsernameFromToken(token);
        if (username == null) {
            return Mono.error(new BusinessLogicException(ExceptionCode.TOKEN_NOT_CONSISTED_PROPERLY));
        }

        return Mono.fromSupplier(() -> tokenService.refreshTokenExists(username))
                .flatMap(exists -> exists ? refreshToken(token, exchange, chain) : Mono.error(new BusinessLogicException(ExceptionCode.TOKEN_EXPIRED)));
    }

    /**
     * 토큰을 재발급하고 새 토큰으로 요청을 업데이트
     * @param token 현재 JWT 토큰
     * @param exchange 현재의 서버 교환
     * @param chain 필터 체인
     * @return 업데이트된 교환에 대한 Mono
     */
    private Mono<Void> refreshToken(String token, ServerWebExchange exchange, GatewayFilterChain chain) {
        return createWebClient().post()
                .uri("/refresh")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .toBodilessEntity()
                .flatMap(response -> {
                    String newToken = response.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                    if (newToken == null) {
                        return Mono.error(new BusinessLogicException(ExceptionCode.TOKEN_NOT_EXIST));
                    }
                    ServerHttpRequest newRequest = exchange.getRequest().mutate()
                            .header(HttpHeaders.AUTHORIZATION, newToken)
                            .build();
                    return chain.filter(exchange.mutate().request(newRequest).build());
                });
    }

    /**
     * WebClient 인스턴스를 생성
     * @return 구성된 WebClient 인스턴스
     */
    private WebClient createWebClient() {
        return WebClient.builder()
                .baseUrl("http://" + host + ":" + port)
                .build();
    }

    /**
     * 실행 순서 정의
     * @return 필터 순서 (낮을수록 먼저 실행)
     */
    @Override
    public int getOrder() {
        return -1;
    }

}
