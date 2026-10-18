package me.kimyeonsup.home.domain.draft.application.port.in;

import me.kimyeonsup.home.domain.draft.application.port.in.command.CreateDraftCommand;
import me.kimyeonsup.home.domain.draft.application.port.in.command.UpdateDraftCommand;
import me.kimyeonsup.home.domain.draft.application.port.in.result.DraftResult;

public interface DraftCommandUseCase {
    DraftResult create(CreateDraftCommand command);
    DraftResult update(UpdateDraftCommand command);
    void delete(Long id, String author);
    void discard(Long id);
}
