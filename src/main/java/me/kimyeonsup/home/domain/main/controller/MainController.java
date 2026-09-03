package me.kimyeonsup.home.domain.main.controller;

import me.kimyeonsup.home.domain.main.domain.dto.title.TitleWords;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String mainPage(Model model) {
        model.addAttribute("titleWords", TitleWords.values());
        return "main";
    }
}
