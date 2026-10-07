package com.heogeokseun.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.heogeokseun.service.EmailService;

import jakarta.servlet.http.HttpSession;

@RestController
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    // 인증번호 발송
    @PostMapping("/email/send")
    public Map<String, Object> sendVerificationCode(
            @RequestParam String email,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {

            String verificationCode =
                    emailService.sendVerificationCode(email);

            // 인증번호 저장
            session.setAttribute(
                    "emailVerificationCode",
                    verificationCode
            );

            // 인증 대상 이메일 저장
            session.setAttribute(
                    "emailVerificationTarget",
                    email
            );

            // 아직 인증되지 않은 상태
            session.setAttribute(
                    "emailVerified",
                    false
            );

            response.put("success", true);
            response.put(
                    "message",
                    "인증번호를 이메일로 발송했습니다."
            );

        } catch (Exception e) {

            System.err.println(
                    "===== 이메일 발송 오류 ====="
            );

            e.printStackTrace();

            response.put("success", false);
            response.put(
                    "message",
                    "인증번호 발송에 실패했습니다."
            );
        }

        return response;
    }

    // 인증번호 확인
    @PostMapping("/email/verify")
    public Map<String, Object> verifyCode(
            @RequestParam String email,
            @RequestParam String code,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        String savedCode =
                (String) session.getAttribute(
                        "emailVerificationCode"
                );

        String targetEmail =
                (String) session.getAttribute(
                        "emailVerificationTarget"
                );

        if (savedCode != null
                && targetEmail != null
                && targetEmail.equals(email)
                && savedCode.equals(code)) {

            // 이메일 인증 성공
            session.setAttribute(
                    "emailVerified",
                    true
            );

            response.put("success", true);
            response.put(
                    "message",
                    "이메일 인증이 완료되었습니다."
            );

        } else {

            // 이메일 인증 실패
            session.setAttribute(
                    "emailVerified",
                    false
            );

            response.put("success", false);
            response.put(
                    "message",
                    "인증번호가 올바르지 않습니다."
            );
        }

        return response;
    }
}