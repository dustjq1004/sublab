package me.kimyeonsup.home.domain.article.application.port.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import me.kimyeonsup.home.config.oauth.AdminProperties;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.article.application.port.service.ArticleService;
import me.kimyeonsup.home.domain.article.application.port.out.ArticleQueryRepository;
import me.kimyeonsup.home.domain.article.application.port.out.ArticleCommandRepository;
import me.kimyeonsup.home.domain.article.application.port.mapper.ArticleCommandMapper;
import me.kimyeonsup.home.domain.article.application.port.in.command.*;
import me.kimyeonsup.home.domain.article.domain.model.*;
import me.kimyeonsup.home.domain.draft.application.port.in.DraftCommandUseCase;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.menu.repository.MenuRepository;
import me.kimyeonsup.home.domain.login.domain.entity.User;
import me.kimyeonsup.home.global.exception.business.ReadAccessDeniedException;
import me.kimyeonsup.home.global.exception.business.ResourceNotFoundException;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
    @Mock ArticleQueryRepository articleQueryRepository;
    @Mock ArticleCommandRepository articleCommandRepository;
    @Mock DraftCommandUseCase draftCommandUseCase;
    @Mock MenuRepository menuRepository;
    @Mock MessageSourceResolver messageSourceResolver;
    @Mock AdminProperties adminProperties;
    @Spy ArticleCommandMapper articleCommandMapper = new ArticleCommandMapper();
    @InjectMocks ArticleService articleService;

    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    @Test
    @DisplayName("공개 게시글은 익명으로 상세 조회할 수 있다")
    void publicArticle() {
        when(articleQueryRepository.findById(1L)).thenReturn(Optional.of(article("PUBLIC")));
        assertThat(articleService.findByIdCredentialCheck(1L).title()).isEqualTo("title");
    }

    @Test
    @DisplayName("비공개 게시글은 작성자만 상세 조회한다")
    void privateArticleAuthor() {
        authenticate("author");
        when(articleQueryRepository.findById(1L)).thenReturn(Optional.of(article("PRIVATE")));
        assertThat(articleService.findByIdCredentialCheck(1L).visibility()).isEqualTo(Visibility.PRIVATE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"anonymous", "other", "string", "unauthenticated"})
    @DisplayName("익명·타인·잘못된 principal은 비공개 게시글을 조회할 수 없다")
    void denyPrivateArticle(String kind) {
        if (kind.equals("other")) authenticate("other");
        if (kind.equals("string")) SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("author", "pwd", List.of()));
        if (kind.equals("unauthenticated")) SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal("author"), "pwd"));
        when(articleQueryRepository.findById(1L)).thenReturn(Optional.of(article("PRIVATE")));
        assertThatThrownBy(() -> articleService.findByIdCredentialCheck(1L))
                .isInstanceOf(ReadAccessDeniedException.class);
    }

    @Test
    void missingArticle() {
        when(articleQueryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> articleService.findByIdCredentialCheck(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("게시글 저장 성공 후에만 연결된 임시저장을 삭제한다")
    void create(boolean withDraft) {
        CreateArticleCommand command = CreateArticleCommand.builder().title("new").content("body")
                .author("author").menuId(2L).visibility("PUBLIC").thumbnailUrl("")
                .thumbnailLogicalFileName("").drateId(withDraft ? 9L : null).build();
        when(articleCommandRepository.save(any())).thenAnswer(invocation -> Optional.of(invocation.getArgument(0)));
        assertThat(articleService.create(command).title()).isEqualTo("new");
        ArgumentCaptor<Article> saved = ArgumentCaptor.forClass(Article.class);
        verify(articleCommandRepository).save(saved.capture());
        assertThat(saved.getValue().getAuthor()).isEqualTo("author");
        assertThat(saved.getValue().getMenu().getId()).isEqualTo(2L);
        if (withDraft) {
            InOrder order = inOrder(articleCommandRepository, draftCommandUseCase);
            order.verify(articleCommandRepository).save(any());
            order.verify(draftCommandUseCase).discard(9L);
        } else verifyNoInteractions(draftCommandUseCase);
    }

    @Test
    void failedSaveDoesNotDeleteDraft() {
        when(articleCommandRepository.save(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> articleService.create(CreateArticleCommand.builder()
                .title("new").menuId(2L).visibility("PUBLIC").drateId(9L).build()))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(draftCommandUseCase);
    }

    @Test
    @DisplayName("작성자 조건으로 조회한 게시글의 내용·메뉴·공개 범위를 변경한다")
    void update() {
        Article article = article("PUBLIC");
        Menu target = Menu.builder().id(3L).name("target").build();
        when(articleQueryRepository.findByIdAndAuthor(1L, "author")).thenReturn(Optional.of(article));
        when(menuRepository.findById(3L)).thenReturn(Optional.of(target));
        var result = articleService.update(updateCommand());
        assertThat(result.title()).isEqualTo("changed");
        assertThat(result.visibility()).isEqualTo(Visibility.PRIVATE);
        assertThat(article.getContent()).isEqualTo("changed body");
        assertThat(article.getMenu()).isSameAs(target);
        assertThat(article.getThumbnailLogicalFileName()).isEqualTo("thumb.png");
        verifyNoInteractions(articleCommandRepository);
    }

    @Test
    void updateRejectsMissingArticleOrDifferentAuthor() {
        when(articleQueryRepository.findByIdAndAuthor(1L, "author")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> articleService.update(updateCommand())).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(menuRepository, articleCommandRepository);
    }

    @Test
    void updateRejectsMissingMenuWithoutChangingArticle() {
        Article article = article("PUBLIC");
        when(articleQueryRepository.findByIdAndAuthor(1L, "author")).thenReturn(Optional.of(article));
        when(menuRepository.findById(3L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> articleService.update(updateCommand())).isInstanceOf(ResourceNotFoundException.class);
        assertThat(article.getTitle()).isEqualTo("title");
    }

    @Test
    void deleteByAuthor() {
        when(articleQueryRepository.findByIdAndAuthor(1L, "author")).thenReturn(Optional.of(article("PUBLIC")));
        articleService.delete(1L, "author");
        verify(articleCommandRepository).deleteBy(1L);
    }

    @Test
    void deleteRejectsMissingArticleOrDifferentAuthor() {
        when(articleQueryRepository.findByIdAndAuthor(1L, "other")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> articleService.delete(1L, "other")).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(articleCommandRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"menu", "category", "all"})
    @DisplayName("목록 조회는 메뉴 이름, 카테고리, 전체 순서로 조건을 선택한다")
    void pagination(String filter) {
        ArticleSearchQuery query = ArticleSearchQuery.builder().menuName(filter.equals("menu") ? "java" : null)
                .categoryId(filter.equals("all") ? null : 2L).searchKeyword("title").build();
        var page = new PageImpl<>(List.of(article("PUBLIC")));
        switch (filter) {
            case "menu" -> when(articleQueryRepository.findByMenuNameAndTitleContainingAndVisibility(
                    query, Visibility.PUBLIC)).thenReturn(page);
            case "category" -> when(articleQueryRepository.findByCategoryIdAndTitleContainingAndVisibility(
                    query, Visibility.PUBLIC)).thenReturn(page);
            default -> when(articleQueryRepository.findByTitleContainingAndVisibility(
                    query, Visibility.PUBLIC)).thenReturn(page);
        }
        assertThat(articleService.findAllPagination(query).getContent()).singleElement()
                .satisfies(result -> assertThat(result.title()).isEqualTo("title"));
        switch (filter) {
            case "menu" -> verify(articleQueryRepository).findByMenuNameAndTitleContainingAndVisibility(
                    query, Visibility.PUBLIC);
            case "category" -> verify(articleQueryRepository).findByCategoryIdAndTitleContainingAndVisibility(
                    query, Visibility.PUBLIC);
            default -> verify(articleQueryRepository).findByTitleContainingAndVisibility(query, Visibility.PUBLIC);
        }
        verifyNoMoreInteractions(articleQueryRepository);
    }

    private UpdateArticleCommand updateCommand() {
        return UpdateArticleCommand.builder().id(1L).author("author").title("changed")
                .content("changed body").menuId(3L).visibility("PRIVATE")
                .thumbnailUrl("https://example.com/thumb.png").thumbnailLogicalFileName("thumb.png").build();
    }

    private Article article(String visibility) {
        Article article = Article.builder().title("title").content("body").author("author")
                .menu(Menu.builder().id(2L).name("java").build()).visibility(visibility).build();
        ReflectionTestUtils.setField(article, "id", 1L);
        return article;
    }

    private PrincipalDetail principal(String name) {
        return new PrincipalDetail(User.builder().email(name + "@example.com").nickname(name).role("USER").build());
    }

    private void authenticate(String name) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal(name), "pwd", List.of()));
    }
}
