package me.kimyeonsup.home.domain.blog.article.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.dto.CategoryResponse;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.dto.MenuResponse;
import me.kimyeonsup.home.domain.blog.admin.menu.service.CategoryQueryService;
import me.kimyeonsup.home.domain.blog.admin.menu.service.MenuQueryService;
import me.kimyeonsup.home.domain.blog.article.assembler.ArticleDtoAssembler;
import me.kimyeonsup.home.domain.blog.article.domain.dto.ArticlePrevNextResponse;
import me.kimyeonsup.home.domain.blog.article.domain.entity.Article;
import me.kimyeonsup.home.domain.blog.article.service.ArticleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Controller
@RequestMapping("/blog")
public class BlogViewController {

    private final ArticleService articleService;
    private final CategoryQueryService categoryQueryService;
    private final MenuQueryService menuQueryService;
    private final ArticleDtoAssembler articleDtoAssembler;

    @GetMapping
    public String getAllArticles(Model model) {
        List<CategoryResponse> categories = categoryQueryService.findAllWithMenusAsResponse();

        model.addAttribute("categories", categories);

        return "blog/index";
    }

    @GetMapping("/{menuName}")
    public String findArticlesByMenuName(@PathVariable String menuName, Model model) {
        List<CategoryResponse> categories = categoryQueryService.findAllWithMenusAsResponse();

        MenuResponse menu = menuQueryService.findByName(menuName);

        model.addAttribute("categories", categories);
        model.addAttribute("menu", menu);

        return "blog/index";
    }

    @GetMapping("/{menuName}/{id}")
    public String getArticle(@PathVariable String menuName, @PathVariable Long id, Model model) {
        List<CategoryResponse> categories = categoryQueryService.findAllWithMenusAsResponse();
        Article article = articleService.findById(id);
        ArticlePrevNextResponse prevNextArticle = articleService.findPrevNextArticle(id, menuName);

        model.addAttribute("categories", categories);
        model.addAttribute("article", articleDtoAssembler.toResponse(article));
        model.addAttribute("prevNextArticle", prevNextArticle);
        model.addAttribute("title", article.getTitle());
        model.addAttribute("description", article.getSubTitle());
        model.addAttribute("type", "article");

        return "blog/article";
    }

    @GetMapping("/new-article")
    public String newArticle(@RequestParam(required = false) Long id, Model model) {
        if (id == null) {
            model.addAttribute("article", articleDtoAssembler.toEmpty());
        } else {
            Article article = articleService.findById(id);
            model.addAttribute("article", articleDtoAssembler.toEditResponse(article));
        }

        model.addAttribute("menus", menuQueryService.findAll());

        return "blog/newArticle";
    }
}
