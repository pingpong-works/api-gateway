package com.pingpong.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ObjectMapper 설정을 위한 구성 클래스
 *
 * 애플리케이션 전체에서 사용될 ObjectMapper 인스턴스를 생성하고 구성
 * ObjectMapper는 JSON과 Java 객체 간의 직렬화 및 역직렬화를 담당하는 Jackson 라이브러리의 핵심 클래스
 *
 * 이 설정을 통해 생성된 ObjectMapper Bean은 다음과 같은 용도로 사용
 * - REST API에서 요청/응답 본문의 JSON 변환
 * - 데이터 저장 및 검색 시 객체-JSON 변환
 * - 서비스 간 통신에서 메시지 직렬화/역직렬화
 */
@Configuration
public class ObjectMapperConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
