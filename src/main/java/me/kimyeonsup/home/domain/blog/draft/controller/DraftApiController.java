package me.kimyeonsup.home.domain.blog.draft.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.blog.draft.domain.dto.AddDraftArticle;
import me.kimyeonsup.home.domain.blog.draft.domain.dto.DraftArticlesResponse;
import me.kimyeonsup.home.domain.blog.draft.domain.dto.UpdateDraftArticle;
import me.kimyeonsup.home.domain.blog.draft.domain.entity.DraftArticle;
import me.kimyeonsup.home.domain.blog.draft.service.DraftService;
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

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class DraftApiController {

    private final DraftService draftService;

    @GetMapping("/draft")
    public ApiResponse<List<DraftArticlesResponse>> findByAuthorAnDelete(@AuthenticationPrincipal PrincipalDetail principalDetail) {
        List<DraftArticlesResponse> draftArticles = draftService
                .findByAuthorAndDelete(principalDetail.getName())
                .stream()
                .map(DraftArticlesResponse::new)
                .toList();
        return ApiResponse.success(draftArticles);
    }

    @PostMapping("/draft")
    public ApiResponse<DraftArticlesResponse> saveOrUpdate(@Validated @RequestBody AddDraftArticle addDraftArticle, @AuthenticationPrincipal PrincipalDetail principalDetail) {
        DraftArticle savedDraftArticle = draftService.save(addDraftArticle, principalDetail.getName());
        return ApiResponse.success(new DraftArticlesResponse(savedDraftArticle));
    }

    @PutMapping("/draft")
    public ApiResponse<DraftArticlesResponse> update(@Validated @RequestBody UpdateDraftArticle draftArticle, @AuthenticationPrincipal PrincipalDetail principalDetail) {
        DraftArticle savedDraftArticle = draftService.update(draftArticle);
        return ApiResponse.success(new DraftArticlesResponse(savedDraftArticle));
    }

    @DeleteMapping("/draft/{id}")
    public ApiResponse<Void> deleteDraftArticle(@PathVariable Long id) {
        draftService.delete(id);
        return ApiResponse.success(null);
    }
}
