package com.heogeokseun.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.heogeokseun.domain.Member;
import com.heogeokseun.service.MemberService;

import jakarta.servlet.http.HttpSession;

@Controller
public class MemberController {

    private final MemberService memberService;

    // MemberService 의존성 주입
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
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

        // 이메일 인증을 하지 않았거나
        // 인증받은 이메일과 회원가입 이메일이 다르면 가입 차단
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

        // 모든 검사를 통과하면 회원가입 처리
        // MemberService에서 비밀번호를 BCrypt로 암호화한 뒤 DB에 저장
        memberService.join(member);

        // 사용이 끝난 이메일 인증정보 제거
        // 같은 인증정보를 다시 사용하는 것을 방지
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

        // templates/member/login.html 호출
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
            Model model) {

        // 입력한 아이디와 비밀번호로 회원 조회
        // MemberService 내부에서 BCrypt 비밀번호 비교까지 수행
        Member loginMember =
                memberService.login(memberId, memberPw);

        // 로그인 실패
        if (loginMember == null) {

            model.addAttribute(
                    "errorMessage",
                    "아이디 또는 비밀번호가 올바르지 않습니다."
            );

            return "member/login";
        }

        // 로그인 성공
        // 로그인한 회원 정보를 세션에 저장
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
    public String logout(HttpSession session) {

        // 현재 세션에 저장된 모든 정보 삭제
        // loginMember도 함께 삭제되므로 로그아웃 처리됨
        session.invalidate();

        // 로그아웃 후 메인 페이지로 이동
        return "redirect:/";
    }
}