package me.kimyeonsup.home.domain.article.application.port.out;

import java.util.List;
import java.util.Optional;
import me.kimyeonsup.home.domain.article.domain.model.Article;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleCommandRepository {

    Optional<Article> save(Article article);
    void deleteBy(Long id);
    int deleteByIds(@Param("ids") List<Long> ids);

}
