package me.kimyeonsup.home.domain.article.adaptor.in.web.admin.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArticleBatchUpdateResponse {
    private int updatedCount;

}
