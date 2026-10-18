package me.kimyeonsup.home.domain.draft.adaptor.out.persistence;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.domain.draft.application.port.out.DraftCommandRepository;
import me.kimyeonsup.home.domain.draft.application.port.out.DraftQueryRepository;
import me.kimyeonsup.home.domain.draft.domain.model.DraftArticle;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DraftPersistenceAdapter implements DraftCommandRepository, DraftQueryRepository {

    private final DraftJpaRepository draftJpaRepository;

    @Override
    public Optional<DraftArticle> save(DraftArticle draftArticle) {
        return Optional.ofNullable(draftJpaRepository.save(draftArticle));
    }

    @Override
    public void flush() {
        draftJpaRepository.flush();
    }

    @Override
    public void deleteById(Long id) {
        draftJpaRepository.deleteById(id);
    }

    @Override
    public Optional<DraftArticle> findByIdAndAuthor(Long id, String author) {
        return draftJpaRepository.findByIdAndAuthor(id, author);
    }

    @Override
    public List<DraftArticle> findByAuthorAndDeleted(String author, boolean deleted) {
        return draftJpaRepository.findByAuthorAndDeleteYn(author, deleted);
    }

    @Override
    public boolean existsByMenuId(long menuId) {
        return draftJpaRepository.existsByMenuId(menuId);
    }
}
