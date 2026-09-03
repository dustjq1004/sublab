package me.kimyeonsup.home.domain.blog.admin.menu.domain.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Menu;
import me.kimyeonsup.home.util.DateTimeFormat;

@NoArgsConstructor
@Getter
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


    public MenuResponse(Menu menu) {
        this(menu, 0);
    }

    public MenuResponse(Menu menu, int articleCount) {
        this.id = menu.getId();
        this.name = menu.getName();
        this.categoryId = menu.getCategory().getId();
        this.categoryEmoji = menu.getCategory().getEmoji();
        this.categoryName = menu.getCategory().getName();
        this.articleCount = articleCount;
    }

    @Builder
    public MenuResponse(Long id, String emoji, String name, String description,
                        Long categoryId, String categoryEmoji, String categoryName,
                        LocalDateTime createdAt,
                        LocalDateTime updatedAt,
                        Integer articleCount) {
        this.id = id;
        this.emoji = emoji;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.categoryEmoji = categoryEmoji;
        this.categoryName = categoryName;
        this.createdAt = DateTimeFormat.diffDateFromNow(createdAt);
        this.updatedAt = DateTimeFormat.diffDateFromNow(updatedAt);
        this.articleCount = articleCount;
    }
}
