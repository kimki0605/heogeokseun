
package com.heogeokseun.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.heogeokseun.domain.Board;
import com.heogeokseun.domain.Post;
import com.heogeokseun.mapper.BoardMapper;
import com.heogeokseun.mapper.PostMapper;

@Controller
public class BoardController {

    private final BoardMapper boardMapper;
    private final PostMapper postMapper;

    // 한 페이지에 표시할 게시글 수
    private static final int PAGE_SIZE = 10;

    // 한 번에 표시할 페이지 번호 개수
    private static final int PAGE_BLOCK_SIZE = 5;

    public BoardController(
            BoardMapper boardMapper,
            PostMapper postMapper) {

        this.boardMapper = boardMapper;
        this.postMapper = postMapper;
    }

    // ========================================
    // 전체 게시판 목록 조회
    // ========================================
    @GetMapping("/boards")
    public String boardList(Model model) {

        // 활성 상태인 게시판 목록 조회
        List<Board> boards = boardMapper.findAllActiveBoards();

        model.addAttribute("boards", boards);

        return "board/list";
    }

    // ========================================
    // 게시판 상세 / 게시글 검색 / 페이징
    // ========================================
    @GetMapping("/boards/{boardNo}")
    public String boardDetail(
            @PathVariable("boardNo") Long boardNo,

            // 검색 유형
            @RequestParam(
                    name = "searchType",
                    defaultValue = "title"
            ) String searchType,

            // 검색어
            @RequestParam(
                    name = "keyword",
                    defaultValue = ""
            ) String keyword,

            // 현재 페이지 번호
            @RequestParam(
                    name = "page",
                    defaultValue = "1"
            ) int page,

            Model model) {

        // ========================================
        // 1. 게시판 정보 조회
        // ========================================
        Board board = boardMapper.findByBoardNo(boardNo);

        // 존재하지 않는 게시판이면 목록으로 이동
        if (board == null) {
            return "redirect:/boards";
        }

        // ========================================
        // 2. 검색 조건 정리
        // ========================================

        // 검색어 앞뒤 공백 제거
        keyword = keyword.trim();

        // 허용된 검색 유형만 사용
        if (!"title".equals(searchType)
                && !"all".equals(searchType)) {

            searchType = "title";
        }

        // 검색 여부
        boolean isSearch = !keyword.isEmpty();

        // ========================================
        // 3. 전체 게시글 개수 조회
        // ========================================
        long totalPosts = postMapper.countPosts(
                boardNo,
                searchType,
                keyword
        );

        // ========================================
        // 4. 전체 페이지 수 계산
        // ========================================

        // 게시글 10개당 1페이지
        // 예: 35개 -> 4페이지
        int totalPages = (int) Math.ceil(
                (double) totalPosts / PAGE_SIZE
        );

        // 게시글이 없는 경우에도 기본 페이지는 1
        if (totalPages < 1) {
            totalPages = 1;
        }

        // ========================================
        // 5. 현재 페이지 번호 보정
        // ========================================

        // 0 이하 페이지는 1페이지로 변경
        if (page < 1) {
            page = 1;
        }

        // 마지막 페이지를 초과하면 마지막 페이지로 변경
        if (page > totalPages) {
            page = totalPages;
        }

        // ========================================
        // 6. OFFSET 계산
        // ========================================

        // 1페이지 -> 0
        // 2페이지 -> 10
        // 3페이지 -> 20
        int offset = (page - 1) * PAGE_SIZE;

        // ========================================
        // 7. 현재 페이지의 게시글 조회
        // ========================================
        List<Post> posts = postMapper.findPostsByPage(
                boardNo,
                searchType,
                keyword,
                offset,
                PAGE_SIZE
        );

        // ========================================
        // 8. 페이지 번호 블록 계산
        // ========================================

        // 1~5페이지 -> 시작 1
        // 6~10페이지 -> 시작 6
        int startPage = ((page - 1) / PAGE_BLOCK_SIZE)
                * PAGE_BLOCK_SIZE + 1;

        // 마지막 페이지 번호 계산
        int endPage = Math.min(
                startPage + PAGE_BLOCK_SIZE - 1,
                totalPages
        );

        // ========================================
        // 9. 화면에 데이터 전달
        // ========================================

        // 게시판 정보
        model.addAttribute("board", board);

        // 현재 페이지의 게시글 목록
        model.addAttribute("posts", posts);

        // 검색 정보
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("isSearch", isSearch);

        // 페이징 정보
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalPosts", totalPosts);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        // 이전 / 다음 페이지 존재 여부
        model.addAttribute("hasPrevious", page > 1);
        model.addAttribute("hasNext", page < totalPages);

        return "board/detail";
    }
}
