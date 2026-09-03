package me.kimyeonsup.home.domain.blog.admin.menu.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.dto.CategoryResponse;
import me.kimyeonsup.home.domain.blog.admin.menu.service.CategoryQueryService;
import me.kimyeonsup.home.global.api.domain.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class NavigatorApiController {

    private final CategoryQueryService categoryQueryService;

    @GetMapping("/navigate")
    public ApiResponse<List<CategoryResponse>> findAllCategories() {
        List<CategoryResponse> categories = categoryQueryService.findAllWithMenusAsResponse();
        return ApiResponse.success(categories);
    }
}
