package me.kimyeonsup.home.domain.menu.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.StreamSupport;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.article.adaptor.out.persistence.ArticleQueryJpaRepository;
import me.kimyeonsup.home.domain.draft.adaptor.out.persistence.DraftJpaRepository;
import me.kimyeonsup.home.domain.login.domain.entity.User;
import me.kimyeonsup.home.domain.menu.domain.dto.AddCategoryRequest;
import me.kimyeonsup.home.domain.menu.domain.dto.AddMenuRequest;
import me.kimyeonsup.home.domain.menu.domain.dto.DeleteCategoryRequest;
import me.kimyeonsup.home.domain.menu.domain.dto.DeleteMenuRequest;
import me.kimyeonsup.home.domain.menu.domain.dto.UpdateCategoryRequest;
import me.kimyeonsup.home.domain.menu.domain.dto.UpdateMenuRequest;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.menu.repository.CategoryRepository;
import me.kimyeonsup.home.domain.menu.repository.MenuRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
class NavigatorCacheEvictionIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CacheManager cacheManager;
    @Autowired CategoryRepository categoryRepository;
    @Autowired MenuRepository menuRepository;
    @Autowired ArticleQueryJpaRepository articleRepository;
    @Autowired DraftJpaRepository draftRepository;

    private final PrincipalDetail principalDetail = new PrincipalDetail(User.builder()
            .email("admin@example.com")
            .nickname("admin")
            .password("test")
            .role("ADMIN")
            .build());

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

    @Test
    void categoryCreationRefreshesNavigation() throws Exception {
        assertThat(navigate()).isEmpty();

        JsonNode created = command(post("/api/category"), new AddCategoryRequest("language"));

        assertThat(category(navigate(), created.path("id").asLong()).path("name").asText())
                .isEqualTo("language");
    }

    @Test
    void categoryUpdateRefreshesNavigation() throws Exception {
        Category saved = saveCategory("old category");
        assertThat(category(navigate(), saved.getId()).path("name").asText()).isEqualTo("old category");

        command(put("/api/category/update"), new UpdateCategoryRequest(saved.getId(), "new category"));

        JsonNode reloaded = category(navigate(), saved.getId());
        assertThat(reloaded.path("name").asText()).isEqualTo("new category");
    }

    @Test
    void categoryDeletionRefreshesNavigation() throws Exception {
        Category saved = saveCategory("empty category");
        assertThat(category(navigate(), saved.getId()).path("name").asText()).isEqualTo("empty category");

        command(delete("/api/category/delete"), new DeleteCategoryRequest(saved.getId()));

        assertThat(findById(navigate(), saved.getId())).isNull();
    }

    @Test
    void menuCreationRefreshesNavigation() throws Exception {
        Category savedCategory = saveCategory("language");
        assertThat(category(navigate(), savedCategory.getId()).path("menus")).isEmpty();

        JsonNode created = command(post("/api/admin/menu"),
                new AddMenuRequest(savedCategory.getId(), "☕", "java", "description"));

        JsonNode menu = menu(category(navigate(), savedCategory.getId()), created.path("id").asLong());
        assertThat(menu.path("name").asText()).isEqualTo("java");
    }

    @Test
    void menuNameUpdateRefreshesNavigation() throws Exception {
        Category savedCategory = saveCategory("language");
        Menu savedMenu = saveMenu(savedCategory, "old menu");
        assertThat(menu(category(navigate(), savedCategory.getId()), savedMenu.getId())
                .path("name").asText()).isEqualTo("old menu");

        command(put("/api/admin/menu/update"),
                new UpdateMenuRequest(savedMenu.getId(), savedCategory.getId(), "☕", "new menu", "description"));

        assertThat(menu(category(navigate(), savedCategory.getId()), savedMenu.getId())
                .path("name").asText()).isEqualTo("new menu");
    }

    @Test
    void movingMenuToAnotherCategoryRefreshesNavigation() throws Exception {
        Category source = saveCategory("source");
        Category target = saveCategory("target");
        Menu savedMenu = saveMenu(source, "java");
        JsonNode cached = navigate();
        assertThat(findById(category(cached, source.getId()).path("menus"), savedMenu.getId())).isNotNull();
        assertThat(category(cached, target.getId()).path("menus")).isEmpty();

        command(put("/api/admin/menu/update"),
                new UpdateMenuRequest(savedMenu.getId(), target.getId(), "☕", "java", "description"));

        JsonNode refreshed = navigate();
        assertThat(findById(category(refreshed, source.getId()).path("menus"), savedMenu.getId())).isNull();
        assertThat(findById(category(refreshed, target.getId()).path("menus"), savedMenu.getId())).isNotNull();
    }

    @Test
    void menuDeletionRefreshesNavigation() throws Exception {
        Category savedCategory = saveCategory("language");
        Menu savedMenu = saveMenu(savedCategory, "java");
        assertThat(findById(category(navigate(), savedCategory.getId()).path("menus"), savedMenu.getId()))
                .isNotNull();

        command(delete("/api/admin/menu/delete"), new DeleteMenuRequest(savedMenu.getId()));

        JsonNode refreshedCategory = category(navigate(), savedCategory.getId());
        assertThat(refreshedCategory.path("name").asText()).isEqualTo("language");
        assertThat(findById(refreshedCategory.path("menus"), savedMenu.getId())).isNull();
    }

    private JsonNode navigate() throws Exception {
        String body = mockMvc.perform(get("/api/navigate").with(user(principalDetail)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data");
    }

    private JsonNode command(MockHttpServletRequestBuilder request, Object body) throws Exception {
        String response = mockMvc.perform(request
                        .with(user(principalDetail))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data");
    }

    private Category saveCategory(String name) {
        return categoryRepository.save(Category.builder().name(name).build());
    }

    private Menu saveMenu(Category category, String name) {
        return menuRepository.save(Menu.builder()
                .name(name)
                .description("description")
                .emoji("☕")
                .category(category)
                .build());
    }

    private JsonNode category(JsonNode navigation, long categoryId) {
        JsonNode found = findById(navigation, categoryId);
        assertThat(found).as("category %s", categoryId).isNotNull();
        return found;
    }

    private JsonNode menu(JsonNode category, long menuId) {
        JsonNode found = findById(category.path("menus"), menuId);
        assertThat(found).as("menu %s", menuId).isNotNull();
        return found;
    }

    private JsonNode findById(JsonNode array, long id) {
        return StreamSupport.stream(array.spliterator(), false)
                .filter(node -> node.path("id").asLong() == id)
                .findFirst()
                .orElse(null);
    }
}
