package me.kimyeonsup.home.domain.article.application.port.in;

import me.kimyeonsup.home.domain.article.application.port.in.command.CreateArticleCommand;
import me.kimyeonsup.home.domain.article.application.port.in.command.UpdateArticleCommand;
import me.kimyeonsup.home.domain.article.application.port.in.result.ArticleResult;

public interface ArticleCommandUseCase {
    ArticleResult create(CreateArticleCommand article);
    ArticleResult update(UpdateArticleCommand article);
    void delete(Long id, String author);
}
