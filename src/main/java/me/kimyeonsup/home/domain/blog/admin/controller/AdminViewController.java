package me.kimyeonsup.home.domain.blog.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminViewController {

    @GetMapping
    public String index(Model model) {
        return "blog/admin/main";
    }

    @GetMapping("/frag/categories")
    public String getCategoriesFragment(Model model) {
        return "fragments :: categoriesFragment";
    }

    @GetMapping("/frag/articles")
    public String getArticlesFragment(Model model) {
        return "fragments :: articlesFragment";
    }
}
