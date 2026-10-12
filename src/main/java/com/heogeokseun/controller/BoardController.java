
package com.heogeokseun.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.heogeokseun.domain.Board;
import com.heogeokseun.domain.Post;
import com.heogeokseun.mapper.BoardMapper;
import com.heogeokseun.mapper.PostMapper;

@Controller
public class BoardController {

    private final BoardMapper boardMapper;
    private final PostMapper postMapper;

    public BoardController(
            BoardMapper boardMapper,
            PostMapper postMapper) {

        this.boardMapper = boardMapper;
        this.postMapper = postMapper;
    }

    @GetMapping("/boards")
    public String boardList(Model model) {

        List<Board> boards = boardMapper.findAllActiveBoards();

        model.addAttribute("boards", boards);

        return "board/list";
    }

    @GetMapping("/boards/{boardNo}")
    public String boardDetail(
            @PathVariable("boardNo") Long boardNo,
            Model model) {

        Board board = boardMapper.findByBoardNo(boardNo);

        if (board == null) {
            return "redirect:/boards";
        }

        List<Post> posts = postMapper.findByBoardNo(boardNo);

        model.addAttribute("board", board);
        model.addAttribute("posts", posts);

        return "board/detail";
    }
}
