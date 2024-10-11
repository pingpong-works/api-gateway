package com.pingpong.service;

import com.pingpong.entity.RefreshToken;
import com.pingpong.exception.BusinessLogicException;
import com.pingpong.exception.ExceptionCode;
import com.pingpong.repository.InvalidatedTokenRepository;
import com.pingpong.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class TokenService {

    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken getRefreshByUuid(String uuid) {
        return refreshTokenRepository.findByUuid(uuid)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.TOKEN_NOT_EXIST));
    }

    public Mono<Boolean> isTokenInvalidated(String token) {
        return Mono.fromSupplier(() -> invalidatedTokenRepository.findByTokenInRedis(token) != null);
    }

    public Boolean refreshTokenExists(String uuid) {
        return refreshTokenRepository.findByUuid(uuid).isPresent();
    }
}
