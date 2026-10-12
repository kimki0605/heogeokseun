
package com.heogeokseun.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.heogeokseun.domain.Post;

@Mapper
public interface PostMapper {

    // 특정 게시판의 게시글 목록 조회
    List<Post> findByBoardNo(@Param("boardNo") Long boardNo);

    // 게시글 번호로 상세 조회
    Post findByPostNo(@Param("postNo") Long postNo);

    // 게시글 등록
    int insertPost(Post post);

    // 게시글 조회수 증가
    int increaseViewCount(@Param("postNo") Long postNo);

    // 게시글 수정
    int updatePost(Post post);

    // 게시글 삭제 (실제 삭제 대신 상태 변경)
    int deletePost(
            @Param("postNo") Long postNo,
            @Param("memberNo") Long memberNo
    );
}
