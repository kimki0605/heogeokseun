
package com.heogeokseun.domain;

import java.time.LocalDateTime;

public class Post {

    // 게시글 고유 번호
    private Long postNo;

    // 게시글이 속한 게시판 번호
    private Long boardNo;

    // 게시글 작성자 회원 번호
    private Long memberNo;

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

    public Long getPostNo() {
        return postNo;
    }

    public void setPostNo(Long postNo) {
        this.postNo = postNo;
    }

    public Long getBoardNo() {
        return boardNo;
    }

    public void setBoardNo(Long boardNo) {
        this.boardNo = boardNo;
    }

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public String getPostStatus() {
        return postStatus;
    }

    public void setPostStatus(String postStatus) {
        this.postStatus = postStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
