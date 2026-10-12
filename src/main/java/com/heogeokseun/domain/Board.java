
package com.heogeokseun.domain;

import java.time.LocalDateTime;

public class Board {

    // 게시판 고유 번호
    private Long boardNo;

    // 게시판 이름
    private String boardName;

    // 게시판 설명
    private String boardDescription;

    // 게시판 상태 (ACTIVE, INACTIVE)
    private String boardStatus;

    // 게시판 생성일
    private LocalDateTime createdAt;

    // 기본 생성자
    public Board() {
    }

    // 게시판 번호 조회
    public Long getBoardNo() {
        return boardNo;
    }

    public void setBoardNo(Long boardNo) {
        this.boardNo = boardNo;
    }

    // 게시판 이름 조회
    public String getBoardName() {
        return boardName;
    }

    public void setBoardName(String boardName) {
        this.boardName = boardName;
    }

    // 게시판 설명 조회
    public String getBoardDescription() {
        return boardDescription;
    }

    public void setBoardDescription(String boardDescription) {
        this.boardDescription = boardDescription;
    }

    // 게시판 상태 조회
    public String getBoardStatus() {
        return boardStatus;
    }

    public void setBoardStatus(String boardStatus) {
        this.boardStatus = boardStatus;
    }

    // 게시판 생성일 조회
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
