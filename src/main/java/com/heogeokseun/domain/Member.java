package com.heogeokseun.domain;

import java.time.LocalDateTime;

public class Member {

    // 회원 고유번호 (PK)
    private Long memberNo;

    // 로그인 아이디
    private String memberId;

    // BCrypt로 암호화된 비밀번호
    private String memberPw;

    // 사용자 닉네임
    private String nickname;

    // 이메일 주소
    private String email;

    // 회원 권한 (USER / ADMIN)
    private String role;

    // 회원 상태 (ACTIVE 등)
    private String status;

    // 회원가입 날짜
    private LocalDateTime createdAt;

    // 프로필 사진 경로
    private String profileImage;

    // 닉네임 마지막 변경 날짜
    private LocalDateTime nicknameChangedAt;

    // 회원 고유번호
    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

    // 로그인 아이디
    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    // 비밀번호
    public String getMemberPw() {
        return memberPw;
    }

    public void setMemberPw(String memberPw) {
        this.memberPw = memberPw;
    }

    // 닉네임
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    // 이메일
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // 권한
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // 회원 상태
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // 가입 날짜
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // 프로필 사진
    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    // 닉네임 마지막 변경 날짜
    public LocalDateTime getNicknameChangedAt() {
        return nicknameChangedAt;
    }

    public void setNicknameChangedAt(LocalDateTime nicknameChangedAt) {
        this.nicknameChangedAt = nicknameChangedAt;
    }
}