package me.kimyeonsup.home.domain.article.adaptor.in.web.blog.mapper;

import me.kimyeonsup.home.domain.article.adaptor.in.web.admin.request.ArticlePagingRequest;
import me.kimyeonsup.home.domain.article.adaptor.in.web.blog.request.AddArticleRequest;
import me.kimyeonsup.home.domain.article.adaptor.in.web.blog.request.UpdateArticleRequest;
import me.kimyeonsup.home.domain.article.adaptor.in.web.blog.response.ArticleListViewResponse;
import me.kimyeonsup.home.domain.article.adaptor.in.web.blog.response.ArticleResponse;
import me.kimyeonsup.home.domain.article.adaptor.in.web.blog.response.ArticlesPaginationResponse;
import me.kimyeonsup.home.domain.article.application.port.in.command.ArticleSearchQuery;
import me.kimyeonsup.home.domain.article.application.port.in.command.CreateArticleCommand;
import me.kimyeonsup.home.domain.article.application.port.in.command.UpdateArticleCommand;
import me.kimyeonsup.home.domain.article.application.port.in.result.ArticleResult;
import me.kimyeonsup.home.domain.article.application.port.in.result.ArticleSummaryResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

@Component
public class ArticleWebMapper {

    public ArticleSearchQuery toArticleSearchQuery(ArticlePagingRequest request) {
        return ArticleSearchQuery.builder()
                        .categoryId(request.getCategoryId())
                        .menuId(request.getMenuId())
                        .menuName(request.getMenuName())
                        .searchKeyword(request.getSearchKeyword())
                        .pageNumber(request.getPageNumber() - 1)
                        .pageSize(request.getPageSize())
                        .sortedBy(request.getSortBy())
                        .direction(Direction.fromString(request.getDirection()))
                        .visibility(request.getVisibility())
                        .build();
    }

    public ArticlesPaginationResponse toArticlesPaginationResponse(Page<ArticleSummaryResult> articles) {
        return ArticlesPaginationResponse.builder()
                .pageNumber(articles.getNumber())
                .pageSize(articles.getSize())
                .numberOfElements(articles.getNumberOfElements())
                .isFirst(articles.isFirst())
                .isLast(articles.isLast())
                .totalPages(articles.getTotalPages())
                .totalCount(articles.getTotalElements())
                .articles(articles.stream()
                        .map(ArticleListViewResponse::new)
                        .toList())
                .build();
    }

    public ArticleResponse toArticleResponse(ArticleResult article) {
        return ArticleResponse.builder()
                .id(article.id())
                .title(article.title())
                .subTitle(article.subTitle())
                .content(article.content())
                .author(article.author())
                .menuName(article.menuName())
                .thumbnailLogicalName(article.thumbnailLogicalFileName())
                .thumbnailUrl(article.thumbnailUrl())
                .visibility(article.visibility())
                .createdAt(article.createdAt())
                .updatedAt(article.updatedAt())
                .build();
    }

    public CreateArticleCommand toCreateArticleCommand(AddArticleRequest request, String author) {
        return CreateArticleCommand.builder()
                .title(request.getTitle())
                .subTitle(request.getSubTitle())
                .content(request.getContent())
                .author(author)
                .menuId(request.getMenuId())
                .thumbnailLogicalFileName(request.getThumbnailLogicalFileName())
                .thumbnailUrl(request.getThumbnailUrl())
                .visibility(request.getVisibility())
                .drateId(request.getDraftId())
                .build();
    }

    public UpdateArticleCommand toUpdateArticleCommand(UpdateArticleRequest request, String author) {
        return UpdateArticleCommand.builder()
                .id(request.getId())
                .title(request.getTitle())
                .subTitle(request.getSubTitle())
                .content(request.getContent())
                .author(author)
                .menuId(request.getMenuId())
                .thumbnailLogicalFileName(request.getThumbnailLogicalFileName())
                .thumbnailUrl(request.getThumbnailUrl())
                .visibility(request.getVisibility())
                .build();
    }
}
