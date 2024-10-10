package com.pingpong.common;

import com.pingpong.exception.ExceptionCode;
import io.jsonwebtoken.JwtException;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * API Gateway에서 발생하는 토큰 관련 예외를 처리
 * 기존 ExceptionCode enum을 사용하여 상태 코드와 메시지를 정의
 */
@Component
public class CustomGlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    public CustomGlobalExceptionHandler(ErrorAttributes errorAttributes,
                                        ApplicationContext applicationContext,
                                        ServerCodecConfigurer serverCodecConfigurer) {
        super(errorAttributes, new WebProperties.Resources(), applicationContext);
        super.setMessageWriters(serverCodecConfigurer.getWriters());
        super.setMessageReaders(serverCodecConfigurer.getReaders());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    /**
     * 에러 응답 시 status와 message를 ExceptionCode enum을 기반으로 반환
     *
     * @param request 서버 Request
     * @return Mono<ServerResponse> 에러 응답
     */
    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable error = getError(request);
        ExceptionCode exceptionCode = mapToExceptionCode(error);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", exceptionCode.getStatusCode());
        responseBody.put("message", exceptionCode.getStatusDescription());

        return ServerResponse.status(exceptionCode.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(responseBody));
    }

    /**
     * 발생한 예외를 ExceptionCode enum에 매핑
     * @param throwable 발생한 예외
     * @return ExceptionCode 매핑된 예외 코드
     */
    private ExceptionCode mapToExceptionCode(Throwable throwable) {
        if (throwable instanceof IllegalArgumentException) {
            return ExceptionCode.TOKEN_NOT_CONSISTED_PROPERLY;
        } else if (throwable instanceof AuthenticationException) {
            return ExceptionCode.TOKEN_NOT_EXIST;
        } else if (throwable instanceof SecurityException) {
            return ExceptionCode.TOKEN_NOT_AUTHENTICATED;
        } else if (throwable instanceof JwtException) {
            return ExceptionCode.TOKEN_EXPIRED;
        } else {
            // 기본적으로 토큰이 올바르게 구성되지 않은 것으로 처리
            return ExceptionCode.TOKEN_NOT_CONSISTED_PROPERLY;
        }
    }
}
