package com.pingpong.config;

import com.pingpong.filter.JwtFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteLocatorConfig {
    /**
     * 각 서비스로 라우팅 및 로드밸런싱 설정을 위한 빈
     * @apiNote rewrite filter를 통해 서비스 구분에 사용되는 경로를 제거하여 서비스로 라우팅
     * @apiNote JWT token을 필요로 하는 api의 경우 /auth 경로를 사용하고, 해당 토큰을 검사하는 filter를 추가
     * @apiNote Feign 클라이언트를 사용하는 서비스 간 통신을 고려하여 설정
     * @param builder RouteLocatorBuilder
     * @param jwtFilter JWT 토큰 검증을 위한 필터
     * @return RouteLocator
     */
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder, JwtFilter jwtFilter) {
        return builder.routes()
                // Auth Service (8081)
                .route("auth-service", r -> r.path("/auth/**")
                        .filters(f -> f.filter(jwtFilter).rewritePath("/auth/(?<segment>.*)", "/${segment}"))
                        .uri("lb://AUTH-API"))

                // Core Service (8082)
                .route("core-service", r -> r.path("/cores/**")
                        .filters(f -> f.rewritePath("/cores/(?<segment>.*)", "/${segment}"))
                        .uri("lb://CORE-API"))

                // Mail Service (8083)
                .route("mail-service", r -> r.path("/mails/**")
                        .filters(f -> f.rewritePath("/mails/(?<segment>.*)", "/${segment}"))
                        .uri("lb://MAIL-API"))

                // Util Service (8084)
                .route("util-service", r -> r.path("/utils/**")
                        .filters(f -> f.rewritePath("/utils/(?<segment>.*)", "/${segment}"))
                        .uri("lb://UTIL-API"))

                // Chat Service (8085)
                .route("chat-service", r -> r.path("/chats/**")
                        .filters(f -> f.rewritePath("/chats/(?<segment>.*)", "/${segment}"))
                        .uri("lb://CHAT-API"))

                // Alarm Service (8086)
                .route("alarm-service", r -> r.path("/alarms/**")
                        .filters(f -> f.rewritePath("/alarms/(?<segment>.*)", "/${segment}"))
                        .uri("lb://ALARM-API"))

                // Frontend (5173)
                .route("frontend", r -> r.path("/**")
                        .uri("http://localhost:5173"))
                .build();
    }
}