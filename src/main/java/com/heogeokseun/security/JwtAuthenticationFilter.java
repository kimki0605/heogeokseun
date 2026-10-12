
package com.heogeokseun.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.heogeokseun.domain.Member;
import com.heogeokseun.mapper.MemberMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT 인증 필터.
 *
 * 브라우저의 쿠키에서 JWT를 읽고 검증한 뒤,
 * 인증된 회원 정보를 Spring Security에 등록한다.
 *
 * OncePerRequestFilter를 상속하여
 * 일반적인 요청 처리 과정에서 한 번 실행되도록 한다.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // JWT 생성 및 검증을 담당하는 클래스
    private final JwtTokenProvider jwtTokenProvider;

    // 회원 정보를 Oracle DB에서 조회하는 MyBatis 매퍼
    private final MemberMapper memberMapper;

    /**
     * 생성자를 통해 필요한 객체를 전달받는다.
     */
    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            MemberMapper memberMapper) {

        this.jwtTokenProvider = jwtTokenProvider;
        this.memberMapper = memberMapper;
    }

    /**
     * 요청이 들어올 때 JWT 인증을 수행한다.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // 브라우저 쿠키에서 JWT를 가져온다.
        String token = extractTokenFromCookie(request);

        // 토큰이 존재하고 유효한 경우에만 인증을 진행한다.
        if (token != null && jwtTokenProvider.validateToken(token)) {

            try {
                // JWT에서 회원 고유 번호를 추출한다.
                Long memberNo = jwtTokenProvider.getMemberNo(token);

                // DB에서 현재 활성 상태인 회원 정보를 조회한다.
                Member member = memberMapper.findByMemberNo(memberNo);

                if (member != null) {

                    // DB에 저장된 회원 권한을 Spring Security 권한으로 변환한다.
                    // 예: USER -> ROLE_USER, ADMIN -> ROLE_ADMIN
                    String role = member.getRole();

                    if (role != null && !role.isBlank()) {

                        List<SimpleGrantedAuthority> authorities = List.of(
                                new SimpleGrantedAuthority("ROLE_" + role)
                        );

                        // 인증된 회원 객체와 권한 정보를 생성한다.
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        member,
                                        null,
                                        authorities
                                );

                        // 현재 요청의 IP 등 웹 인증 부가 정보를 설정한다.
                        authentication.setDetails(
                                new WebAuthenticationDetailsSource()
                                        .buildDetails(request)
                        );

                        // Spring Security에 인증 정보를 등록한다.
                        SecurityContextHolder.getContext()
                                .setAuthentication(authentication);
                    }
                }

            } catch (RuntimeException e) {

                // 토큰의 회원 번호가 잘못되었거나 인증 처리에 실패한 경우
                // 기존 인증 정보를 제거한다.
                SecurityContextHolder.clearContext();
            }
        }

        // 인증 성공 여부와 관계없이 다음 필터로 요청을 전달한다.
        // 실제 접근 허용 여부는 SecurityConfig에서 결정한다.
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청의 쿠키에서 JWT를 추출한다.
     *
     * 로그인 구현 단계에서 동일한 이름의 HttpOnly 쿠키를 발급할 예정이다.
     */
    private String extractTokenFromCookie(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {

            // 허걱슨닷컴의 JWT Access Token 쿠키 이름
            if ("accessToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
