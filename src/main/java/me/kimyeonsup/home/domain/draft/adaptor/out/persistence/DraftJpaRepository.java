package me.kimyeonsup.home.domain.draft.adaptor.out.persistence;

import java.util.List;
import java.util.Optional;
import me.kimyeonsup.home.domain.draft.domain.model.DraftArticle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DraftJpaRepository extends JpaRepository<DraftArticle, Long> {
    Optional<DraftArticle> findByIdAndAuthor(Long id, String author);
    List<DraftArticle> findByAuthorAndDeleteYn(String author, Boolean deleted);
    boolean existsByMenuId(long menuId);
}
