package com.pingpong.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 각각의 서비스로 라우팅 및 로드밸런싱 설정
 * @apiNote rewrite filter 를 통해 서비스 구분에 사용되는 경로를 제거하여 서비스로 라우팅
 * @apiNote JWT Token 을 필요로 하는 api의 경우 url로 구분하고, 해당 토큰을 검사하는 filter 추가 (예정)
 */
@Configuration
public class FilterConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r.path("/chat-api/**")
                        .filters(f -> f.addRequestHeader("chat-request", "chat-request-header")
                                       .addResponseHeader("chat-response", "chat-response-header"))
                        .uri("http://localhost:8081"))
                .build();
    }
}
