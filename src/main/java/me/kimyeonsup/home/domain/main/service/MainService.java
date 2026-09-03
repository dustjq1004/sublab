package me.kimyeonsup.home.domain.main.service;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.config.oauth.AdminProperties;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.blog.article.domain.dto.ArticleListViewResponse;
import me.kimyeonsup.home.domain.blog.article.domain.vo.Visibility;
import me.kimyeonsup.home.domain.blog.article.repository.ArticleRepository;
import me.kimyeonsup.home.domain.main.domain.dto.LatestArticles;
import me.kimyeonsup.home.domain.main.domain.dto.SearchedArticles;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MainService {

    private final ArticleRepository articleRepository;
    private final AdminProperties adminProperties;

    public LatestArticles getLatestArticles() {

        return new LatestArticles((isAdmin()
                ? articleRepository.findTop8ByOrderByCreatedAtDesc()
                : articleRepository.findTop8ByVisibilityOrderByCreatedAtDesc(Visibility.PUBLIC))
                .stream()
                .map(ArticleListViewResponse::new)
                .toList());
    }

    public SearchedArticles getArticlesByTitle(@NotBlank String titleParam) {
        return new SearchedArticles((isAdmin()
                ? articleRepository.findByTitleContains(titleParam)
                : articleRepository.findByTitleContainsAndVisibility(titleParam, Visibility.PUBLIC))
                .stream()
                .map(ArticleListViewResponse::new)
                .toList());
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof PrincipalDetail principalDetail) {
            return adminProperties.getEmail().equals(principalDetail.getEmail());
        }
        return false;
    }
}
