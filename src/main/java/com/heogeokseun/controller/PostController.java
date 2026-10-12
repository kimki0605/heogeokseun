
package com.heogeokseun.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.heogeokseun.mapper.MemberMapper;
import com.heogeokseun.mapper.PostMapper;

@Controller
public class PostController {

    private final BoardMapper boardMapper;
    private final PostMapper postMapper;
    private final MemberMapper memberMapper;

    // 기존 Mapper를 그대로 사용
    public PostController(
            BoardMapper boardMapper,
            PostMapper postMapper,
            MemberMapper memberMapper) {

        this.boardMapper = boardMapper;
        this.postMapper = postMapper;
        this.memberMapper = memberMapper;
    }

    /**
     * JWT 인증 필터가 등록한 회원 정보를 가져온다.
     *
     * 로그인하지 않았거나 인증 정보가 없으면 null 반환.
     */
    private Member getLoginMember() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Member member) {
            return member;
        }

        return null;
    }

    /**
     * 게시글 작성자 본인인지 검사한다.
     */
    private boolean isPostOwner(Post post, Member member) {

        return post != null
                && member != null
                && post.getMemberNo() != null
                && post.getMemberNo().equals(member.getMemberNo());
    }

    /**
     * 게시글 작성 화면
     */
    @GetMapping("/boards/{boardNo}/write")
    public String writeForm(
            @PathVariable("boardNo") Long boardNo,
            Model model) {

        Member loginMember = getLoginMember();

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

    /**
     * 새 게시글 등록
     */
    @PostMapping("/boards/{boardNo}/posts")
    public String createPost(
            @PathVariable("boardNo") Long boardNo,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            RedirectAttributes redirectAttributes) {

        Member loginMember = getLoginMember();

        if (loginMember == null) {
            return "redirect:/login";
        }

        Board board = boardMapper.findByBoardNo(boardNo);

        if (board == null) {
            return "redirect:/boards";
        }

        title = title == null ? "" : title.trim();
        content = content == null ? "" : content.trim();

        // 게시글 제목과 내용 검사
        if (title.isEmpty()
                || title.length() > 200
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

    /**
     * 게시글 상세보기
     */
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

        // 존재하지 않거나 다른 게시판에 속한 게시글 차단
        if (post == null
                || !boardNo.equals(post.getBoardNo())) {
            return "redirect:/boards/" + boardNo;
        }

        // 조회수 증가
        postMapper.increaseViewCount(postNo);

        // 증가한 조회수를 반영
        post = postMapper.findByPostNo(postNo);

        // 작성자 닉네임 조회
        Member author =
                memberMapper.findByMemberNo(post.getMemberNo());

        String authorNickname = "알 수 없는 사용자";

        if (author != null && author.getNickname() != null) {
            authorNickname = author.getNickname();
        }

        // 현재 로그인한 회원이 작성자인지 검사
        Member loginMember = getLoginMember();

        boolean isOwner = isPostOwner(post, loginMember);

        model.addAttribute("board", board);
        model.addAttribute("post", post);
        model.addAttribute("authorNickname", authorNickname);
        model.addAttribute("isOwner", isOwner);

        return "board/post-detail";
    }

    /**
     * 게시글 수정 화면
     *
     * 작성자 본인만 접근 가능
     */
    @GetMapping("/boards/{boardNo}/posts/{postNo}/edit")
    public String editForm(
            @PathVariable("boardNo") Long boardNo,
            @PathVariable("postNo") Long postNo,
            Model model,
            RedirectAttributes redirectAttributes) {

        Member loginMember = getLoginMember();

        if (loginMember == null) {
            return "redirect:/login";
        }

        Board board = boardMapper.findByBoardNo(boardNo);

        if (board == null) {
            return "redirect:/boards";
        }

        Post post = postMapper.findByPostNo(postNo);

        if (post == null
                || !boardNo.equals(post.getBoardNo())) {
            return "redirect:/boards/" + boardNo;
        }

        // 작성자가 아니면 수정 화면 접근 차단
        if (!isPostOwner(post, loginMember)) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "본인이 작성한 게시글만 수정할 수 있습니다."
            );

            return "redirect:/boards/" + boardNo
                    + "/posts/" + postNo;
        }

        model.addAttribute("board", board);
        model.addAttribute("post", post);

        return "board/post-edit";
    }

    /**
     * 게시글 수정 처리
     *
     * 화면 접근 검사와 별도로 서버에서 작성자 재검증.
     * DB UPDATE 조건에서도 MEMBER_NO를 검사한다.
     */
    @PostMapping("/boards/{boardNo}/posts/{postNo}/edit")
    public String updatePost(
            @PathVariable("boardNo") Long boardNo,
            @PathVariable("postNo") Long postNo,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            RedirectAttributes redirectAttributes) {

        Member loginMember = getLoginMember();

        if (loginMember == null) {
            return "redirect:/login";
        }

        Post existingPost = postMapper.findByPostNo(postNo);

        if (existingPost == null
                || !boardNo.equals(existingPost.getBoardNo())) {
            return "redirect:/boards/" + boardNo;
        }

        // 작성자 본인 여부 확인
        if (!isPostOwner(existingPost, loginMember)) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "게시글 수정 권한이 없습니다."
            );

            return "redirect:/boards/" + boardNo
                    + "/posts/" + postNo;
        }

        title = title == null ? "" : title.trim();
        content = content == null ? "" : content.trim();

        // 수정할 내용의 유효성 검사
        if (title.isEmpty()
                || title.length() > 200
                || content.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "제목은 1~200자, 내용은 1자 이상 입력해 주세요."
            );

            return "redirect:/boards/" + boardNo
                    + "/posts/" + postNo + "/edit";
        }

        Post updatePost = new Post();

        updatePost.setPostNo(postNo);
        updatePost.setMemberNo(loginMember.getMemberNo());
        updatePost.setTitle(title);
        updatePost.setContent(content);

        // SQL에서도 작성자 번호가 일치해야 수정됨
        int result = postMapper.updatePost(updatePost);

        if (result != 1) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "게시글 수정에 실패했습니다."
            );

            return "redirect:/boards/" + boardNo
                    + "/posts/" + postNo + "/edit";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "게시글이 수정되었습니다."
        );

        return "redirect:/boards/" + boardNo
                + "/posts/" + postNo;
    }

    /**
     * 게시글 삭제 처리
     *
     * 실제 데이터를 삭제하지 않고
     * POST_STATUS를 DELETED로 변경한다.
     */
    @PostMapping("/boards/{boardNo}/posts/{postNo}/delete")
    public String deletePost(
            @PathVariable("boardNo") Long boardNo,
            @PathVariable("postNo") Long postNo,
            RedirectAttributes redirectAttributes) {

        Member loginMember = getLoginMember();

        if (loginMember == null) {
            return "redirect:/login";
        }

        Post post = postMapper.findByPostNo(postNo);

        if (post == null
                || !boardNo.equals(post.getBoardNo())) {
            return "redirect:/boards/" + boardNo;
        }

        // 작성자 본인인지 확인
        if (!isPostOwner(post, loginMember)) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "본인이 작성한 게시글만 삭제할 수 있습니다."
            );

            return "redirect:/boards/" + boardNo
                    + "/posts/" + postNo;
        }

        // SQL에서도 작성자 번호를 다시 검사
        int result = postMapper.deletePost(
                postNo,
                loginMember.getMemberNo()
        );

        if (result != 1) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "게시글 삭제에 실패했습니다."
            );

            return "redirect:/boards/" + boardNo
                    + "/posts/" + postNo;
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "게시글이 삭제되었습니다."
        );

        return "redirect:/boards/" + boardNo;
    }
}
