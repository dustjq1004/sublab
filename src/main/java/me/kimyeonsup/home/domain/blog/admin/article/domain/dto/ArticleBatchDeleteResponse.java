package me.kimyeonsup.home.domain.blog.admin.article.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ArticleBatchDeleteResponse {

    int deletedCount;

    @Builder
    public ArticleBatchDeleteResponse(int deletedCount) {
        this.deletedCount = deletedCount;
    }
}
