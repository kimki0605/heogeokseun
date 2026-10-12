
package com.heogeokseun.service;

import java.time.LocalDateTime;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.heogeokseun.domain.Member;
import com.heogeokseun.mapper.MemberMapper;

@Service
public class MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    // 생성자를 통한 의존성 주입
    public MemberService(MemberMapper memberMapper,
                         PasswordEncoder passwordEncoder) {
        this.memberMapper = memberMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // 회원가입
    public void join(Member member) {

        // 비밀번호를 BCrypt로 암호화한 후 저장
        String encodedPassword =
                passwordEncoder.encode(member.getMemberPw());

        member.setMemberPw(encodedPassword);

        memberMapper.insertMember(member);
    }

    // 아이디 중복 검사
    public boolean isMemberIdDuplicate(String memberId) {
        return memberMapper.countByMemberId(memberId) > 0;
    }

    // 닉네임 중복 검사
    public boolean isNicknameDuplicate(String nickname) {
        return memberMapper.countByNickname(nickname) > 0;
    }

    // 이메일 중복 검사
    public boolean isEmailDuplicate(String email) {
        return memberMapper.countByEmail(email) > 0;
    }

    // 로그인
    public Member login(String memberId, String memberPw) {

        // 아이디로 회원 조회
        Member member = memberMapper.findByMemberId(memberId);

        // 회원이 없거나 비활성 상태라면 로그인 실패
        if (member == null || !"ACTIVE".equals(member.getStatus())) {
            return null;
        }

        // 입력한 비밀번호와 암호화된 비밀번호 비교
        if (!passwordEncoder.matches(memberPw, member.getMemberPw())) {
            return null;
        }

        return member;
    }

    // 닉네임 변경
    public String changeNickname(Long memberNo, String newNickname) {

        // 입력값 검사
        if (memberNo == null || newNickname == null) {
            return "잘못된 요청입니다.";
        }

        newNickname = newNickname.trim();

        // 닉네임 길이 검사
        if (newNickname.isEmpty() || newNickname.length() > 30) {
            return "닉네임은 1자 이상 30자 이하로 입력해주세요.";
        }

        // DB에서 최신 회원 정보 조회
        Member member = memberMapper.findByMemberNo(memberNo);

        if (member == null || !"ACTIVE".equals(member.getStatus())) {
            return "회원 정보를 찾을 수 없습니다.";
        }

        // 기존 닉네임과 동일한지 검사
        if (newNickname.equals(member.getNickname())) {
            return "현재 사용 중인 닉네임과 동일합니다.";
        }

        // 닉네임 변경 후 30일이 지났는지 검사
        LocalDateTime lastChanged = member.getNicknameChangedAt();

        if (lastChanged != null &&
                lastChanged.plusDays(30).isAfter(LocalDateTime.now())) {
            return "닉네임은 마지막 변경일로부터 30일 후 다시 변경할 수 있습니다.";
        }

        // 다른 회원이 사용 중인 닉네임인지 검사
        if (isNicknameDuplicate(newNickname)) {
            return "이미 사용 중인 닉네임입니다.";
        }

        try {
            // DB에서도 30일 제한을 검사하며 변경
            int updatedRows =
                    memberMapper.updateNickname(memberNo, newNickname);

            if (updatedRows == 0) {
                return "닉네임을 변경할 수 없습니다. 변경 가능 기간을 확인해주세요.";
            }

            return "SUCCESS";

        } catch (DuplicateKeyException e) {
            // 동시 요청으로 닉네임이 중복된 경우
            return "이미 사용 중인 닉네임입니다.";
        }
    }
}
