package me.kimyeonsup.home.domain.draft.application.port.service;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.domain.draft.application.port.in.DraftCommandUseCase;
import me.kimyeonsup.home.domain.draft.application.port.in.DraftQueryUseCase;
import me.kimyeonsup.home.domain.draft.application.port.in.command.CreateDraftCommand;
import me.kimyeonsup.home.domain.draft.application.port.in.command.UpdateDraftCommand;
import me.kimyeonsup.home.domain.draft.application.port.in.result.DraftResult;
import me.kimyeonsup.home.domain.draft.application.port.mapper.DraftCommandMapper;
import me.kimyeonsup.home.domain.draft.application.port.out.DraftCommandRepository;
import me.kimyeonsup.home.domain.draft.application.port.out.DraftQueryRepository;
import me.kimyeonsup.home.domain.draft.domain.model.DraftArticle;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.global.exception.business.DraftConflictException;
import me.kimyeonsup.home.global.exception.business.ResourceNotFoundException;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import me.kimyeonsup.home.global.exception.message.constants.DraftExceptionMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DraftService implements DraftCommandUseCase, DraftQueryUseCase {

    private final DraftQueryRepository draftQueryRepository;
    private final DraftCommandRepository draftCommandRepository;
    private final DraftCommandMapper draftCommandMapper;
    private final MessageSourceResolver messageSourceResolver;

    @Override
    @Transactional(readOnly = true)
    public List<DraftResult> findAllByAuthor(String author) {
        return draftQueryRepository.findByAuthorAndDeleted(author, false).stream()
                .map(draftCommandMapper::toResult)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByMenuId(long menuId) {
        return draftQueryRepository.existsByMenuId(menuId);
    }

    @Override
    @Transactional
    public DraftResult create(CreateDraftCommand command) {
        DraftArticle draft = draftCommandRepository.save(draftCommandMapper.toEntity(command))
                .orElseThrow(() -> notFound(null));
        return draftCommandMapper.toResult(draft);
    }

    @Override
    @Transactional
    public DraftResult update(UpdateDraftCommand command) {
        DraftArticle draft = findByIdAndAuthor(command.id(), command.author());
        if (!Objects.equals(draft.getVersion(), command.version())) {
            throw new DraftConflictException();
        }
        draft.update(command.title(), command.subTitle(), command.content(),
                Menu.builder().id(command.menuId()).build(),
                draftCommandMapper.toThumbnail(command.thumbnailUrl(), command.thumbnailLogicalFileName()));
        draftCommandRepository.flush();
        return draftCommandMapper.toResult(draft);
    }

    @Override
    @Transactional
    public void delete(Long id, String author) {
        findByIdAndAuthor(id, author).delete();
    }

    @Override
    @Transactional
    public void discard(Long id) {
        draftCommandRepository.deleteById(id);
    }

    private DraftArticle findByIdAndAuthor(Long id, String author) {
        return draftQueryRepository.findByIdAndAuthor(id, author).orElseThrow(() -> notFound(id));
    }

    private ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException(messageSourceResolver.resolve(DraftExceptionMessage.DRAFT_NOT_FOUND, id));
    }
}
