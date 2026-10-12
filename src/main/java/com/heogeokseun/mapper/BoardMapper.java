
package com.heogeokseun.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.heogeokseun.domain.Board;

@Mapper
public interface BoardMapper {

    // 활성화된 게시판 전체 조회
    List<Board> findAllActiveBoards();

    // 게시판 번호로 게시판 상세 조회
    Board findByBoardNo(@Param("boardNo") Long boardNo);

}
