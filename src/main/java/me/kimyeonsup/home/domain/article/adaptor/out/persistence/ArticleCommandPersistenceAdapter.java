package me.kimyeonsup.home.domain.article.adaptor.out.persistence;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.domain.article.application.port.out.ArticleCommandRepository;
import me.kimyeonsup.home.domain.article.domain.model.Article;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleCommandPersistenceAdapter implements ArticleCommandRepository {

    private final ArticleCommandJpaRepository articleCommandJpaRepository;

    @Override
    public Optional<Article> save(Article article) {
        return Optional.ofNullable(articleCommandJpaRepository.save(article));
    }

    @Override
    public void deleteBy(Long id) {
        articleCommandJpaRepository.deleteById(id);
    }

    @Override
    public int deleteByIds(List<Long> ids) {
        return articleCommandJpaRepository.deleteByIds(ids);
    }
}
