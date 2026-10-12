
package com.heogeokseun.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.heogeokseun.domain.Post;
import com.heogeokseun.mapper.PostMapper;

@Controller
public class HomeController {

    private final PostMapper postMapper;

    // ========================================
    // 생성자를 통한 PostMapper 의존성 주입
    // ========================================
    public HomeController(PostMapper postMapper) {
        this.postMapper = postMapper;
    }

    // ========================================
    // 메인페이지 조회
    // ========================================
    @GetMapping("/")
    public String home(Model model) {

        // ========================================
        // 1. 최신 게시글 5개 조회 (기존 기능)
        // ========================================
        List<Post> latestPosts =
                postMapper.findLatestPosts(5);

        model.addAttribute("latestPosts", latestPosts);

        // ========================================
        // 2. 인기 게시글 5개 조회 (신규 기능)
        // ========================================
        List<Post> popularPosts =
                postMapper.findPopularPosts(5);

        model.addAttribute("popularPosts", popularPosts);

        // ========================================
        // 3. 메인페이지 반환
        // ========================================
        return "index";
    }
}
