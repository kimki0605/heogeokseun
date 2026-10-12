
package com.heogeokseun.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.heogeokseun.domain.Member;

@Mapper
public interface MemberMapper {

    // 회원가입: 회원 정보를 DB에 저장
    int insertMember(Member member);

    // 회원가입 시 아이디 중복 검사
    int countByMemberId(String memberId);

    // 회원가입 시 닉네임 중복 검사
    int countByNickname(String nickname);

    // 회원가입 시 이메일 중복 검사
    int countByEmail(String email);

    // 로그인 시 아이디로 회원 조회
    Member findByMemberId(String memberId);

    // 마이페이지: 회원 고유번호로 최신 정보 조회
    Member findByMemberNo(Long memberNo);

    // 닉네임 변경
    int updateNickname(
            @Param("memberNo") Long memberNo,
            @Param("nickname") String nickname
    );

    // 프로필 이미지 경로 변경
    // 변경 성공 시 1, 해당 회원이 없거나 비활성 상태면 0 반환
    int updateProfileImage(
            @Param("memberNo") Long memberNo,
            @Param("profileImage") String profileImage
    );
}
