package me.kimyeonsup.home.domain.menu.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MenuResponse {

    private Long id;
    private String emoji;
    private String name;
    private String description;
    private String createdAt;
    private String updatedAt;
    private Integer articleCount;

    private Long categoryId;
    private String categoryEmoji;
    private String categoryName;
}
