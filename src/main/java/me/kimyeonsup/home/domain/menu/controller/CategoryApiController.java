package me.kimyeonsup.home.domain.menu.controller;

import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.domain.menu.domain.dto.AddCategoryRequest;
import me.kimyeonsup.home.domain.menu.domain.dto.CategoriesResponse;
import me.kimyeonsup.home.domain.menu.domain.dto.CategoryResponse;
import me.kimyeonsup.home.domain.menu.domain.dto.DeleteCategoryRequest;
import me.kimyeonsup.home.domain.menu.domain.dto.UpdateCategoryRequest;
import me.kimyeonsup.home.domain.menu.service.CategoryQueryService;
import me.kimyeonsup.home.domain.menu.service.CategoryService;
import me.kimyeonsup.home.global.api.domain.dto.ApiResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CategoryApiController {

    private final CategoryService categoryService;
    private final CategoryQueryService categoryQueryService;

    @GetMapping("/categories")
    public ApiResponse<CategoriesResponse> findCategories() {
        CategoriesResponse response = categoryQueryService.findAll();
        return ApiResponse.success(response);
    }

    @GetMapping("/category")
    public ApiResponse<CategoryResponse> findCategory(@RequestParam long categoryId) {
        CategoryResponse category = categoryQueryService.findById(categoryId);
        return ApiResponse.success(category);
    }

    @PostMapping({"/category/create", "/category"})
    public ApiResponse<CategoryResponse> addCategory(@Validated @RequestBody AddCategoryRequest request) {
        CategoryResponse savedCategory = categoryService.save(request);
        return ApiResponse.success(savedCategory);
    }

    @DeleteMapping("/category/delete")
    public ApiResponse<Void> deleteCategory(@Validated @RequestBody DeleteCategoryRequest request) {
        categoryService.delete(request.getCategoryId());

        return ApiResponse.success(null);
    }

    @PutMapping("/category/update")
    public ApiResponse<CategoryResponse> updateCategory(@Validated @RequestBody UpdateCategoryRequest request) {
        CategoryResponse updatedCategory = categoryService.update(request);
        return ApiResponse.success(updatedCategory);
    }
}
