package me.kimyeonsup.home.domain.draft.adaptor.in.web;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.draft.adaptor.in.web.mapper.DraftWebMapper;
import me.kimyeonsup.home.domain.draft.adaptor.in.web.request.CreateDraftRequest;
import me.kimyeonsup.home.domain.draft.adaptor.in.web.request.UpdateDraftRequest;
import me.kimyeonsup.home.domain.draft.adaptor.in.web.response.DraftResponse;
import me.kimyeonsup.home.domain.draft.application.port.in.DraftCommandUseCase;
import me.kimyeonsup.home.domain.draft.application.port.in.DraftQueryUseCase;
import me.kimyeonsup.home.global.api.domain.dto.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DraftApiController {

    private final DraftCommandUseCase draftCommandUseCase;
    private final DraftQueryUseCase draftQueryUseCase;
    private final DraftWebMapper draftWebMapper;

    @GetMapping("/drafts")
    public ApiResponse<List<DraftResponse>> findAll(@AuthenticationPrincipal PrincipalDetail principal) {
        return ApiResponse.success(draftQueryUseCase.findAllByAuthor(principal.getName()).stream()
                .map(draftWebMapper::toResponse).toList());
    }

    @PostMapping("/draft/create")
    public ApiResponse<DraftResponse> create(@Validated @RequestBody CreateDraftRequest request,
            @AuthenticationPrincipal PrincipalDetail principal) {
        return ApiResponse.success(draftWebMapper.toResponse(
                draftCommandUseCase.create(draftWebMapper.toCreateCommand(request, principal.getName()))));
    }

    @PutMapping("/draft/update/{id}")
    public ApiResponse<DraftResponse> update(@PathVariable Long id,
            @Validated @RequestBody UpdateDraftRequest request,
            @AuthenticationPrincipal PrincipalDetail principal) {
        return ApiResponse.success(draftWebMapper.toResponse(
                draftCommandUseCase.update(draftWebMapper.toUpdateCommand(id, request, principal.getName()))));
    }

    @DeleteMapping("/draft/delete/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal PrincipalDetail principal) {
        draftCommandUseCase.delete(id, principal.getName());
        return ApiResponse.success(null);
    }
}
