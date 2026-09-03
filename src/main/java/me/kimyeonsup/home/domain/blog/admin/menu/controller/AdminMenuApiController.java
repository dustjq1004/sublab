package me.kimyeonsup.home.domain.blog.admin.menu.controller;

import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.dto.AddMenuRequest;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.dto.DeleteMenuRequest;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.dto.MenuResponse;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.dto.MenusResponse;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.dto.UpdateMenuRequest;
import me.kimyeonsup.home.domain.blog.admin.menu.service.MenuQueryService;
import me.kimyeonsup.home.domain.blog.admin.menu.service.MenuService;
import me.kimyeonsup.home.global.api.domain.dto.ApiResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminMenuApiController {

    private final MenuService menuService;
    private final MenuQueryService menuQueryService;

    @GetMapping("/menus")
    public ApiResponse<MenusResponse> findMenus() {
        return ApiResponse.success(menuQueryService.findAll());
    }

    @GetMapping("/menus/{categoryId}")
    public ApiResponse<MenusResponse> findMenusByCategoryId(@PathVariable long categoryId) {
        return ApiResponse.success(menuQueryService.findByCategory(categoryId));
    }

    @GetMapping("/menu/{menuId}")
    public ApiResponse<MenuResponse> findById(@PathVariable long menuId) {
        return ApiResponse.success(menuQueryService.findById(menuId));
    }

    @PostMapping("/menu")
    public ApiResponse<MenuResponse> addMenu(@Validated @RequestBody AddMenuRequest request) {
        MenuResponse savedMenu = menuService.save(request);
        return ApiResponse.success(savedMenu);
    }

    @DeleteMapping("/menu/delete")
    public ApiResponse<MenuResponse> deleteMenu(@RequestBody DeleteMenuRequest request) {
        long menuId = request.getMenuId();
        MenuResponse deletedMenu = menuService.delete(menuId);
        return ApiResponse.success(deletedMenu);
    }

    @PutMapping("/menu/update")
    public ApiResponse<MenuResponse> updateMenu(@RequestBody UpdateMenuRequest request) {
        MenuResponse updatedMenu = menuService.update(request);
        return ApiResponse.success(updatedMenu);
    }
}
