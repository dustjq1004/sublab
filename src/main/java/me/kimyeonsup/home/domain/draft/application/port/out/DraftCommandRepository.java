package me.kimyeonsup.home.domain.draft.application.port.out;

import java.util.Optional;
import me.kimyeonsup.home.domain.draft.domain.model.DraftArticle;

public interface DraftCommandRepository {
    Optional<DraftArticle> save(DraftArticle draftArticle);
    void flush();
    void deleteById(Long id);
}
