package me.kimyeonsup.home.domain.draft.adaptor.in.web.mapper;

import me.kimyeonsup.home.domain.draft.adaptor.in.web.request.CreateDraftRequest;
import me.kimyeonsup.home.domain.draft.adaptor.in.web.request.UpdateDraftRequest;
import me.kimyeonsup.home.domain.draft.adaptor.in.web.response.DraftResponse;
import me.kimyeonsup.home.domain.draft.application.port.in.command.CreateDraftCommand;
import me.kimyeonsup.home.domain.draft.application.port.in.command.UpdateDraftCommand;
import me.kimyeonsup.home.domain.draft.application.port.in.result.DraftResult;
import org.springframework.stereotype.Component;

@Component
public class DraftWebMapper {

    public CreateDraftCommand toCreateCommand(CreateDraftRequest request, String author) {
        return new CreateDraftCommand(request.getTitle(), request.getSubTitle(), request.getContent(),
                request.getMenuId(), request.getThumbnailUrl(), request.getThumbnailLogicalFileName(), author);
    }

    public UpdateDraftCommand toUpdateCommand(Long id, UpdateDraftRequest request, String author) {
        return new UpdateDraftCommand(id, request.getVersion(), request.getTitle(), request.getSubTitle(),
                request.getContent(), request.getMenuId(), request.getThumbnailUrl(),
                request.getThumbnailLogicalFileName(), author);
    }

    public DraftResponse toResponse(DraftResult result) {
        return DraftResponse.builder()
                .id(result.id()).version(result.version()).title(result.title()).subTitle(result.subTitle())
                .content(result.content()).author(result.author()).menuId(result.menuId())
                .thumbnailUrl(result.thumbnailUrl())
                .thumbnailLogicalFileName(result.thumbnailLogicalFileName())
                .build();
    }
}
