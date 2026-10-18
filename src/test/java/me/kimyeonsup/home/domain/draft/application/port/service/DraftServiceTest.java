package me.kimyeonsup.home.domain.draft.application.port.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import me.kimyeonsup.home.domain.article.domain.model.Thumbnail;
import me.kimyeonsup.home.domain.draft.application.port.in.command.CreateDraftCommand;
import me.kimyeonsup.home.domain.draft.application.port.in.command.UpdateDraftCommand;
import me.kimyeonsup.home.domain.draft.application.port.mapper.DraftCommandMapper;
import me.kimyeonsup.home.domain.draft.application.port.out.DraftCommandRepository;
import me.kimyeonsup.home.domain.draft.application.port.out.DraftQueryRepository;
import me.kimyeonsup.home.domain.draft.domain.model.DraftArticle;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.global.exception.business.DraftConflictException;
import me.kimyeonsup.home.global.exception.business.ResourceNotFoundException;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DraftServiceTest {

    @Mock DraftQueryRepository draftQueryRepository;
    @Mock DraftCommandRepository draftCommandRepository;
    @Mock MessageSourceResolver messageSourceResolver;
    @Spy DraftCommandMapper draftCommandMapper = new DraftCommandMapper();
    @InjectMocks DraftService draftService;

    @Test
    void findAllByAuthor() {
        when(draftQueryRepository.findByAuthorAndDeleted("author", false)).thenReturn(List.of(draft(1L, 0L)));

        assertThat(draftService.findAllByAuthor("author")).singleElement()
                .satisfies(result -> assertThat(result.id()).isEqualTo(1L));
    }

    @Test
    void existsByMenuIdDelegatesToQueryRepository() {
        // DRAFT-QUERY-001
        when(draftQueryRepository.existsByMenuId(2L)).thenReturn(true);

        assertThat(draftService.existsByMenuId(2L)).isTrue();

        verify(draftQueryRepository).existsByMenuId(2L);
    }

    @Test
    void create() {
        var command = new CreateDraftCommand("title", "sub", "content", 2L, "url", "thumb.png", "author");
        when(draftCommandRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> Optional.of(invocation.getArgument(0)));

        var result = draftService.create(command);

        assertThat(result.title()).isEqualTo("title");
        assertThat(result.author()).isEqualTo("author");
        assertThat(result.menuId()).isEqualTo(2L);
    }

    @Test
    void updateChecksVersionAndAuthor() {
        DraftArticle draft = draft(1L, 3L);
        when(draftQueryRepository.findByIdAndAuthor(1L, "author")).thenReturn(Optional.of(draft));
        var command = new UpdateDraftCommand(1L, 3L, "changed", "sub", "body", 4L, "url", "thumb.png", "author");

        var result = draftService.update(command);

        assertThat(result.title()).isEqualTo("changed");
        assertThat(result.menuId()).isEqualTo(4L);
    }

    @Test
    void updateRejectsStaleVersion() {
        when(draftQueryRepository.findByIdAndAuthor(1L, "author")).thenReturn(Optional.of(draft(1L, 3L)));
        var command = new UpdateDraftCommand(1L, 2L, "changed", "sub", "body", 4L, "", "", "author");

        assertThatThrownBy(() -> draftService.update(command)).isInstanceOf(DraftConflictException.class);
    }

    @Test
    void deleteUsesAuthorAndSoftDeletes() {
        DraftArticle draft = draft(1L, 0L);
        when(draftQueryRepository.findByIdAndAuthor(1L, "author")).thenReturn(Optional.of(draft));

        draftService.delete(1L, "author");

        assertThat(draft.getDeleteYn()).isTrue();
    }

    @Test
    void differentAuthorIsNotFound() {
        when(draftQueryRepository.findByIdAndAuthor(1L, "other")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> draftService.delete(1L, "other")).isInstanceOf(ResourceNotFoundException.class);
        verify(draftQueryRepository).findByIdAndAuthor(1L, "other");
    }

    @Test
    void discardPermanentlyDeletesPublishedDraft() {
        draftService.discard(1L);

        verify(draftCommandRepository).deleteById(1L);
    }

    private DraftArticle draft(Long id, Long version) {
        DraftArticle draft = DraftArticle.builder().title("title").subTitle("sub").content("body")
                .author("author").menu(Menu.builder().id(2L).build()).thumbnailUrl(Thumbnail.empty()).build();
        ReflectionTestUtils.setField(draft, "id", id);
        ReflectionTestUtils.setField(draft, "version", version);
        return draft;
    }
}
