package me.kimyeonsup.home.domain.blog.draft.service;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kimyeonsup.home.domain.blog.draft.domain.dto.AddDraftArticle;
import me.kimyeonsup.home.domain.blog.draft.domain.dto.UpdateDraftArticle;
import me.kimyeonsup.home.domain.blog.draft.domain.entity.DraftArticle;
import me.kimyeonsup.home.domain.blog.draft.repository.DraftRepository;
import me.kimyeonsup.home.global.exception.business.DraftConflictException;
import me.kimyeonsup.home.global.exception.business.ResourceNotFoundException;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import me.kimyeonsup.home.global.exception.message.constants.DraftExceptionMessage;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class DraftService {

    private static final int MAX_TRANSIENT_LOCK_RETRY_COUNT = 2;
    private static final long TRANSIENT_LOCK_RETRY_BACKOFF_MILLIS = 100L;

    private final DraftRepository draftRepository;
    private final MessageSourceResolver messageSourceResolver;
    private final TransactionTemplate transactionTemplate;

    public List<DraftArticle> findByAuthorAndDelete(String author) {
        return draftRepository.findByAuthorAndDeleteYn(author, false);
    }

    public DraftArticle save(AddDraftArticle addDraftArticle, String author) {
        return draftRepository.save(addDraftArticle.toEntity(author));
    }

    public DraftArticle update(UpdateDraftArticle updateDraftArticle) {
        int attempt = 0;
        while (true) {
            try {
                return transactionTemplate.execute(status -> updateOnce(updateDraftArticle));
            } catch (ObjectOptimisticLockingFailureException | DraftConflictException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                if (!isTransientLockFailure(ex) || attempt >= MAX_TRANSIENT_LOCK_RETRY_COUNT) {
                    throw ex;
                }
                attempt++;
                sleepBeforeRetry();
            }
        }
    }

    private DraftArticle updateOnce(UpdateDraftArticle updateDraftArticle) {
        Long id = updateDraftArticle.getId();
        DraftArticle draftArticle = draftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        DraftExceptionMessage.DRAFT_NOT_FOUND, id
                )));

        if (!Objects.equals(draftArticle.getVersion(), updateDraftArticle.getVersion())) {
            throw new DraftConflictException();
        }

        return draftArticle.updateAll(updateDraftArticle);
    }

    private boolean isTransientLockFailure(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof CannotAcquireLockException
                    || current instanceof DeadlockLoserDataAccessException
                    || current instanceof PessimisticLockingFailureException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(TRANSIENT_LOCK_RETRY_BACKOFF_MILLIS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Draft update retry interrupted.", ex);
        }
    }

    @Transactional
    public void delete(Long id) {
        DraftArticle draftArticle = draftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        DraftExceptionMessage.DRAFT_NOT_FOUND, id
                )));
        draftArticle.delete();
    }

    public void deleteIfExistsForArticlePublish(Long id) {
        try {
            transactionTemplate.execute(status -> {
                DraftArticle draftArticle = draftRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                                DraftExceptionMessage.DRAFT_NOT_FOUND, id
                        )));
                draftArticle.delete();
                return null;
            });
        } catch (ResourceNotFoundException | ObjectOptimisticLockingFailureException ex) {
            log.warn("Draft cleanup after article publish failed. draftId={}", id, ex);
        }
    }
}
