
package com.heogeokseun.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.heogeokseun.domain.Board;
import com.heogeokseun.domain.Member;
import com.heogeokseun.domain.Post;
import com.heogeokseun.mapper.BoardMapper;
import com.heogeokseun.mapper.PostMapper;

@Controller
public class PostController {

    private final BoardMapper boardMapper;
    private final PostMapper postMapper;

    public PostController(
            BoardMapper boardMapper,
            PostMapper postMapper) {

        this.boardMapper = boardMapper;
        this.postMapper = postMapper;
    }

    @GetMapping("/boards/{boardNo}/write")
    public String writeForm(
            @PathVariable("boardNo") Long boardNo,
            HttpSession session,
            Model model) {

        Member loginMember =
                (Member) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/login";
        }

        Board board = boardMapper.findByBoardNo(boardNo);

        if (board == null) {
            return "redirect:/boards";
        }

        model.addAttribute("board", board);

        return "board/write";
    }

    @PostMapping("/boards/{boardNo}/posts")
    public String createPost(
            @PathVariable("boardNo") Long boardNo,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Member loginMember =
                (Member) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/login";
        }

        Board board = boardMapper.findByBoardNo(boardNo);

        if (board == null) {
            return "redirect:/boards";
        }

        title = title.trim();
        content = content.trim();

        if (title.isEmpty() || title.length() > 200
                || content.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "제목은 1~200자, 내용은 1자 이상 입력해 주세요."
            );

            return "redirect:/boards/" + boardNo + "/write";
        }

        Post post = new Post();

        post.setBoardNo(boardNo);
        post.setMemberNo(loginMember.getMemberNo());
        post.setTitle(title);
        post.setContent(content);

        int result = postMapper.insertPost(post);

        if (result != 1) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "게시글 등록에 실패했습니다."
            );

            return "redirect:/boards/" + boardNo + "/write";
        }

        return "redirect:/boards/" + boardNo;
    }

    @GetMapping("/boards/{boardNo}/posts/{postNo}")
    public String postDetail(
            @PathVariable("boardNo") Long boardNo,
            @PathVariable("postNo") Long postNo,
            Model model) {

        Board board = boardMapper.findByBoardNo(boardNo);

        if (board == null) {
            return "redirect:/boards";
        }

        Post post = postMapper.findByPostNo(postNo);

        if (post == null || !boardNo.equals(post.getBoardNo())) {
            return "redirect:/boards/" + boardNo;
        }

        postMapper.increaseViewCount(postNo);

        post = postMapper.findByPostNo(postNo);

        model.addAttribute("board", board);
        model.addAttribute("post", post);

        return "board/post-detail";
    }
}
