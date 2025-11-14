package com.back.domain.home;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class tmpController {

    // todo 추후 프론트 페이지 개발 후 삭제
    @GetMapping("/tmp-for-complete-join-of-oauth2-user")
    public String tmpForCompleteJoinOfOAuth2User(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "onboarding_nickname";
    }
}
