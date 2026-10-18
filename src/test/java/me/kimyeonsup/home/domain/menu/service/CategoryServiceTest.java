package me.kimyeonsup.home.domain.menu.service;

import static me.kimyeonsup.home.global.exception.message.constants.CategoryExceptionMessage.CATEGORY_HAS_MENUS;
import static me.kimyeonsup.home.global.exception.message.constants.CategoryExceptionMessage.CATEGORY_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import me.kimyeonsup.home.domain.menu.domain.dto.AddCategoryRequest;
import me.kimyeonsup.home.domain.menu.domain.dto.CategoryResponse;
import me.kimyeonsup.home.domain.menu.domain.dto.UpdateCategoryRequest;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.menu.repository.CategoryRepository;
import me.kimyeonsup.home.domain.menu.service.mapper.CategoryMapper;
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
class CategoryServiceTest {

    @Mock CategoryRepository categoryRepository;
    @Mock MessageSourceResolver messageSourceResolver;
    @Mock CategoryMapper mapper;
    @InjectMocks CategoryService categoryService;

    @Test
    void saveCreatesCategory() {
        // CS-CMD-001
        AddCategoryRequest request = new AddCategoryRequest("new");
        Category category = category(null, "new", List.of());
        Category savedCategory = category(1L, "new", List.of());
        CategoryResponse expected = CategoryResponse.builder().id(1L).name("new").build();
        when(mapper.toCategory(request)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(savedCategory);
        when(mapper.toCreatedResponse(savedCategory)).thenReturn(expected);

        CategoryResponse actual = categoryService.save(request);

        assertThat(actual).isSameAs(expected);
        verify(mapper).toCategory(request);
        verify(categoryRepository).save(category);
        verify(mapper).toCreatedResponse(savedCategory);
        verifyNoInteractions(messageSourceResolver);
    }

    @Test
    void deleteRemovesCategoryWithoutMenus() {
        // CS-CMD-002
        Category category = category(1L, "category", List.of());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThatCode(() -> categoryService.delete(1L)).doesNotThrowAnyException();

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).delete(category);
        verifyNoInteractions(messageSourceResolver, mapper);
    }

    @Test
    void deleteRejectsMissingCategoryWithoutDeleting() {
        // CS-CMD-003
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        when(messageSourceResolver.resolve(CATEGORY_NOT_FOUND, 1L)).thenReturn("category not found");

        ResourceNotFoundException exception = catchThrowableOfType(
                () -> categoryService.delete(1L), ResourceNotFoundException.class);

        assertThat(exception).hasMessage("category not found");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getCode()).isEqualTo("E404");

        verify(messageSourceResolver).resolve(CATEGORY_NOT_FOUND, 1L);
        verify(categoryRepository, never()).delete(any(Category.class));
        verifyNoInteractions(mapper);
    }

    @Test
    void deleteRejectsCategoryWithMenusAndPreservesState() {
        // CS-CMD-004
        List<Menu> menus = List.of(Menu.builder().id(10L).build());
        Category category = category(1L, "category", menus);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(messageSourceResolver.resolve(CATEGORY_HAS_MENUS)).thenReturn("category has menus");

        ResourceConflictException exception = catchThrowableOfType(
                () -> categoryService.delete(1L), ResourceConflictException.class);

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getCode()).isEqualTo("E409");
        assertThat(exception).hasMessage("category has menus");

        assertThat(category.getName()).isEqualTo("category");
        assertThat(category.getMenus()).isSameAs(menus);
        verify(messageSourceResolver).resolve(CATEGORY_HAS_MENUS);
        verify(categoryRepository, never()).delete(any(Category.class));
        verifyNoInteractions(mapper);
    }

    @Test
    void updateChangesCategoryName() {
        // CS-CMD-005
        Category category = category(1L, "old", List.of());
        UpdateCategoryRequest request = new UpdateCategoryRequest(1L, "new");
        CategoryResponse expected = CategoryResponse.builder().id(1L).name("new").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(mapper.toUpdatedResponse(category)).thenReturn(expected);

        CategoryResponse actual = categoryService.update(request);

        assertThat(category.getName()).isEqualTo("new");
        assertThat(actual).isSameAs(expected);
        verify(categoryRepository).findById(request.getId());
        verify(mapper).toUpdatedResponse(category);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateRejectsMissingCategoryWithoutMappingResponse() {
        // CS-CMD-006
        UpdateCategoryRequest request = new UpdateCategoryRequest(1L, "new");
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        when(messageSourceResolver.resolve(CATEGORY_NOT_FOUND, 1L)).thenReturn("category not found");

        assertThatThrownBy(() -> categoryService.update(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("category not found");

        verify(messageSourceResolver).resolve(CATEGORY_NOT_FOUND, request.getId());
        verify(categoryRepository, never()).save(any(Category.class));
        verifyNoInteractions(mapper);
    }

    private Category category(Long id, String name, List<Menu> menus) {
        Category category = Category.builder().name(name).menus(menus).build();
        if (id != null) {
            ReflectionTestUtils.setField(category, "id", id);
        }
        return category;
    }
}
