package com.pingpong.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    private final Key key;

    /**
     * JWT 서명에 사용될 키를 초기화
     * @param secretKey application.yml 에서 주입되는 JWT secret key
     */
    public JwtUtils(@Value("${jwt.key}") String secretKey) {
        String base64EncodedSecretKey = Encoders.BASE64.encode(secretKey.getBytes(StandardCharsets.UTF_8));
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64EncodedSecretKey));
    }

    /**
     * JWT 토큰 유효성 검사
     * @param token 검증할 JWT 토큰
     * @return 토큰이 유효하면 true, 아니면 false 반환
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * JWT 토큰에서 Claims를 추출
     * @param token JWT token
     * @return 토큰의 Claims / 토큰이 유효하지 않으면 null 반환
     */
    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * JWT token에서 username 추출
     * @param token JWT token
     * @return 토큰에서 추출한 사용자 이름. 토큰이 유효하지 않으면 null 반환
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * JWT token에서 username 추출
     * @param token JWT token
     * @return 토큰에서 추출한 사용자 이름. 토큰이 유효하지 않으면 null 반환
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.getExpiration() : null;
    }

    /**
     * JWT 토큰 만료 검증
     * @param token JWT token
     * @return 토큰이 만료되었으면 true, 아니면 false 반환
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration != null && expiration.before(new Date());
    }
}
