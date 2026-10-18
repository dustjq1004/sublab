package me.kimyeonsup.home.domain.article.adaptor.in.web.blog;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.article.adaptor.in.web.blog.mapper.ArticleWebMapper;
import me.kimyeonsup.home.domain.article.adaptor.in.web.blog.request.AddArticleRequest;
import me.kimyeonsup.home.domain.article.adaptor.in.web.blog.request.UpdateArticleRequest;
import me.kimyeonsup.home.domain.article.adaptor.in.web.blog.response.ArticlesPaginationResponse;
import me.kimyeonsup.home.domain.article.application.port.in.ArticleCommandUseCase;
import me.kimyeonsup.home.domain.article.application.port.in.ArticleQueryUseCase;
import me.kimyeonsup.home.domain.article.application.port.in.command.ArticleSearchQuery;
import me.kimyeonsup.home.domain.article.application.port.in.command.UpdateArticleCommand;
import me.kimyeonsup.home.domain.article.application.port.in.result.ArticleResult;
import me.kimyeonsup.home.domain.article.application.port.in.result.ArticleSummaryResult;
import me.kimyeonsup.home.domain.article.adaptor.in.web.blog.response.ArticleListViewResponse;
import me.kimyeonsup.home.domain.article.adaptor.in.web.blog.response.ArticleResponse;
import me.kimyeonsup.home.domain.article.adaptor.in.web.blog.response.LatestArticles;
import me.kimyeonsup.home.domain.article.adaptor.in.web.blog.request.SearchParams;
import me.kimyeonsup.home.domain.article.adaptor.in.web.blog.response.SearchedArticles;
import me.kimyeonsup.home.global.api.domain.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.repository.query.Param;
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

    private final ArticleQueryUseCase articleQueryUseCase;
    private final ArticleWebMapper articleWebMapper;
    private final ArticleCommandUseCase articleCommandUseCase;

    @GetMapping("/api/articles")
    public ApiResponse<ArticlesPaginationResponse> findArticles(
            @RequestParam(required = false, defaultValue = "0") int pageNumber,
            @RequestParam(required = false, defaultValue = "12") int pageSize,
            @RequestParam(required = false, defaultValue = "createdAt") String sortedBy,
            @RequestParam(required = false, defaultValue = "DESC") String direction,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String menuName,
            @RequestParam(required = false, defaultValue = "") String searchKeyword) {

        Page<ArticleSummaryResult> articles = articleQueryUseCase.findAllPagination(ArticleSearchQuery.builder()
                .categoryId(categoryId)
                .menuName(menuName)
                .searchKeyword(searchKeyword)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .sortedBy(sortedBy)
                .direction(Direction.fromString(direction))
                .build());

        return ApiResponse.success(articleWebMapper.toArticlesPaginationResponse(articles));
    }

    @GetMapping("/api/articles/{id}")
    public ApiResponse<ArticleResponse> findArticle(@PathVariable long id) {
        ArticleResult article = articleQueryUseCase.findByIdCredentialCheck(id);
        return ApiResponse.success(articleWebMapper.toArticleResponse(article));
    }

    @GetMapping("/api/articles/{id}/markdown")
    public ApiResponse<ArticleResponse> getArticleMarkdownContent(@PathVariable long id) {
        ArticleResult article = articleQueryUseCase.findByIdCredentialCheck(id);
        return ApiResponse.success(articleWebMapper.toArticleResponse(article));
    }

    @PostMapping("/api/articles/create")
    public ApiResponse<ArticleResponse> create(@Validated @RequestBody AddArticleRequest request,
                                                   @AuthenticationPrincipal PrincipalDetail user) {
        //Article savedArticle = articleService.save(request, user.getName());
        ArticleResult articleResult = articleCommandUseCase.create(
                articleWebMapper.toCreateArticleCommand(request, user.getName()));

        return ApiResponse.success(articleWebMapper.toArticleResponse(articleResult));
    }

    @DeleteMapping("/api/articles/delete")
    public ApiResponse<Void> delete(@Param("id") long id, @AuthenticationPrincipal PrincipalDetail user) {
        articleCommandUseCase.delete(id, user.getName());
        return ApiResponse.success(null);
    }

    @PutMapping("/api/articles/update")
    public ApiResponse<ArticleResponse> update(@Validated @RequestBody UpdateArticleRequest request,
                                                    @AuthenticationPrincipal PrincipalDetail user) {
        UpdateArticleCommand command = articleWebMapper.toUpdateArticleCommand(request, user.getName());
        ArticleResult updatedArticle = articleCommandUseCase.update(command);

        return ApiResponse.success(articleWebMapper.toArticleResponse(updatedArticle));
    }

    @GetMapping("/api/articles/latest")
    public ApiResponse<LatestArticles> getLatestArticles() {
        List<ArticleSummaryResult> latestArticles = articleQueryUseCase.getLatestArticles();
        return ApiResponse.success(new LatestArticles(latestArticles
                .stream().map(ArticleListViewResponse::new).toList()));
    }

    @GetMapping("/api/articles/search")
    public ApiResponse<SearchedArticles> getSearchedArticles(@RequestParam("titleParam") SearchParams searchParams) {
        String titleParam = searchParams.getTitleParam();
        if (titleParam.isEmpty()) {
            return ApiResponse.success(new SearchedArticles(Collections.emptyList()));
        }
        List<ArticleSummaryResult> articlesByTitle = articleQueryUseCase.getArticlesByTitle(titleParam);
        return ApiResponse.success(new SearchedArticles(articlesByTitle
                .stream().map(ArticleListViewResponse::new).toList()));
    }
}
