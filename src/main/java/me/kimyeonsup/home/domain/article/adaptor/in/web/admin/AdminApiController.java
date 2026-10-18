package me.kimyeonsup.home.domain.article.adaptor.in.web.admin;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.article.adaptor.in.web.admin.request.ArticleBatchDeleteRequest;
import me.kimyeonsup.home.domain.article.adaptor.in.web.admin.request.ArticleBatchUpdateRequest;
import me.kimyeonsup.home.domain.article.adaptor.in.web.admin.request.ArticlePagingRequest;
import me.kimyeonsup.home.domain.article.adaptor.in.web.admin.request.MenuBatchUpdateRequest;
import me.kimyeonsup.home.domain.article.adaptor.in.web.admin.response.AdminArticlesPaginationResponse;
import me.kimyeonsup.home.domain.article.adaptor.in.web.admin.response.AdminArticlesResponse;
import me.kimyeonsup.home.domain.article.adaptor.in.web.admin.response.ArticleBatchDeleteResponse;
import me.kimyeonsup.home.domain.article.adaptor.in.web.admin.response.ArticleBatchUpdateResponse;
import me.kimyeonsup.home.domain.article.adaptor.in.web.admin.response.MenuBatchUpdateResponse;
import me.kimyeonsup.home.domain.article.adaptor.in.web.blog.mapper.ArticleWebMapper;
import me.kimyeonsup.home.domain.article.application.port.in.AdminArticleUseCase;
import me.kimyeonsup.home.domain.article.application.port.in.command.ArticleSearchQuery;
import me.kimyeonsup.home.domain.article.application.port.in.result.ArticleResult;
import me.kimyeonsup.home.global.api.domain.dto.ApiResponse;
import me.kimyeonsup.home.global.exception.business.UnexpectedResultSizeException;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminApiController {
    private final AdminArticleUseCase adminArticleUseCase;
    private final ArticleWebMapper articleWebMapper;

    @GetMapping("/articles")
    public ApiResponse<AdminArticlesPaginationResponse<AdminArticlesResponse>> findArticles(
            @Validated @ModelAttribute ArticlePagingRequest request) {

        ArticleSearchQuery searchQuery = articleWebMapper.toArticleSearchQuery(request);
        Page<ArticleResult> articles = adminArticleUseCase.findArticlesBy(searchQuery);

        AdminArticlesPaginationResponse<AdminArticlesResponse> articlesDto = new AdminArticlesPaginationResponse<>(
                articles.getNumber() + 1,
                articles.getSize(),
                articles.getNumberOfElements(),
                articles.isFirst(),
                articles.isLast(),
                articles.getTotalPages(),
                articles.getTotalElements(),
                articles.stream().map(AdminArticlesResponse::new).toList());

        return ApiResponse.success(articlesDto);
    }

    /**
     * 게시글 일괄 삭제
     *
     * @param params 삭제할 게시글 ID 목록
     * @return ResponseEntity<ArticleBatchDeleteResponse>
     */
    @DeleteMapping("/articles/batch")
    public ApiResponse<ArticleBatchDeleteResponse> deleteArticlesByIds(
            @Validated @RequestBody ArticleBatchDeleteRequest params) {
        int deletedCount = adminArticleUseCase.deleteArticlesByIds(List.of(params.getArticleIds()));
        return ApiResponse.success(ArticleBatchDeleteResponse.builder()
                .deletedCount(deletedCount)
                .build());
    }

    /**
     * 메뉴 일괄 변경
     *
     * @param params 변경할 게시글 ID 목록과 대상 메뉴 ID
     * @throws UnexpectedResultSizeException if articleIds is null or empty
     * @Returns MenuBatchUpdateResponse
     */
    @PutMapping("/articles/batch/menu")
    public ApiResponse<MenuBatchUpdateResponse> changeArticlesMenuByIds(
            @Validated @RequestBody MenuBatchUpdateRequest params) {
        int updatedCount = adminArticleUseCase.updateArticlesMenuByIds(params.getArticleIds(), params.getTargetMenuId());
        return ApiResponse.success(MenuBatchUpdateResponse.builder()
                .updatedCount(updatedCount)
                .build());
    }

    /**
     * visibility 일괄 변경
     *
     * @param params 변경할 게시글 ID 목록과 대상 visibility 값
     * @throws UnexpectedResultSizeException if articleIds is null or empty
     * @Returns int
     */
    @PutMapping("/"
            + "articles/batch/visibility")
    public ApiResponse<ArticleBatchUpdateResponse> changeArticlesVisibilityByIds(
            @Validated @RequestBody ArticleBatchUpdateRequest params,
            @AuthenticationPrincipal PrincipalDetail user) {
        int updatedCount = adminArticleUseCase.updateArticlesVisibilityByIds(params.getArticleIds(), params.getVisibility(), user.getName());
        return ApiResponse.success(ArticleBatchUpdateResponse.builder()
                .updatedCount(updatedCount)
                .build());
    }
}
