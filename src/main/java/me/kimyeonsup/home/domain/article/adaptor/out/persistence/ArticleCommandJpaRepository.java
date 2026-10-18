package me.kimyeonsup.home.domain.article.adaptor.out.persistence;

import java.util.List;
import me.kimyeonsup.home.domain.article.domain.model.Article;
import me.kimyeonsup.home.domain.article.domain.model.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleCommandJpaRepository extends JpaRepository<Article, Long> {


    Page<Article> findByMenu_IdAndTitleContaining(PageRequest pageRequest, Long menuId, String title);

    Page<Article> findByMenu_IdAndVisibilityAndTitleContaining(PageRequest pageRequest, Long menuId,
                                                               Visibility visibility, String title);

    Page<Article> findByMenu_CategoryIdAndTitleContaining(PageRequest pageRequest, Long categoryId, String title);

    Page<Article> findByMenu_CategoryIdAndVisibilityAndTitleContaining(PageRequest pageRequest, Long categoryId,
                                                                       Visibility visibility, String title);

    Page<Article> findByTitleContaining(PageRequest pageRequest, String title);

    Page<Article> findByVisibilityAndTitleContaining(PageRequest pageRequest, Visibility visibility, String title);

    @Modifying
    @Query("DELETE FROM Article a WHERE a.id IN :ids")
    int deleteByIds(@Param("ids") List<Long> ids);
}
