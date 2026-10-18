package me.kimyeonsup.home.domain.menu.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import me.kimyeonsup.home.domain.article.adaptor.out.persistence.ArticleQueryJpaRepository;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.menu.repository.CategoryRepository;
import me.kimyeonsup.home.domain.menu.repository.MenuRepository;
import me.kimyeonsup.home.domain.draft.adaptor.out.persistence.DraftJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
class NavigatorApiControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    MenuRepository menuRepository;

    @Autowired
    ArticleQueryJpaRepository articleRepository;

    @Autowired
    DraftJpaRepository draftRepository;

    @Autowired
    CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        clearCategoriesCache();
        draftRepository.deleteAll();
        articleRepository.deleteAll();
        menuRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @AfterEach
    void clearCategoriesCache() {
        var cache = cacheManager.getCache("categories");
        if (cache != null) {
            cache.clear();
        }
    }

    @DisplayName("findAll : 블로그 메뉴를 조회 한다.")
    @Test
    public void findAllArticle() throws Exception {
        // SEC-NAV-001
        // given
        final String url = "/api/navigate";
        Category savedCategory = createDefaultNavigator();

        // when
        final ResultActions resultActions = mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value(savedCategory.getName()));

    }

    private Category createDefaultNavigator() {
        Category savedCategory = categoryRepository.save(Category.builder()
                .name("언어")
                .menus(new ArrayList<>())
                .build());

        Menu savedMenu = menuRepository.save(Menu.builder()
                .name("자바")
                .category(savedCategory)
                .build());

        savedCategory.getMenus().add(savedMenu);
        return savedCategory;
    }
}
