package com.pingpong.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ExceptionCode {

    // 토큰이 올바르게 구성되지 않은 경우 에러 발생
    TOKEN_NOT_CONSISTED_PROPERLY(400, "Token is not properly constructed"),

     // 토큰이 없는 경우 에러 발생
    TOKEN_NOT_EXIST(401, "Access Token Not Exists"),

     // 서버가 발행한 토큰이 아닐 경우 예외 발생
    TOKEN_NOT_AUTHENTICATED(401, "Not Authenticated Token"),

     // access 혹은 refresh 토큰이 만료됐을 경우 에러 발생
    TOKEN_EXPIRED(401, "Token has expired");

    @Getter
    private int statusCode;

    @Getter
    private String statusDescription;
}
