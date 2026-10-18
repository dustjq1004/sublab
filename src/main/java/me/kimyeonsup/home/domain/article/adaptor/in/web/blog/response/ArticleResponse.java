package me.kimyeonsup.home.domain.article.adaptor.in.web.blog.response;

import java.time.LocalDateTime;
import lombok.Builder;
import me.kimyeonsup.home.domain.article.domain.model.Visibility;

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
