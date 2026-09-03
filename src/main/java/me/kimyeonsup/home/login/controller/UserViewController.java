package me.kimyeonsup.home.login.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.kimyeonsup.home.config.oauth.OAuth2UserCustomService;
import me.kimyeonsup.home.login.domain.dto.SessionUser;
import me.kimyeonsup.home.util.CookieUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Controller
public class UserViewController {

    @GetMapping("/login")
    public String login(HttpServletRequest request, HttpServletResponse response) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        SessionUser sessionUser = (SessionUser) attributes.getRequest()
                .getSession()
                .getAttribute(OAuth2UserCustomService.SESSION_USER_KEY);
        if (sessionUser != null) {
            return "redirect:/blog";
        }

        // 원래 요청 URL 저장
        String referer = request.getHeader("referer");
        if (referer != null && !referer.contains("/login") && !referer.contains("/oauth2")) {
            CookieUtil.addCookie(response, "original_request_url", referer, 300);
        }
        return "oauthLogin";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }
}
