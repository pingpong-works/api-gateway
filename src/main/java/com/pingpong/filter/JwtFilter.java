package com.pingpong.filter;

import com.pingpong.common.JwtUtils;
import com.pingpong.exception.BusinessLogicException;
import com.pingpong.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * 인증이 필요한 URL로 요청이 들어온 경우, 인가 처리를 위한 필터
 */
@Component
@RequiredArgsConstructor
public class JwtFilter implements GatewayFilter {

    private final JwtUtils jwtUtils;

    /**
     * 토큰의 signature, expiration 등을 확인하는 필터
     * 요청이 일치하면 경로 재지정 수행
     * @param exchange, chain
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String url = exchange.getRequest().getURI().getPath();

        if (!url.startsWith("/auth/")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "No valid token found", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        try {
            if (!jwtUtils.getValidation(token)) {
                return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return onError(exchange, "Error validating token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }

        // 경로 재작성 Logic
        if (url.startsWith("/auth/")) {
            String[] strs = url.split("/");
            String newPath = "/" + String.join("/", Arrays.copyOfRange(strs, 2, strs.length));
            ServerHttpRequest newRequest = exchange.getRequest().mutate().path(newPath).build();
            return chain.filter(exchange.mutate().request(newRequest).build());
        }

        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json");
        DataBuffer buffer = response.bufferFactory().wrap(err.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
