package me.kimyeonsup.home.domain.blog.article.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.dto.AddMenuRequest;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.blog.admin.menu.repository.CategoryRepository;
import me.kimyeonsup.home.domain.blog.admin.menu.repository.MenuRepository;
import me.kimyeonsup.home.domain.blog.article.domain.dto.AddArticleRequest;
import me.kimyeonsup.home.domain.blog.article.repository.ArticleRepository;
import me.kimyeonsup.home.domain.blog.draft.repository.DraftRepository;
import me.kimyeonsup.home.login.domain.entity.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

@SpringBootTest
@AutoConfigureMockMvc
class BlogSidebarCacheIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private DraftRepository draftRepository;

    private Category category;
    private PrincipalDetail principalDetail;

    @BeforeEach
    void setUp() {
        clearCache();
        articleRepository.deleteAll();
        draftRepository.deleteAll();
        menuRepository.deleteAll();
        categoryRepository.deleteAll();

        category = categoryRepository.save(Category.builder()
                .name("cache-test-category")
                .build());
        principalDetail = new PrincipalDetail(User.builder()
                .email("cache-test@example.com")
                .password("test")
                .role("관리자")
                .build());
    }

    @AfterEach
    void tearDown() {
        clearCache();
    }

    @Test
    @DisplayName("/blog 캐시 생성 후 메뉴를 추가하면 사이드바에 즉시 반영된다")
    void blogSidebarReflectsMenuImmediatelyAfterCreate() throws Exception {
        requestBlog();

        String menuName = "cache-test-menu";
        AddMenuRequest request = new AddMenuRequest(category.getId(), "", menuName, "");
        mockMvc.perform(post("/api/admin/menu")
                        .with(user(principalDetail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Document document = Jsoup.parse(requestBlog());

        assertThat(document.select("a[data-menu-name=" + menuName + "]")).hasSize(1);
    }

    @Test
    @DisplayName("/blog 캐시 생성 후 글을 작성하면 사이드바 글 수에 즉시 반영된다")
    void blogSidebarReflectsArticleCountImmediatelyAfterCreate() throws Exception {
        Menu menu = menuRepository.save(Menu.builder()
                .name("cache-test-article-menu")
                .category(category)
                .build());
        requestBlog();

        AddArticleRequest request = new AddArticleRequest(
                "cache-test-title",
                "",
                "cache-test-content",
                menu.getId(),
                null,
                null,
                null,
                "PUBLIC"
        );
        mockMvc.perform(post("/api/articles")
                        .with(user(principalDetail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Document document = Jsoup.parse(requestBlog());

        assertThat(document.select("a[data-menu-name=cache-test-article-menu] .badge").text()).isEqualTo("1");
    }

    private String requestBlog() throws Exception {
        return mockMvc.perform(get("/blog").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private void clearCache() {
        cacheManager.getCacheNames().stream()
                .map(cacheManager::getCache)
                .forEach(cache -> {
                    if (cache != null) {
                        cache.clear();
                    }
                });
    }
}
