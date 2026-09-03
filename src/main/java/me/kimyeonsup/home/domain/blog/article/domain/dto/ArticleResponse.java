package me.kimyeonsup.home.domain.blog.article.domain.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import me.kimyeonsup.home.domain.blog.article.domain.vo.Visibility;

@Builder
public record ArticleResponse (
    Long id,
    String title,
    String subTitle,
    String content,
    String author,
    String menuName,
    String thumbnailLogicalName,
    String thumbnailUrl,
    Visibility visibility,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
