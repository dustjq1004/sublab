package me.kimyeonsup.home.domain.blog.admin.article.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArticleBatchUpdateResponse {
    private int updatedCount;

}
