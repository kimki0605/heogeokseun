package com.heogeokseun.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.heogeokseun.domain.Member;

@Mapper
public interface MemberMapper {

    int insertMember(Member member);

    int countByMemberId(String memberId);

    int countByNickname(String nickname);

    int countByEmail(String email);

    Member findByMemberId(String memberId);
}