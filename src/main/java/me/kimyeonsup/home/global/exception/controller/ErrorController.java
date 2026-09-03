package me.kimyeonsup.home.global.exception.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

    @GetMapping("/login-error")
    public String loginError() {

        return "error/login-error";
    }
}
