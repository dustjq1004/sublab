package me.kimyeonsup.home.domain.menu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import me.kimyeonsup.home.domain.article.adaptor.out.persistence.ArticleQueryJpaRepository;
import me.kimyeonsup.home.domain.draft.adaptor.out.persistence.DraftJpaRepository;
import me.kimyeonsup.home.domain.menu.domain.dto.AddMenuRequest;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.menu.repository.CategoryRepository;
import me.kimyeonsup.home.domain.menu.repository.MenuRepository;
import me.kimyeonsup.home.domain.menu.service.mapper.MenuMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

@SpringBootTest
class MenuServiceTransactionIntegrationTest {

    @Autowired
    private MenuService menuService;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ArticleQueryJpaRepository articleRepository;

    @Autowired
    private DraftJpaRepository draftRepository;

    @SpyBean
    private MenuMapper menuMapper;

    @BeforeEach
    void setUp() {
        draftRepository.deleteAll();
        articleRepository.deleteAll();
        menuRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void rollsBackMenuSaveWhenResponseMappingFails() {
        Category category = categoryRepository.save(Category.builder().name("language").build());
        long countBefore = menuRepository.count();
        AddMenuRequest request = new AddMenuRequest(category.getId(), "☕", "java", "description");
        doThrow(new IllegalStateException("response mapping failed"))
                .when(menuMapper).toCreatedResponse(any(Menu.class));

        assertThatThrownBy(() -> menuService.save(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("response mapping failed");

        assertThat(menuRepository.count()).isEqualTo(countBefore);
        assertThat(menuRepository.findAll())
                .noneMatch(menu -> menu.getName().equals("java")
                        && menu.getCategory().getId().equals(category.getId()));
    }
}
