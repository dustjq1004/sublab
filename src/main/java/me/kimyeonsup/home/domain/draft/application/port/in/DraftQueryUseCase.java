package me.kimyeonsup.home.domain.draft.application.port.in;

import java.util.List;
import me.kimyeonsup.home.domain.draft.application.port.in.result.DraftResult;

public interface DraftQueryUseCase {
    List<DraftResult> findAllByAuthor(String author);
    boolean existsByMenuId(long menuId);
}
