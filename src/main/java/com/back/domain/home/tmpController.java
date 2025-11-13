package com.back.domain.home;

import com.back.global.config.security.SecurityUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class tmpController {

    // todo 추후 프론트 페이지 개발 후 삭제
    @GetMapping("/tmp-for-complete-join-of-oauth2-user")
    public String tmpForCompleteJoinOfOAuth2User(@AuthenticationPrincipal SecurityUser securityUser) {
        if(securityUser == null) {
            System.out.println("securityUser is null");
        }else {
            System.out.println("securityUser 살아있음: " + securityUser.getId());
        }
        return "onboarding_nickname";
    }
}
