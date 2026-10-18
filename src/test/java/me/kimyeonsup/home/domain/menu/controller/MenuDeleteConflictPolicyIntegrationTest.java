package me.kimyeonsup.home.domain.menu.controller;

import static me.kimyeonsup.home.global.exception.message.constants.CategoryExceptionMessage.CATEGORY_HAS_MENUS;
import static me.kimyeonsup.home.global.exception.message.constants.CategoryExceptionMessage.CATEGORY_NOT_FOUND;
import static me.kimyeonsup.home.global.exception.message.constants.MenuExceptionMessage.MENU_DELETE_HAS_ARTICLE;
import static me.kimyeonsup.home.global.exception.message.constants.MenuExceptionMessage.MENU_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.article.adaptor.out.persistence.ArticleQueryJpaRepository;
import me.kimyeonsup.home.domain.article.domain.model.Article;
import me.kimyeonsup.home.domain.draft.adaptor.out.persistence.DraftJpaRepository;
import me.kimyeonsup.home.domain.login.domain.entity.User;
import me.kimyeonsup.home.domain.menu.domain.dto.DeleteCategoryRequest;
import me.kimyeonsup.home.domain.menu.domain.dto.DeleteMenuRequest;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.menu.repository.CategoryRepository;
import me.kimyeonsup.home.domain.menu.repository.MenuRepository;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MenuDeleteConflictPolicyIntegrationTest {

    private static final long MISSING_ID = Long.MAX_VALUE;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CategoryRepository categoryRepository;
    @Autowired MenuRepository menuRepository;
    @Autowired ArticleQueryJpaRepository articleRepository;
    @Autowired DraftJpaRepository draftRepository;
    @Autowired MessageSourceResolver messageSourceResolver;

    private final PrincipalDetail principalDetail = new PrincipalDetail(User.builder()
            .email("admin@example.com")
            .nickname("admin")
            .password("test")
            .role("ADMIN")
            .build());

    @BeforeEach
    void setUp() {
        draftRepository.deleteAll();
        articleRepository.deleteAll();
        menuRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void deletingMenuWithArticleReturnsConflictAndPreservesRelationship() throws Exception {
        // DEL-HTTP-MENU-409
        Category category = saveCategory("language");
        Menu menu = saveMenu(category, "java");
        Article article = articleRepository.save(Article.builder()
                .title("article")
                .content("body")
                .author("admin")
                .menu(menu)
                .visibility("PUBLIC")
                .build());

        mockMvc.perform(delete("/api/admin/menu/delete")
                        .with(user(principalDetail))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new DeleteMenuRequest(menu.getId()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("E409"))
                .andExpect(jsonPath("$.message").value(messageSourceResolver.resolve(MENU_DELETE_HAS_ARTICLE)))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.errors").value(nullValue()));

        assertThat(menuRepository.existsById(menu.getId())).isTrue();
        Article persistedArticle = articleRepository.findById(article.getId()).orElseThrow();
        assertThat(persistedArticle.getMenu().getId()).isEqualTo(menu.getId());
    }

    @Test
    void deletingCategoryWithMenuReturnsConflictAndPreservesRelationship() throws Exception {
        // DEL-HTTP-CATEGORY-409
        Category category = saveCategory("language");
        Menu menu = saveMenu(category, "java");

        mockMvc.perform(delete("/api/category/delete")
                        .with(user(principalDetail))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new DeleteCategoryRequest(category.getId()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("E409"))
                .andExpect(jsonPath("$.message").value(messageSourceResolver.resolve(CATEGORY_HAS_MENUS)))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.errors").value(nullValue()));

        assertThat(categoryRepository.existsById(category.getId())).isTrue();
        Menu persistedMenu = menuRepository.findById(menu.getId()).orElseThrow();
        assertThat(persistedMenu.getCategory().getId()).isEqualTo(category.getId());
    }

    @Test
    void deletingMissingMenuKeepsNotFoundContract() throws Exception {
        // DEL-HTTP-NOT-FOUND
        assertNotFound(delete("/api/admin/menu/delete")
                        .content(objectMapper.writeValueAsBytes(new DeleteMenuRequest(MISSING_ID))),
                messageSourceResolver.resolve(MENU_NOT_FOUND, MISSING_ID));
    }

    @Test
    void deletingMissingCategoryKeepsNotFoundContract() throws Exception {
        // DEL-HTTP-NOT-FOUND
        assertNotFound(delete("/api/category/delete")
                        .content(objectMapper.writeValueAsBytes(new DeleteCategoryRequest(MISSING_ID))),
                messageSourceResolver.resolve(CATEGORY_NOT_FOUND, MISSING_ID));
    }

    private void assertNotFound(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,
                                String expectedMessage) throws Exception {
        mockMvc.perform(request
                        .with(user(principalDetail))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("E404"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.errors").value(nullValue()));
    }

    private Category saveCategory(String name) {
        return categoryRepository.save(Category.builder().name(name).build());
    }

    private Menu saveMenu(Category category, String name) {
        return menuRepository.save(Menu.builder()
                .name(name)
                .description("description")
                .emoji("M")
                .category(category)
                .build());
    }
}
