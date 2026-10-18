package me.kimyeonsup.home.domain.menu.service.mapper;

import java.util.Collections;
import java.util.Map;
import me.kimyeonsup.home.domain.menu.domain.dto.AddCategoryRequest;
import me.kimyeonsup.home.domain.menu.domain.dto.CategoryResponse;
import me.kimyeonsup.home.domain.menu.domain.dto.MenuResponse;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.util.DateTimeFormat;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public Category toCategory(AddCategoryRequest request) {
        return Category.builder()
                .name(request.getName())
                .build();
    }

    public CategoryResponse toCreatedResponse(Category category) {
        return baseResponse(category)
                .menus(Collections.emptyList())
                .build();
    }

    public CategoryResponse toDetailResponse(Category category) {
        return baseResponse(category)
                .createdAt(DateTimeFormat.diffDateFromNow(null))
                .updatedAt(DateTimeFormat.diffDateFromNow(null))
                .build();
    }

    public CategoryResponse toUpdatedResponse(Category category) {
        return baseResponse(category)
                .createdAt(DateTimeFormat.diffDateFromNow(null))
                .updatedAt(DateTimeFormat.diffDateFromNow(category.getUpdatedAt()))
                .build();
    }

    public CategoryResponse toListItemResponse(Category category) {
        return baseResponse(category)
                .createdAt(DateTimeFormat.diffDateFromNow(category.getCreatedAt()))
                .updatedAt(DateTimeFormat.diffDateFromNow(category.getUpdatedAt()))
                .build();
    }

    public CategoryResponse toNavigationResponse(Category category, Map<Long, Integer> articleCountByMenuId) {
        return baseResponse(category)
                .menus(category.getMenus().stream()
                        .map(menu -> MenuResponse.builder()
                                .id(menu.getId())
                                .name(menu.getName())
                                .categoryId(category.getId())
                                .categoryEmoji(category.getEmoji())
                                .categoryName(category.getName())
                                .articleCount(articleCountByMenuId.getOrDefault(menu.getId(), 0))
                                .build())
                        .toList())
                .build();
    }

    private CategoryResponse.CategoryResponseBuilder baseResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .emoji(category.getEmoji());
    }
}
