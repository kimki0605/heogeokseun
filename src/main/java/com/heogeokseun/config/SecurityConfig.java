
package com.heogeokseun.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.heogeokseun.mapper.MemberMapper;
import com.heogeokseun.security.JwtAuthenticationFilter;
import com.heogeokseun.security.JwtTokenProvider;

/**
 * 허걱슨닷컴 Spring Security 설정 클래스.
 *
 * JWT 인증 필터 등록, 기존 로그인 기능 유지,
 * 비밀번호 암호화 객체 등록을 담당한다.
 */
@Configuration
public class SecurityConfig {

    /**
     * 회원 비밀번호를 BCrypt 알고리즘으로 암호화한다.
     *
     * MemberService에서 PasswordEncoder를 주입받아
     * 회원가입 및 로그인 시 사용한다.
     *
     * 기존 회원의 BCrypt 비밀번호와도 호환된다.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * JWT 인증 필터를 Spring Bean으로 등록한다.
     *
     * JwtTokenProvider는 JWT를 검증하고,
     * MemberMapper는 회원 정보를 DB에서 조회한다.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            MemberMapper memberMapper) {

        return new JwtAuthenticationFilter(
                jwtTokenProvider,
                memberMapper
        );
    }

    /**
     * Spring Security의 HTTP 요청 보안 설정.
     *
     * JWT 전환 작업 중에는 기존 페이지가 차단되지 않도록
     * 임시로 모든 요청을 허용한다.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {

        http
            // JWT 전환이 완료되기 전까지 임시로 접근을 허용한다.
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )

            // 기존 Thymeleaf 로그인 화면을 사용한다.
            .formLogin(form -> form.disable())

            // 기존 로그아웃 컨트롤러를 사용한다.
            .logout(logout -> logout.disable())

            // JWT 인증 필터를 Spring Security 필터 체인에 등록한다.
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
