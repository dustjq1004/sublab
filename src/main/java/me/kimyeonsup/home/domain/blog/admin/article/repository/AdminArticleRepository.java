package me.kimyeonsup.home.domain.blog.admin.article.repository;

import java.util.List;
import me.kimyeonsup.home.domain.blog.admin.article.domain.entity.AdminArticle;
import me.kimyeonsup.home.domain.blog.article.domain.vo.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminArticleRepository extends JpaRepository<AdminArticle, Long> {


    Page<AdminArticle> findByMenu_IdAndTitleContaining(PageRequest pageRequest, Long menuId, String title);

    Page<AdminArticle> findByMenu_IdAndVisibilityAndTitleContaining(PageRequest pageRequest, Long menuId,
            Visibility visibility, String title);

    Page<AdminArticle> findByMenu_CategoryIdAndTitleContaining(PageRequest pageRequest, Long categoryId, String title);

    Page<AdminArticle> findByMenu_CategoryIdAndVisibilityAndTitleContaining(PageRequest pageRequest, Long categoryId,
            Visibility visibility, String title);

    Page<AdminArticle> findByTitleContaining(PageRequest pageRequest, String title);

    Page<AdminArticle> findByVisibilityAndTitleContaining(PageRequest pageRequest, Visibility visibility, String title);

    @Modifying
    @Query("DELETE FROM AdminArticle a WHERE a.id IN :ids")
    int deleteByIds(@Param("ids") List<Long> ids);
}
