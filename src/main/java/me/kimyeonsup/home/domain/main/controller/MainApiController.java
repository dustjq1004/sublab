package me.kimyeonsup.home.domain.main.controller;

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.domain.main.domain.dto.LatestArticles;
import me.kimyeonsup.home.domain.main.domain.dto.SearchParams;
import me.kimyeonsup.home.domain.main.domain.dto.SearchedArticles;
import me.kimyeonsup.home.domain.main.service.MainService;
import me.kimyeonsup.home.global.api.domain.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/main")
public class MainApiController {

    private final MainService mainService;

    @GetMapping("/articles/latest")
    public ApiResponse<LatestArticles> getLatestArticles() {
        return ApiResponse.success(mainService.getLatestArticles());
    }

    @GetMapping("/articles/search")
    public ApiResponse<SearchedArticles> getSearchedArticles(@RequestParam("titleParam") SearchParams searchParams) {
        String titleParam = searchParams.getTitleParam();
        if (titleParam.isEmpty()) {
            return ApiResponse.success(new SearchedArticles(Collections.emptyList()));
        }
        return ApiResponse.success(mainService.getArticlesByTitle(titleParam));
    }
}
