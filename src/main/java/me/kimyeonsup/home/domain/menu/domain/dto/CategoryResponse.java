package me.kimyeonsup.home.domain.menu.domain.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CategoryResponse {

    private Long id;
    private String name;
    private String emoji;
    private List<MenuResponse> menus;
    private String createdAt;
    private String updatedAt;

    @Builder
    public CategoryResponse(Long id, String name, String emoji, List<MenuResponse> menus, String createdAt,
                            String updatedAt) {
        this.id = id;
        this.name = name;
        this.menus = menus;
        this.emoji = emoji;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
