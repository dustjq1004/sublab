package me.kimyeonsup.home.domain.article.application.port.mapper;

import me.kimyeonsup.home.domain.article.application.port.in.command.CreateArticleCommand;
import me.kimyeonsup.home.domain.article.application.port.in.result.ArticleResult;
import me.kimyeonsup.home.domain.article.application.port.in.result.ArticleSummaryResult;
import me.kimyeonsup.home.domain.article.domain.model.Article;
import me.kimyeonsup.home.domain.article.domain.model.Thumbnail;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import org.springframework.stereotype.Component;

@Component
public class ArticleCommandMapper {

    public ArticleResult toArticleResult(Article article) {
        return ArticleResult.builder()
                .id(article.getId())
                .menuName(article.getMenu().getName())
                .title(article.getTitle())
                .content(article.getContent())
                .thumbnailUrl(article.getThumbnailUrl())
                .visibility(article.getVisibility())
                .subTitle(article.getSubTitle())
                .author(article.getAuthor())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }

    public ArticleSummaryResult toArticleSummaryResult(Article article) {
        return ArticleSummaryResult.builder()
                .id(article.getId())
                .menuName(article.getMenu().getName())
                .title(article.getTitle())
                .content(article.getContent())
                .thumbnailUrl(article.getThumbnailUrl())
                .visibility(article.getVisibility())
                .subTitle(article.getSubTitle())
                .build();
    }

    public Article toEntity(CreateArticleCommand command) {
        return Article.builder()
                .title(command.title())
                .subTitle(command.subTitle())
                .content(command.content())
                .author(command.author())
                .menu(Menu.builder().id(command.menuId()).build())
                .thumbnailUrl(Thumbnail.of(command.thumbnailUrl(), command.thumbnailLogicalFileName()))
                .author(command.author())
                .visibility(command.visibility())
                .build();
    }
}
