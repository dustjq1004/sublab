package me.kimyeonsup.home.domain.blog.draft.service;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import me.kimyeonsup.home.domain.blog.draft.domain.entity.DraftArticle;
import me.kimyeonsup.home.domain.blog.draft.repository.DraftRepository;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class DraftServiceTest {

    @Mock
    DraftRepository draftRepository;

    @Mock
    MessageSourceResolver messageSourceResolver;

    @Mock
    TransactionTemplate transactionTemplate;

    @InjectMocks
    DraftService draftService;

    @BeforeEach
    void setUp() {
        when(transactionTemplate.execute(org.mockito.ArgumentMatchers.<TransactionCallback<Void>>any()))
                .thenAnswer(invocation -> invocation.<TransactionCallback<Void>>getArgument(0).doInTransaction(null));
    }

    @Test
    @DisplayName("게시글 발행 후 draft 정리는 존재하지 않는 draft여도 실패하지 않는다.")
    void deleteIfExistsForArticlePublishIgnoresNotFoundDraft() {
        Long draftId = 1L;
        when(draftRepository.findById(draftId)).thenReturn(Optional.empty());

        assertThatNoException()
                .isThrownBy(() -> draftService.deleteIfExistsForArticlePublish(draftId));

        verify(draftRepository, never()).deleteById(draftId);
    }

    @Test
    @DisplayName("게시글 발행 후 draft 정리는 version 충돌이 발생해도 실패하지 않는다.")
    void deleteIfExistsForArticlePublishIgnoresOptimisticLockFailure() {
        Long draftId = 1L;
        DraftArticle draftArticle = mock(DraftArticle.class);
        when(draftRepository.findById(draftId)).thenReturn(Optional.of(draftArticle));
        doThrow(new ObjectOptimisticLockingFailureException(DraftArticle.class, draftId))
                .when(draftArticle).delete();

        assertThatNoException()
                .isThrownBy(() -> draftService.deleteIfExistsForArticlePublish(draftId));
    }
}
