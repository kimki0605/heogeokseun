
package com.heogeokseun.domain;

import java.time.LocalDateTime;

public class Post {

    // 게시글 고유 번호
    private Long postNo;

    // 게시글이 속한 게시판 번호
    private Long boardNo;

    // 게시글 작성자 회원 번호
    private Long memberNo;

    // 게시글 작성자 닉네임 (MEMBER 테이블 JOIN 결과)
    // POST 테이블에 직접 저장되는 컬럼은 아님
    private String authorNickname;

    // 게시글 제목
    private String title;

    // 게시글 내용
    private String content;

    // 조회수
    private Long viewCount;

    // 게시글 상태 (ACTIVE, DELETED)
    private String postStatus;

    // 게시글 작성일
    private LocalDateTime createdAt;

    // 게시글 수정일
    private LocalDateTime updatedAt;

    // 기본 생성자
    public Post() {
    }

    // 게시글 고유 번호
    public Long getPostNo() {
        return postNo;
    }

    public void setPostNo(Long postNo) {
        this.postNo = postNo;
    }

    // 게시판 번호
    public Long getBoardNo() {
        return boardNo;
    }

    public void setBoardNo(Long boardNo) {
        this.boardNo = boardNo;
    }

    // 작성자 회원 번호
    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

    // 작성자 닉네임
    public String getAuthorNickname() {
        return authorNickname;
    }

    public void setAuthorNickname(String authorNickname) {
        this.authorNickname = authorNickname;
    }

    // 게시글 제목
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // 게시글 내용
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // 조회수
    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    // 게시글 상태
    public String getPostStatus() {
        return postStatus;
    }

    public void setPostStatus(String postStatus) {
        this.postStatus = postStatus;
    }

    // 게시글 작성일
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // 게시글 수정일
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
