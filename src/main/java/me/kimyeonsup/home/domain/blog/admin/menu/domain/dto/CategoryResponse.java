package me.kimyeonsup.home.domain.blog.admin.menu.domain.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Category;
import me.kimyeonsup.home.util.DateTimeFormat;

@Getter
public class CategoryResponse {

    private Long id;
    private String name;
    private String emoji;
    private List<MenuResponse> menus;
    private String createdAt;
    private String updatedAt;

    public CategoryResponse(Category category) {
        this(category, Collections.emptyMap());
    }

    public CategoryResponse(Category category, Map<Long, Integer> articleCountMap) {
        this.id = category.getId();
        this.name = category.getName();
        this.emoji = category.getEmoji();
        this.menus = Optional.ofNullable(category.getMenus())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(menu -> new MenuResponse(menu, articleCountMap.getOrDefault(menu.getId(), 0)))
                .toList();
    }

    @Builder
    public CategoryResponse(Long id, String name, String emoji, List<MenuResponse> menus, LocalDateTime createdAt,
                            LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.menus = menus;
        this.emoji = emoji;
        this.createdAt = DateTimeFormat.diffDateFromNow(createdAt);
        this.updatedAt = DateTimeFormat.diffDateFromNow(updatedAt);
    }
}
