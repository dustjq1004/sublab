package me.kimyeonsup.home.domain.blog.admin.article.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class ArticleSelectCondition {
    private Long categoryId;
    private Long menuId;
    private String title;
    private String visibility;

    @Builder
    public ArticleSelectCondition(Long categoryId, Long menuId, String title, String visibility) {
        this.categoryId = categoryId;
        this.menuId = menuId;
        this.title = title;
        this.visibility = visibility;
    }
}
