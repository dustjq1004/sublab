package me.kimyeonsup.home.domain.draft.application.port.out;

import java.util.List;
import java.util.Optional;
import me.kimyeonsup.home.domain.draft.domain.model.DraftArticle;

public interface DraftQueryRepository {
    Optional<DraftArticle> findByIdAndAuthor(Long id, String author);
    List<DraftArticle> findByAuthorAndDeleted(String author, boolean deleted);
    boolean existsByMenuId(long menuId);
}
