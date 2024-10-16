package com.pingpong.repository;

import com.pingpong.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String>, RefreshTokenRepositoryCustom {
    Optional<RefreshToken> findByUuid(String uuid);
}
