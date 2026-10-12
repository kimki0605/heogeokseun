
package com.heogeokseun.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.heogeokseun.domain.Post;

@Mapper
public interface PostMapper {

    // ========================================
    // 기존 게시글 기능
    // ========================================

    // 특정 게시판의 전체 게시글 조회
    List<Post> findByBoardNo(@Param("boardNo") Long boardNo);

    // 게시글 번호로 상세 조회
    Post findByPostNo(@Param("postNo") Long postNo);

    // 게시글 등록
    int insertPost(Post post);

    // 게시글 조회수 증가
    int increaseViewCount(@Param("postNo") Long postNo);

    // 게시글 수정
    int updatePost(Post post);

    // 게시글 삭제 (소프트 삭제)
    int deletePost(
            @Param("postNo") Long postNo,
            @Param("memberNo") Long memberNo
    );

    // 게시글 검색
    List<Post> searchPosts(
            @Param("boardNo") Long boardNo,
            @Param("searchType") String searchType,
            @Param("keyword") String keyword
    );

    // ========================================
    // 페이징 기능
    // ========================================

    /**
     * 게시글 목록을 페이지 단위로 조회한다.
     *
     * boardNo    : 게시판 번호
     * searchType : title(제목), all(제목+내용)
     * keyword    : 검색어
     * offset     : 건너뛸 게시글 개수
     * pageSize   : 한 페이지에 표시할 게시글 개수
     *
     * 검색어가 없으면 전체 게시글을 조회하고,
     * 검색어가 있으면 검색 결과를 조회한다.
     */
    List<Post> findPostsByPage(
            @Param("boardNo") Long boardNo,
            @Param("searchType") String searchType,
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    /**
     * 게시글 총 개수를 조회한다.
     *
     * 전체 게시글 수 또는 검색 결과 수를 계산하여
     * 전체 페이지 개수를 구할 때 사용한다.
     */
    long countPosts(
            @Param("boardNo") Long boardNo,
            @Param("searchType") String searchType,
            @Param("keyword") String keyword
    );

    // ========================================
    // 메인페이지 최신 게시글 조회 기능 (신규)
    // ========================================

    /**
     * 전체 게시판에서 최신 게시글을 조회한다.
     *
     * - 삭제되지 않은 게시글만 조회
     * - 최신 작성일 순으로 정렬
     * - 최대 limit개 조회
     * - 메인페이지 최신 게시글 영역에서 사용
     */
    List<Post> findLatestPosts(
            @Param("limit") int limit
    );

    // ========================================
    // 메인페이지 인기 게시글 조회 기능 (신규)
    // ========================================
    /**
     * 전체 게시판에서 조회수가 높은 게시글을 조회한다.
     *
     * - 삭제되지 않은 게시글만 조회
     * - 조회수 높은 순으로 정렬
     * - 조회수가 같으면 최신 작성일순 정렬
     * - 최대 limit개 조회
     *
     * @param limit 조회할 게시글 개수
     * @return 인기 게시글 목록
     */
    List<Post> findPopularPosts(@Param("limit") int limit);

}
