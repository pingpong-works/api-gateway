package com.pingpong.filter;

import com.pingpong.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * HttpServletRequest로 들어온 요청의 JWT가 무효화 된 토큰인지 확인하는 필터
 */
@RequiredArgsConstructor
@Component
public class GlobalInvalidatedTokenFilter implements GlobalFilter, Ordered {

    private final TokenService tokenService;

    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * request를 검사해 access token이 있는지, 무효화된 토큰인지 확인하는 필터
     * @param exchange ServerWebExchange
     * @param chain GatewayFilterChain
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        return extractToken(request)
                .map(token -> validateToken(token, exchange, chain))
                .orElseGet(() -> chain.filter(exchange));
    }

    /**
     * HTTP 요청 헤더에서 Bearer 토큰을 추출
     * @param request ServerHttpRequest
     * @return Optional<String> 추출된 토큰
     */
    private Optional<String> extractToken(ServerHttpRequest request) {
        return Optional.ofNullable(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(authHeader -> authHeader.startsWith(BEARER_PREFIX))
                .map(authHeader -> authHeader.substring(BEARER_PREFIX.length()));
    }

    /**
     * 토큰의 유효성을 검사하고 적절한 응답을 반환
     * @param token 검증할 토큰
     * @param exchange ServerWebExchange
     * @param chain GatewayFilterChain
     * @return Mono<Void>
     */
    private Mono<Void> validateToken(String token, ServerWebExchange exchange, GatewayFilterChain chain) {
        return tokenService.isTokenInvalidated(token)
                .flatMap(isInvalidated -> {
                    if (isInvalidated) {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    } else {
                        return chain.filter(exchange);
                    }
                })
                .onErrorResume(e -> {
                    exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    return exchange.getResponse().setComplete();
                });
    }

    /**
     * 필터의 우선순위를 설정 (높은 우선순위)
     * @return int 필터 순서
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

}
