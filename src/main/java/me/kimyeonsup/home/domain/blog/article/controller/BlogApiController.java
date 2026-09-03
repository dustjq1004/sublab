package me.kimyeonsup.home.domain.blog.article.controller;

import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.blog.article.assembler.ArticleDtoAssembler;
import me.kimyeonsup.home.domain.blog.article.domain.dto.AddArticleRequest;
import me.kimyeonsup.home.domain.blog.article.domain.dto.ArticleResponse;
import me.kimyeonsup.home.domain.blog.article.domain.dto.ArticlesPaginationResponse;
import me.kimyeonsup.home.domain.blog.article.domain.dto.UpdateArticleRequest;
import me.kimyeonsup.home.domain.blog.article.domain.entity.Article;
import me.kimyeonsup.home.domain.blog.article.service.ArticleService;
import me.kimyeonsup.home.domain.blog.draft.service.DraftService;
import me.kimyeonsup.home.global.api.domain.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class BlogApiController {

    private final ArticleService articleService;
    private final DraftService draftService;
    private final ArticleDtoAssembler articleDtoAssembler;

    @GetMapping("/api/articles")
    public ApiResponse<ArticlesPaginationResponse> findArticles(
            @RequestParam(required = false, defaultValue = "0") int pageNumber,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String menuName,
            @RequestParam(required = false, defaultValue = "") String searchKeyword) {

        Page<Article> articles = articleService.findAllPagination(pageNumber, categoryId, menuName, searchKeyword);

        return ApiResponse.success(ArticlesPaginationResponse.of(articles));
    }

    @GetMapping("/api/articles/{id}")
    public ApiResponse<ArticleResponse> findArticle(@PathVariable long id) {
        Article article = articleService.findByIdCredentialCheck(id);
        return ApiResponse.success(articleDtoAssembler.toResponse(article));
    }

    @PostMapping("/api/articles")
    public ApiResponse<ArticleResponse> addArticle(@Validated @RequestBody AddArticleRequest request,
                                                   @AuthenticationPrincipal PrincipalDetail user) {
        Article savedArticle = articleService.save(request, user.getName());
        Long draftId = request.getDraftId();
        if (draftId != null) {
            draftService.deleteIfExistsForArticlePublish(draftId);
        }
        return ApiResponse.success(articleDtoAssembler.toResponse(savedArticle));
    }

    @DeleteMapping("/api/articles/{id}")
    public ApiResponse<Void> deleteArticle(@PathVariable long id) {
        articleService.delete(id);

        return ApiResponse.success(null);
    }

    @PutMapping("/api/articles/{id}")
    public ApiResponse<ArticleResponse> updateArticle(@PathVariable long id,
                                                         @Validated @RequestBody UpdateArticleRequest request) {
        Article updatedArticle = articleService.update(id, request);

        return ApiResponse.success(articleDtoAssembler.toResponse(updatedArticle));
    }
}
