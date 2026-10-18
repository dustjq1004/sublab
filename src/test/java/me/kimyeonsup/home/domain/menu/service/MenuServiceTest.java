package me.kimyeonsup.home.domain.menu.service;

import static me.kimyeonsup.home.global.exception.message.constants.CategoryExceptionMessage.CATEGORY_NOT_FOUND;
import static me.kimyeonsup.home.global.exception.message.constants.MenuExceptionMessage.MENU_DELETE_HAS_ARTICLE;
import static me.kimyeonsup.home.global.exception.message.constants.MenuExceptionMessage.MENU_DELETE_HAS_DRAFT;
import static me.kimyeonsup.home.global.exception.message.constants.MenuExceptionMessage.MENU_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import me.kimyeonsup.home.domain.article.domain.model.Article;
import me.kimyeonsup.home.domain.draft.application.port.in.DraftQueryUseCase;
import me.kimyeonsup.home.domain.menu.domain.dto.AddMenuRequest;
import me.kimyeonsup.home.domain.menu.domain.dto.MenuResponse;
import me.kimyeonsup.home.domain.menu.domain.dto.UpdateMenuRequest;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.menu.repository.CategoryRepository;
import me.kimyeonsup.home.domain.menu.repository.MenuRepository;
import me.kimyeonsup.home.domain.menu.service.mapper.MenuMapper;
import me.kimyeonsup.home.global.exception.business.ResourceConflictException;
import me.kimyeonsup.home.global.exception.business.ResourceNotFoundException;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock MenuRepository menuRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock MessageSourceResolver messageSourceResolver;
    @Mock MenuMapper mapper;
    @Mock DraftQueryUseCase draftQueryUseCase;
    @InjectMocks MenuService menuService;

    @Test
    void saveCreatesMenuInExistingCategory() {
        // MS-CMD-001
        AddMenuRequest request = menuRequest(1L, "new", "new description", "N");
        Category category = category(1L, "category", List.of());
        Menu menu = menu(null, "new", "new description", "N", category, List.of());
        Menu savedMenu = menu(10L, "new", "new description", "N", category, List.of());
        MenuResponse expected = MenuResponse.builder().id(10L).build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(mapper.toMenu(request, category)).thenReturn(menu);
        when(menuRepository.save(menu)).thenReturn(savedMenu);
        when(mapper.toCreatedResponse(savedMenu)).thenReturn(expected);

        MenuResponse actual = menuService.save(request);

        assertThat(actual).isSameAs(expected);
        verify(categoryRepository).findById(request.categoryId());
        verify(mapper).toMenu(request, category);
        verify(menuRepository).save(menu);
        verify(mapper).toCreatedResponse(savedMenu);
    }

    @Test
    void saveRejectsMissingCategoryWithoutCreatingMenu() {
        // MS-CMD-002
        AddMenuRequest request = menuRequest(1L, "new", "description", "N");
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        when(messageSourceResolver.resolve(CATEGORY_NOT_FOUND, 1L)).thenReturn("category not found");

        assertThatThrownBy(() -> menuService.save(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("category not found");

        verify(categoryRepository).findById(request.categoryId());
        verify(messageSourceResolver).resolve(CATEGORY_NOT_FOUND, request.categoryId());
        verifyNoInteractions(menuRepository, mapper);
    }

    @Test
    void deleteRemovesMenuWithoutArticles() {
        // MS-CMD-003
        Category category = category(1L, "category", List.of());
        Menu menu = menu(10L, "menu", "description", "M", category, List.of());
        MenuResponse expected = MenuResponse.builder().id(10L).build();
        when(menuRepository.findById(10L)).thenReturn(Optional.of(menu));
        when(draftQueryUseCase.existsByMenuId(10L)).thenReturn(false);
        when(mapper.toDeletedResponse(menu)).thenReturn(expected);

        MenuResponse actual = menuService.delete(10L);

        assertThat(actual).isSameAs(expected);
        verify(menuRepository).findById(10L);
        verify(draftQueryUseCase).existsByMenuId(10L);
        verify(menuRepository).delete(menu);
        verify(mapper).toDeletedResponse(menu);
        verifyNoInteractions(messageSourceResolver);
    }

    @Test
    void deleteRejectsMissingMenuWithoutDeleting() {
        // MS-CMD-004
        when(menuRepository.findById(10L)).thenReturn(Optional.empty());
        when(messageSourceResolver.resolve(MENU_NOT_FOUND, 10L)).thenReturn("menu not found");

        ResourceNotFoundException exception = catchThrowableOfType(
                () -> menuService.delete(10L), ResourceNotFoundException.class);

        assertThat(exception).hasMessage("menu not found");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getCode()).isEqualTo("E404");

        verify(messageSourceResolver).resolve(MENU_NOT_FOUND, 10L);
        verify(menuRepository, never()).delete(any(Menu.class));
        verifyNoInteractions(draftQueryUseCase);
        verifyNoInteractions(mapper);
    }

    @Test
    void deleteRejectsMenuWithArticlesAndPreservesState() {
        // MS-CMD-005
        Category category = category(1L, "category", List.of());
        Menu menu = menu(10L, "menu", "description", "M", category, List.of(mock(Article.class)));
        when(menuRepository.findById(10L)).thenReturn(Optional.of(menu));
        when(messageSourceResolver.resolve(MENU_DELETE_HAS_ARTICLE)).thenReturn("menu has articles");

        ResourceConflictException exception = catchThrowableOfType(
                () -> menuService.delete(10L), ResourceConflictException.class);

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getCode()).isEqualTo("E409");
        assertThat(exception).hasMessage("menu has articles");

        assertThat(menu.getName()).isEqualTo("menu");
        assertThat(menu.getDescription()).isEqualTo("description");
        assertThat(menu.getEmoji()).isEqualTo("M");
        assertThat(menu.getCategory()).isSameAs(category);
        verify(messageSourceResolver).resolve(MENU_DELETE_HAS_ARTICLE);
        verify(menuRepository, never()).delete(any(Menu.class));
        verifyNoInteractions(draftQueryUseCase);
        verifyNoInteractions(mapper);
    }

    @Test
    void deleteRejectsMenuWithDraftAndPreservesState() {
        // DRAFT-SVC-001
        Category category = category(1L, "category", List.of());
        Menu menu = menu(10L, "menu", "description", "M", category, List.of());
        when(menuRepository.findById(10L)).thenReturn(Optional.of(menu));
        when(draftQueryUseCase.existsByMenuId(10L)).thenReturn(true);
        when(messageSourceResolver.resolve(MENU_DELETE_HAS_DRAFT)).thenReturn("menu has drafts");

        ResourceConflictException exception = catchThrowableOfType(
                () -> menuService.delete(10L), ResourceConflictException.class);

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getCode()).isEqualTo("E409");
        assertThat(exception).hasMessage("menu has drafts");
        assertThat(menu.getName()).isEqualTo("menu");
        assertThat(menu.getDescription()).isEqualTo("description");
        assertThat(menu.getEmoji()).isEqualTo("M");
        assertThat(menu.getCategory()).isSameAs(category);
        assertThat(menu.getArticles()).isEmpty();
        verify(draftQueryUseCase).existsByMenuId(10L);
        verify(messageSourceResolver).resolve(MENU_DELETE_HAS_DRAFT);
        verify(menuRepository, never()).delete(any(Menu.class));
        verify(mapper, never()).toDeletedResponse(any(Menu.class));
    }

    @Test
    void updateChangesMenuAndMovesItToTargetCategory() {
        // MS-CMD-006
        Category originalCategory = category(1L, "original", List.of());
        Category targetCategory = category(2L, "target", List.of());
        Menu menu = menu(10L, "old", "old description", "O", originalCategory, List.of());
        UpdateMenuRequest request = new UpdateMenuRequest(10L, 2L, "N", "new", "new description");
        MenuResponse expected = MenuResponse.builder().id(10L).build();
        when(menuRepository.findById(10L)).thenReturn(Optional.of(menu));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(targetCategory));
        when(mapper.toUpdatedResponse(menu)).thenReturn(expected);

        MenuResponse actual = menuService.update(request);

        assertThat(menu.getName()).isEqualTo("new");
        assertThat(menu.getDescription()).isEqualTo("new description");
        assertThat(menu.getEmoji()).isEqualTo("N");
        assertThat(menu.getCategory()).isSameAs(targetCategory);
        assertThat(actual).isSameAs(expected);
        verify(menuRepository).findById(request.getId());
        verify(categoryRepository).findById(request.getCategoryId());
        verify(mapper).toUpdatedResponse(menu);
        verify(menuRepository, never()).save(any(Menu.class));
    }

    @Test
    void updateRejectsMissingMenuBeforeLookingUpCategory() {
        // MS-CMD-007
        UpdateMenuRequest request = new UpdateMenuRequest(10L, 2L, "N", "new", "description");
        when(menuRepository.findById(10L)).thenReturn(Optional.empty());
        when(messageSourceResolver.resolve(MENU_NOT_FOUND, 10L)).thenReturn("menu not found");

        assertThatThrownBy(() -> menuService.update(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("menu not found");

        verify(categoryRepository, never()).findById(anyLong());
        verify(menuRepository, never()).save(any(Menu.class));
        verifyNoInteractions(mapper);
    }

    @Test
    void updatePreservesMenuStateWhenTargetCategoryIsMissing() {
        // MS-CMD-008
        Category originalCategory = category(1L, "original", List.of());
        Menu menu = menu(10L, "old", "old description", "O", originalCategory, List.of());
        String originalName = menu.getName();
        String originalDescription = menu.getDescription();
        String originalEmoji = menu.getEmoji();
        Category originalMenuCategory = menu.getCategory();
        UpdateMenuRequest request = new UpdateMenuRequest(10L, 2L, "N", "new", "new description");
        when(menuRepository.findById(10L)).thenReturn(Optional.of(menu));
        when(categoryRepository.findById(2L)).thenReturn(Optional.empty());
        when(messageSourceResolver.resolve(CATEGORY_NOT_FOUND, 2L)).thenReturn("category not found");

        assertThatThrownBy(() -> menuService.update(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("category not found");

        assertThat(menu.getName()).isEqualTo(originalName);
        assertThat(menu.getDescription()).isEqualTo(originalDescription);
        assertThat(menu.getEmoji()).isEqualTo(originalEmoji);
        assertThat(menu.getCategory()).isSameAs(originalMenuCategory);
        var lookupOrder = inOrder(menuRepository, categoryRepository);
        lookupOrder.verify(menuRepository).findById(10L);
        lookupOrder.verify(categoryRepository).findById(2L);
        verify(mapper, never()).toUpdatedResponse(any(Menu.class));
        verify(menuRepository, never()).save(any(Menu.class));
    }

    private AddMenuRequest menuRequest(long categoryId, String name, String description, String emoji) {
        return AddMenuRequest.builder()
                .categoryId(categoryId)
                .name(name)
                .description(description)
                .emoji(emoji)
                .build();
    }

    private Category category(long id, String name, List<Menu> menus) {
        Category category = Category.builder().name(name).menus(menus).build();
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }

    private Menu menu(Long id, String name, String description, String emoji, Category category,
                      List<Article> articles) {
        return Menu.builder()
                .id(id)
                .name(name)
                .description(description)
                .emoji(emoji)
                .category(category)
                .articles(articles)
                .build();
    }
}
