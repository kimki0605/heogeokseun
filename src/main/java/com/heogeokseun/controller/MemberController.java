
package com.heogeokseun.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.heogeokseun.domain.Member;
import com.heogeokseun.security.JwtTokenProvider;
import com.heogeokseun.service.MemberService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원 서비스와 JWT 발급 객체를 생성자 주입받는다.
    public MemberController(
            MemberService memberService,
            JwtTokenProvider jwtTokenProvider) {

        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // =========================
    // 회원가입 페이지
    // =========================
    @GetMapping("/join")
    public String joinForm() {
        return "member/join";
    }

    // =========================
    // 회원가입 처리
    // =========================
    @PostMapping("/join")
    public String join(
            Member member,
            Model model,
            HttpSession session) {

        // 사용자가 입력한 비밀번호 가져오기
        String password = member.getMemberPw();

        // 비밀번호 조건 검사
        // 6자리 이상 + 특수문자 최소 1개
        if (password == null
                || password.length() < 6
                || !password.matches(".*[^a-zA-Z0-9].*")) {

            model.addAttribute(
                    "errorMessage",
                    "비밀번호는 6자리 이상이며 특수문자를 최소 1개 포함해야 합니다."
            );

            return "member/join";
        }

        // 이메일 인증 성공 여부 확인
        Boolean emailVerified =
                (Boolean) session.getAttribute("emailVerified");

        // 실제로 인증받은 이메일 주소 가져오기
        String verifiedEmail =
                (String) session.getAttribute("emailVerificationTarget");

        // 인증받은 이메일과 회원가입 이메일이 일치해야 한다.
        if (!Boolean.TRUE.equals(emailVerified)
                || verifiedEmail == null
                || !verifiedEmail.equals(member.getEmail())) {

            model.addAttribute(
                    "errorMessage",
                    "이메일 인증을 완료해주세요."
            );

            return "member/join";
        }

        // 아이디 중복 검사
        if (memberService.isMemberIdDuplicate(member.getMemberId())) {

            model.addAttribute(
                    "errorMessage",
                    "이미 사용 중인 아이디입니다."
            );

            return "member/join";
        }

        // 닉네임 중복 검사
        if (memberService.isNicknameDuplicate(member.getNickname())) {

            model.addAttribute(
                    "errorMessage",
                    "이미 사용 중인 닉네임입니다."
            );

            return "member/join";
        }

        // 이메일 중복 검사
        if (memberService.isEmailDuplicate(member.getEmail())) {

            model.addAttribute(
                    "errorMessage",
                    "이미 사용 중인 이메일입니다."
            );

            return "member/join";
        }

        // 비밀번호를 BCrypt로 암호화한 뒤 회원 정보를 저장한다.
        memberService.join(member);

        // 사용이 끝난 이메일 인증정보 제거
        session.removeAttribute("emailVerificationCode");
        session.removeAttribute("emailVerificationTarget");
        session.removeAttribute("emailVerified");

        // 회원가입 성공 후 메인 페이지로 이동
        return "redirect:/";
    }

    // =========================
    // 로그인 페이지
    // =========================
    @GetMapping("/login")
    public String loginForm() {

        return "member/login";
    }

    // =========================
    // 로그인 처리
    // =========================
    @PostMapping("/login")
    public String login(
            @RequestParam String memberId,
            @RequestParam String memberPw,
            HttpSession session,
            HttpServletResponse response,
            Model model) {

        // 아이디와 비밀번호를 검증한다.
        // MemberService에서 BCrypt 비밀번호 비교를 수행한다.
        Member loginMember =
                memberService.login(memberId, memberPw);

        // 로그인 실패 시 기존 오류 메시지를 표시한다.
        if (loginMember == null) {

            model.addAttribute(
                    "errorMessage",
                    "아이디 또는 비밀번호가 올바르지 않습니다."
            );

            return "member/login";
        }

        // =========================
        // JWT Access Token 발급
        // =========================

        // 회원 고유번호를 JWT의 subject에 저장한다.
        String accessToken =
                jwtTokenProvider.createAccessToken(
                        loginMember.getMemberNo()
                );

        // =========================
        // JWT 쿠키 생성
        // =========================

        // HttpOnly: JavaScript에서 토큰을 직접 읽지 못하게 한다.
        // SameSite=Lax: 일반적인 외부 사이트의 교차 출처 요청에서
        // 쿠키 전송을 제한한다.
        // Secure=false: 현재 localhost HTTP 개발 환경용 설정이다.
        // 운영 HTTPS 환경에서는 반드시 Secure=true로 변경해야 한다.
        ResponseCookie accessTokenCookie =
                ResponseCookie.from("accessToken", accessToken)
                        .httpOnly(true)
                        .secure(false)
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(30 * 60)
                        .build();

        // 브라우저에 JWT 쿠키를 전달한다.
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                accessTokenCookie.toString()
        );

        // =========================
        // 기존 세션 로그인 임시 유지
        // =========================

        // 기존 마이페이지 및 게시판 기능과의 호환성을 위해
        // JWT 전환이 끝날 때까지 세션 로그인도 유지한다.
        session.setAttribute(
                "loginMember",
                loginMember
        );

        // 로그인 성공 후 메인 페이지로 이동
        return "redirect:/";
    }

    // =========================
    // 로그아웃 처리
    // =========================
    @GetMapping("/logout")
    public String logout(
            HttpSession session,
            HttpServletResponse response) {

        // =========================
        // JWT 쿠키 삭제
        // =========================

        // 로그인 시 발급한 쿠키와 동일한 이름 및 경로를 사용하고,
        // Max-Age를 0으로 설정해 브라우저에서 삭제한다.
        ResponseCookie deleteAccessTokenCookie =
                ResponseCookie.from("accessToken", "")
                        .httpOnly(true)
                        .secure(false)
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(0)
                        .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                deleteAccessTokenCookie.toString()
        );

        // 기존 세션 로그인 정보도 함께 삭제한다.
        session.invalidate();

        // 로그아웃 후 메인 페이지로 이동
        return "redirect:/";
    }
}
