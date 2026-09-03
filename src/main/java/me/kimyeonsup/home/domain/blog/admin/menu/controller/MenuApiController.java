package me.kimyeonsup.home.domain.blog.admin.menu.controller;

import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.dto.MenusResponse;
import me.kimyeonsup.home.domain.blog.admin.menu.service.MenuQueryService;
import me.kimyeonsup.home.global.api.domain.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MenuApiController {

    private final MenuQueryService menuQueryService;

    @GetMapping("/menus")
    public ApiResponse<MenusResponse> findMenus() {
        return ApiResponse.success(menuQueryService.findAll());
    }

}
