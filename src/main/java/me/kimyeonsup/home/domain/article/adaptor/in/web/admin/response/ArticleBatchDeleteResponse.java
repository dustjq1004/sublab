package me.kimyeonsup.home.domain.article.adaptor.in.web.admin.response;

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
