package me.kimyeonsup.home.domain.blog.admin.menu.domain.dto;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Category;

@Getter
public class CategoriesResponse {

    private List<CategoryResponse> categories;

    public CategoriesResponse(List<CategoryResponse> categories) {
        this.categories = categories;
    }

    public static CategoriesResponse ofNotContainsMenu(List<Category> categories) {
        List<CategoryResponse> categoriesDto = Optional.ofNullable(categories)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(category -> CategoryResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .emoji(category.getEmoji())
                        .createdAt(category.getCreatedAt())
                        .updatedAt(category.getUpdatedAt())
                        .build())
                .toList();
        return new CategoriesResponse(categoriesDto);

    }
}
