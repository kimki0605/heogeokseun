package com.heogeokseun.service;

import java.security.SecureRandom;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final SecureRandom random = new SecureRandom();

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // 6자리 인증번호 생성
    public String createVerificationCode() {
        int number = random.nextInt(900000) + 100000;
        return String.valueOf(number);
    }

    // 인증번호 이메일 발송
    public String sendVerificationCode(String email) {

        String verificationCode = createVerificationCode();

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("[허걱슨닷컴] 이메일 인증번호입니다.");
        message.setText(
            "허걱슨닷컴 회원가입 이메일 인증번호입니다.\n\n"
            + "인증번호 : " + verificationCode + "\n\n"
            + "본인이 요청하지 않았다면 이 메일을 무시해주세요."
        );

        mailSender.send(message);

        return verificationCode; //다음 단계에서 서버가 잠시 보관했다가 사용자가 입력한 인증번호와 비교하기 위해서 반환
    }
}