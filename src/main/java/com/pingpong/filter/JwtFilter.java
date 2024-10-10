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

        // 인가 처리가 필요 없는 경우 필터링
        if (!url.startsWith("/auth/")) {
            return chain.filter(exchange);
        }

        // access 토큰이 필요한데 없는 경우는 에러 발생
        if (Objects.isNull(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))) {
            throw new BusinessLogicException(ExceptionCode.TOKEN_NOT_EXIST);
        }

        String access = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION).trim().split(" ")[1];

        // token이 서버가 발행한게 맞는지, 혹은 만료되지 않았는지 확인
        if (jwtUtils.getValidation(access)) {
            throw new BusinessLogicException(ExceptionCode.TOKEN_NOT_AUTHENTICATED);
        }

        // 경로 재작성 Logic
        // "/auth/serviceName/*/*" -> "/*/*"
        if (url.startsWith("/auth/")) {
            String[] strs = url.split("/");
            String newPath = "/" + String.join("/", Arrays.copyOfRange(strs, 2, strs.length));
            ServerHttpRequest newRequest = exchange.getRequest().mutate().path(newPath).build();
            return chain.filter(exchange.mutate().request(newRequest).build());
        }

        // 모든 과정들을 만족하지 않는 경우
        return chain.filter(exchange).then(Mono.defer(() -> {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            DataBuffer buffer = response.bufferFactory().wrap("Not Match Any Conditions.".getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }));
    }
}
