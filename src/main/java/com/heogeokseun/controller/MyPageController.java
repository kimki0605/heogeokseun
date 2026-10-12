
package com.heogeokseun.controller;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.heogeokseun.domain.Member;
import com.heogeokseun.mapper.MemberMapper;
import com.heogeokseun.service.MemberService;
import com.heogeokseun.service.ProfileImageService;

import jakarta.servlet.http.HttpSession;

@Controller
public class MyPageController {

    // 회원 정보 조회
    private final MemberMapper memberMapper;

    // 닉네임 변경
    private final MemberService memberService;

    // 프로필 이미지 저장 및 삭제
    private final ProfileImageService profileImageService;

    public MyPageController(
            MemberMapper memberMapper,
            MemberService memberService,
            ProfileImageService profileImageService) {

        this.memberMapper = memberMapper;
        this.memberService = memberService;
        this.profileImageService = profileImageService;
    }

    // ========================================
    // 마이페이지
    // ========================================
    @GetMapping("/mypage")
    public String myPage(HttpSession session, Model model) {

        Member loginMember =
                (Member) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/login";
        }

        Member member =
                memberMapper.findByMemberNo(loginMember.getMemberNo());

        if (member == null || !"ACTIVE".equals(member.getStatus())) {
            session.invalidate();
            return "redirect:/login";
        }

        model.addAttribute("member", member);

        // 비밀번호 해시를 제외하고 세션 갱신
        Member sessionMember = new Member();

        sessionMember.setMemberNo(member.getMemberNo());
        sessionMember.setMemberId(member.getMemberId());
        sessionMember.setNickname(member.getNickname());
        sessionMember.setEmail(member.getEmail());
        sessionMember.setRole(member.getRole());
        sessionMember.setStatus(member.getStatus());
        sessionMember.setProfileImage(member.getProfileImage());

        session.setAttribute("loginMember", sessionMember);

        return "member/mypage";
    }

    // ========================================
    // 닉네임 변경
    // ========================================
    @PostMapping("/mypage/nickname")
    public String changeNickname(
            @RequestParam("nickname") String nickname,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Member loginMember =
                (Member) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/login";
        }

        String result = memberService.changeNickname(
                loginMember.getMemberNo(),
                nickname
        );

        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "닉네임이 성공적으로 변경되었습니다."
            );
        } else {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    result
            );
        }

        return "redirect:/mypage";
    }

    // ========================================
    // 프로필 사진 변경
    // ========================================
    @PostMapping("/mypage/profile-image")
    public String changeProfileImage(
            @RequestParam("profileImage") MultipartFile profileImage,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Member loginMember =
                (Member) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/login";
        }

        // DB에서 현재 회원 정보 조회
        Member member =
                memberMapper.findByMemberNo(loginMember.getMemberNo());

        if (member == null || !"ACTIVE".equals(member.getStatus())) {
            session.invalidate();
            return "redirect:/login";
        }

        // 변경 전 프로필 사진 경로
        String oldImageUrl = member.getProfileImage();

        // 새로 저장한 이미지 경로
        String newImageUrl = null;

        try {

            // 1. 새 이미지 저장
            newImageUrl =
                    profileImageService.saveProfileImage(profileImage);

            // 2. DB 프로필 이미지 경로 변경
            int updatedRows = memberMapper.updateProfileImage(
                    member.getMemberNo(),
                    newImageUrl
            );

            // 3. DB 변경 실패 시 새 이미지 삭제
            if (updatedRows == 0) {

                profileImageService.deleteProfileImage(newImageUrl);

                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "프로필 사진을 변경할 수 없습니다."
                );

                return "redirect:/mypage";
            }

            // 4. DB 변경 성공 후 기존 이미지 삭제
            // 기존 이미지 삭제에 실패하더라도 새 사진 변경은 유지
            try {
                profileImageService.deleteProfileImage(oldImageUrl);
            } catch (IOException e) {
                // 운영 환경에서는 로그로 기록할 수 있음
                System.err.println(
                        "이전 프로필 이미지 삭제 실패: " + e.getMessage()
                );
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "프로필 사진이 성공적으로 변경되었습니다."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

        } catch (IOException | RuntimeException e) {

            // 새 파일 저장 후 DB 업데이트가 실패한 경우 파일 정리
            if (newImageUrl != null) {
                try {
                    profileImageService.deleteProfileImage(newImageUrl);
                } catch (IOException cleanupException) {
                    System.err.println(
                            "새 프로필 이미지 정리 실패: "
                            + cleanupException.getMessage()
                    );
                }
            }

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "프로필 사진 변경 중 오류가 발생했습니다."
            );
        }

        return "redirect:/mypage";
    }
}
