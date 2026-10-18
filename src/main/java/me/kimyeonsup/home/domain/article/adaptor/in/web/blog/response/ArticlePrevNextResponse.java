package me.kimyeonsup.home.domain.article.adaptor.in.web.blog.response;

import lombok.Builder;
import lombok.Getter;
import me.kimyeonsup.home.domain.article.adaptor.out.persistence.ArticlePrevNextDto;

@Getter
public class ArticlePrevNextResponse {

    private final Long prevId;
    private final String prevTitle;
    private final Long nextId;
    private final String nextTitle;

    private final boolean isPrev;
    private final boolean isNext;

    @Builder
    public ArticlePrevNextResponse(ArticlePrevNextDto articlePrevNextDto) {
        this.prevId = articlePrevNextDto.getPrevId();
        this.prevTitle = articlePrevNextDto.getPrevTitle();
        this.nextId = articlePrevNextDto.getNextId();
        this.nextTitle = articlePrevNextDto.getNextTitle();
        this.isPrev = articlePrevNextDto.getPrevId() > 0;
        this.isNext = articlePrevNextDto.getNextId() > 0;
    }
}
