
package com.heogeokseun.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;

/**
 * JWT 생성 및 검증을 담당하는 클래스.
 *
 * 로그인에 성공한 회원에게 JWT를 발급하고,
 * 이후 요청에서 전달받은 JWT가 유효한지 확인한다.
 */
@Component
public class JwtTokenProvider {

    // JWT 서명 및 검증에 사용하는 비밀 키
    private final SecretKey secretKey;

    // Access Token 유효 시간 (밀리초)
    private final long accessTokenExpiration;

    /**
     * application.properties에서 JWT 설정을 가져온다.
     *
     * jwt.secret: Base64로 인코딩된 비밀 키
     * jwt.access-token-expiration: 토큰 유효 시간
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration) {

        // Base64 문자열을 원래의 비밀 키 바이트 배열로 변환한다.
        byte[] keyBytes = Decoders.BASE64.decode(secret);

        // HS256 서명에 사용할 SecretKey 객체를 생성한다.
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);

        this.accessTokenExpiration = accessTokenExpiration;
    }

    /**
     * 회원 고유 번호를 이용해 JWT Access Token을 발급한다.
     *
     * @param memberNo 로그인한 회원의 고유 번호
     * @return 생성된 JWT 문자열
     */
    public String createAccessToken(Long memberNo) {

        Date now = new Date();

        // 현재 시간에 토큰 유효 시간을 더해 만료 시간을 계산한다.
        Date expiration = new Date(
                now.getTime() + accessTokenExpiration
        );

        return Jwts.builder()
                // 토큰의 주체(subject)에 회원 고유 번호를 저장한다.
                .subject(String.valueOf(memberNo))

                // 토큰 발급 시간
                .issuedAt(now)

                // 토큰 만료 시간
                .expiration(expiration)

                // 비밀 키를 사용해 토큰에 서명한다.
                .signWith(secretKey, Jwts.SIG.HS256)

                // 최종 JWT 문자열 생성
                .compact();
    }

    /**
     * JWT가 유효한지 검증한다.
     *
     * 서명이 잘못되었거나 만료된 토큰은 false를 반환한다.
     *
     * @param token 검증할 JWT
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {

        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            // JWT 서명과 만료 시간을 검증한다.
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (JwtException | IllegalArgumentException e) {
            // 만료, 서명 오류, 형식 오류 등은 유효하지 않은 토큰으로 처리한다.
            return false;
        }
    }

    /**
     * 검증된 JWT에서 회원 고유 번호를 추출한다.
     *
     * @param token JWT 문자열
     * @return 회원 고유 번호
     */
    public Long getMemberNo(String token) {

        // JWT 서명을 검증한 후 Claims를 가져온다.
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // subject에 저장된 회원 번호를 Long으로 변환한다.
        return Long.valueOf(claims.getSubject());
    }
}
