package me.kimyeonsup.home.domain.blog.article.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.blog.admin.menu.repository.MenuRepository;
import me.kimyeonsup.home.domain.blog.article.domain.dto.UpdateArticleRequest;
import me.kimyeonsup.home.domain.blog.article.domain.entity.Article;
import me.kimyeonsup.home.domain.blog.article.domain.vo.Visibility;
import me.kimyeonsup.home.domain.blog.article.repository.ArticleRepository;
import me.kimyeonsup.home.global.exception.business.ReadAccessDeniedException;
import me.kimyeonsup.home.global.exception.business.ResourceNotFoundException;
import me.kimyeonsup.home.global.exception.business.UpdateAccessDeniedException;
import me.kimyeonsup.home.global.exception.business.CredentialsMismatchException;
import me.kimyeonsup.home.global.exception.message.BaseExceptionMessage;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import me.kimyeonsup.home.login.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ArticleVisibilityServiceTest {

    @Mock ArticleRepository articleRepository;
    @Mock MenuRepository menuRepository;
    @Mock MessageSourceResolver messageSourceResolver;

    @InjectMocks ArticleService articleService;

    private static final String AUTHOR_NAME = "author";
    private static final String OTHER_NAME = "other";
    private static final String ORDER_CRITERIA = "createdAt";
    private static final long ARTICLE_ID = 1L;
    private static final int PAGE_NUMBER = 1;
    private static final int PAGE_SIZE = 12;
    Category category;
    Menu menu;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        category = createCategory("cat1");
        menu = createMenu("menu1", category);
    }

    @Nested
    @DisplayName("findById - visibility 검증")
    class FindById {

        @Test
        @DisplayName("given_public_article_when_any_user_then_returns_article")
        void given_public_article_when_any_user_then_returns_article() {
            // 검증: PUBLIC 글은 조회자와 무관하게 반환
            Article article = createArticle(Visibility.PUBLIC, AUTHOR_NAME);
            when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(article));

            setSecurityContext(OTHER_NAME);

            Article result = articleService.findByIdCredentialCheck(ARTICLE_ID);

            assertThat(result).isNotNull();
            assertThat(result.getVisibility()).isEqualTo(Visibility.PUBLIC);
        }

        @Test
        @DisplayName("given_private_article_when_author_then_returns_article")
        void given_private_article_when_author_then_returns_article() {
            // 검증: PRIVATE 글은 작성자만 조회 가능
            Article article = createArticle(Visibility.PRIVATE, AUTHOR_NAME);
            when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(article));

            setSecurityContext(AUTHOR_NAME);

            Article result = articleService.findByIdCredentialCheck(ARTICLE_ID);

            assertThat(result).isNotNull();
            assertThat(result.getVisibility()).isEqualTo(Visibility.PRIVATE);
        }

        @Test
        @DisplayName("given_private_article_when_other_user_then_throws_credentials_mismatch")
        void given_private_article_when_other_user_then_throws() {
            // 검증: PRIVATE 글에 다른 사용자 접근 시 CredentialsMismatchException
            Article article = createArticle(Visibility.PRIVATE, AUTHOR_NAME);
            when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(article));

            setSecurityContext(OTHER_NAME);

            assertThatThrownBy(() -> articleService.findByIdCredentialCheck(ARTICLE_ID))
                    .isInstanceOf(ReadAccessDeniedException.class)
                    .hasMessage("읽기 권한이 없습니다.");
        }

        @Test
        @DisplayName("given_non_existent_article_when_find_then_throws_resource_not_found")
        void given_non_existent_article_when_find_then_throws() {
            // 검증: 존재하지 않는 글 조회 시 ResourceNotFoundException
            when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.empty());
            when(messageSourceResolver.resolve(any(BaseExceptionMessage.class), any(Long.class))).thenReturn("글을 찾을 수 없습니다.");

            assertThatThrownBy(() -> articleService.findByIdCredentialCheck(ARTICLE_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("update - visibility 전환")
    class UpdateVisibility {

        @Test
        @DisplayName("given_public_article_when_author_changes_to_private_then_visibility_updated")
        void given_public_when_change_to_private_then_updated() {
            // 검증: 작성자가 PUBLIC → PRIVATE 전환 시 즉시 반영
            Article article = createArticle(Visibility.PUBLIC, AUTHOR_NAME);
            UpdateArticleRequest request = new UpdateArticleRequest("t", "", "c", 1L, null, null, "PRIVATE");
            when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(article));
            when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));

            setSecurityContext(AUTHOR_NAME);

            Article result = articleService.update(ARTICLE_ID, request);

            assertThat(result.getVisibility()).isEqualTo(Visibility.PRIVATE);
        }

        @Test
        @DisplayName("given_private_article_when_author_changes_to_public_then_visibility_updated")
        void given_private_when_change_to_public_then_updated() {
            // 검증: 작성자가 PRIVATE → PUBLIC 전환 시 즉시 반영
            Article article = createArticle(Visibility.PRIVATE, AUTHOR_NAME);
            UpdateArticleRequest request = new UpdateArticleRequest("t", "", "c", 1L, null, null, "PUBLIC");
            when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(article));
            when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));

            setSecurityContext(AUTHOR_NAME);

            Article result = articleService.update(ARTICLE_ID, request);

            assertThat(result.getVisibility()).isEqualTo(Visibility.PUBLIC);
        }

        @Test
        @DisplayName("given_article_when_other_user_updates_then_throws_update_access_denied")
        void given_article_when_other_user_updates_then_throws() {
            // 검증: 다른 사용자 수정 시 CredentialsMismatchException
            Article article = createArticle(Visibility.PRIVATE, AUTHOR_NAME);
            UpdateArticleRequest request = new UpdateArticleRequest("t", "", "c", 1L, null, null, "PUBLIC");
            when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(article));
            when(messageSourceResolver.resolve(any(BaseExceptionMessage.class), eq(ARTICLE_ID)))
                    .thenReturn("작성자만 수정 가능합니다.");

            setSecurityContext(OTHER_NAME);

            assertThatThrownBy(() -> articleService.update(ARTICLE_ID, request))
                    .isInstanceOf(UpdateAccessDeniedException.class)
                    .hasMessage("작성자만 수정 가능합니다.");
        }
    }

    @Nested
    @DisplayName("delete - 권한 검증")
    class DeleteArticle {

        @Test
        @DisplayName("작성자는 게시글을 삭제할 수 있다")
        void given_article_when_author_deletes_then_delete_called() {
            Article article = createArticle(Visibility.PRIVATE, AUTHOR_NAME);
            when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(article));
            doNothing().when(articleRepository).delete(article);

            setSecurityContext(AUTHOR_NAME);

            articleService.delete(ARTICLE_ID);

            verify(articleRepository).delete(article);
        }

        @Test
        @DisplayName("다른 사용자는 게시글을 삭제할 수 없다")
        void given_article_when_other_user_deletes_then_throws() {
            Article article = createArticle(Visibility.PRIVATE, AUTHOR_NAME);
            when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(article));
            when(messageSourceResolver.resolve(any(BaseExceptionMessage.class), eq(ARTICLE_ID)))
                    .thenReturn("작성자만 삭제 가능합니다.");

            setSecurityContext(OTHER_NAME);

            assertThatThrownBy(() -> articleService.delete(ARTICLE_ID))
                    .isInstanceOf(CredentialsMismatchException.class)
                    .hasMessage("작성자만 삭제 가능합니다.");

            verify(articleRepository, never()).delete(any());
        }

        @Test
        @DisplayName("비로그인 사용자는 게시글을 삭제할 수 없다")
        void given_article_when_anonymous_deletes_then_throws() {
            Article article = createArticle(Visibility.PRIVATE, AUTHOR_NAME);
            when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(article));
            when(messageSourceResolver.resolve(any(BaseExceptionMessage.class), eq(ARTICLE_ID)))
                    .thenReturn("작성자만 삭제 가능합니다.");

            SecurityContextHolder.clearContext();

            assertThatThrownBy(() -> articleService.delete(ARTICLE_ID))
                    .isInstanceOf(CredentialsMismatchException.class)
                    .hasMessage("작성자만 삭제 가능합니다.");

            verify(articleRepository, never()).delete(any());
        }

        @Test
        @DisplayName("존재하지 않는 게시글 삭제는 예외를 던진다")
        void given_non_existent_article_when_delete_then_throws() {
            when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.empty());
            when(messageSourceResolver.resolve(any(BaseExceptionMessage.class), eq(ARTICLE_ID)))
                    .thenReturn("글을 찾을 수 없습니다.");

            assertThatThrownBy(() -> articleService.delete(ARTICLE_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("글을 찾을 수 없습니다.");

            verify(articleRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("findAllPagination - 목록 조회")
    class FindAllPagination {

        @Test
        @DisplayName("given_public_and_private_articles_when_find_all_then_returns_both")
        void given_mixed_visibility_when_find_all_then_returns_both() {
            // 검증: 공개 목록 API는 PUBLIC/PRIVATE 모두 반환 (현재 구현 기준)
            // Repository 쿼리가 visibility를 필터링하지 않으면 둘 다 포함
            // 필요 시 Page<Article> 반환 검증
            // 검증: PUBLIC 글은 조회자와 무관하게 반환
            Article private1 = createArticle(Visibility.PRIVATE, AUTHOR_NAME);
            Article public1 = createArticle(Visibility.PUBLIC, AUTHOR_NAME);
            Article public2 = createArticle(Visibility.PUBLIC, AUTHOR_NAME);
            Page<Article> mockPage = new PageImpl<>(List.of(private1, public1, public2), PageRequest.of(1, 12), 0);
            when(articleRepository.findByTitleContaining(eq(""), any(Pageable.class)))
                    .thenReturn(mockPage);

            setSecurityContext(OTHER_NAME);

            Page<Article> result = articleService.findAllPagination(PAGE_NUMBER, null, null, "");

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(articleRepository).findByTitleContaining(eq(""), pageableCaptor.capture());

            Pageable captured = pageableCaptor.getValue();
            assertThat(captured.getPageNumber()).isEqualTo(1);
            assertThat(captured.getPageSize()).isEqualTo(12);
            assertThat(captured.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent().stream()
                    .filter(article -> article.getVisibility() == Visibility.PRIVATE)
                    .toList()).hasSize(1);
        }
    }

    private Article createArticle(Visibility visibility, String author) {
        return Article.builder()
                .title("title")
                .content("content")
                .author(author)
                .visibility(visibility.name())
                //.menu(menu)
                .build();
    }

    private Category createCategory(String name) {
        return Category.builder().name(name).build();
    }

    private Menu createMenu(String name, Category category) {
        return Menu.builder().name(name).category(category).build();
    }

    private void setSecurityContext(String userName) {
        User user = User.builder().email(userName + "@test.com").password("pwd").role("USER").build();
        PrincipalDetail principal = new PrincipalDetail(user);
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken(
                        principal, principal.getAuthorities(), "GM"));
    }
}
