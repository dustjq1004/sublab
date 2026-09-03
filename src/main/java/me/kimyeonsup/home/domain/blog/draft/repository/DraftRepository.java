package me.kimyeonsup.home.domain.blog.draft.repository;

import me.kimyeonsup.home.domain.blog.draft.domain.entity.DraftArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DraftRepository extends JpaRepository<DraftArticle, Long> {

    List<DraftArticle> findByAuthorAndDeleteYn(String author, Boolean delete);
}
