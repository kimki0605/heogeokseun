package com.heogeokseun.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.heogeokseun.domain.Member;
import com.heogeokseun.mapper.MemberMapper;

@Service
public class MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    public MemberService(
            MemberMapper memberMapper,
            PasswordEncoder passwordEncoder) {

        this.memberMapper = memberMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // 회원가입
    public int join(Member member) {

        String encodedPassword =
                passwordEncoder.encode(member.getMemberPw());

        member.setMemberPw(encodedPassword);

        return memberMapper.insertMember(member);
    }

    // 아이디 중복 확인
    public boolean isMemberIdDuplicate(String memberId) {
        return memberMapper.countByMemberId(memberId) > 0;
    }

    // 닉네임 중복 확인
    public boolean isNicknameDuplicate(String nickname) {
        return memberMapper.countByNickname(nickname) > 0;
    }

    // 이메일 중복 확인
    public boolean isEmailDuplicate(String email) {
        return memberMapper.countByEmail(email) > 0;
    }

    // 로그인
    public Member login(
            String memberId,
            String memberPw) {

        Member member =
                memberMapper.findByMemberId(memberId);

        // 존재하지 않는 아이디
        if (member == null) {
            return null;
        }

        // 탈퇴 또는 정지된 회원 로그인 차단
        if (!"ACTIVE".equals(member.getStatus())) {
            return null;
        }

        // BCrypt 비밀번호 비교
        if (!passwordEncoder.matches(
                memberPw,
                member.getMemberPw())) {

            return null;
        }

        return member;
    }
}