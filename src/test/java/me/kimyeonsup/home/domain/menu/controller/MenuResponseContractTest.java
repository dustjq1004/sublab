package me.kimyeonsup.home.domain.menu.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.util.stream.StreamSupport;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.article.adaptor.out.persistence.ArticleQueryJpaRepository;
import me.kimyeonsup.home.domain.article.domain.model.Article;
import me.kimyeonsup.home.domain.draft.adaptor.out.persistence.DraftJpaRepository;
import me.kimyeonsup.home.domain.login.domain.entity.User;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

/** 현재 JSON을 고정하는 특성 테스트. 누락 필드 보완은 별도 계약 변경으로 처리한다. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MenuResponseContractTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired EntityManager em;
    @Autowired CategoryRepository categories;
    @Autowired MenuRepository menus;
    @Autowired ArticleQueryJpaRepository articles;
    @Autowired DraftJpaRepository drafts;
    @Autowired CacheManager caches;

    final PrincipalDetail principal = new PrincipalDetail(User.builder()
            .email("admin@example.com").nickname("admin").password("test").role("ADMIN").build());

    @BeforeEach
    void setup() {
        clearCache();
        drafts.deleteAll();
        articles.deleteAll();
        menus.deleteAll();
        categories.deleteAll();
        em.flush();
        em.clear();
    }

    @AfterEach
    void clearCache() {
        var cache = caches.getCache("categories");
        if (cache != null) cache.clear();
    }

    @Test
    void menuCreateAndUpdateKeepResponseContractAndUpdateEmoji() throws Exception {
        Category category = category("language");
        JsonNode created = response(post("/api/admin/menu").content("""
                {"categoryId":%d,"emoji":"☕","name":"java","description":"description"}
                """.formatted(category.getId())));
        long id = created.path("id").asLong();
        assertThat(id).isPositive();
        assertThat(created).isEqualTo(mapper.readTree(menuJson(id, category.getId(), "java", 0).toString()));
        Menu menu = menus.findById(id).orElseThrow();
        assertThat(menu.getEmoji()).isEqualTo("☕");
        assertThat(menu.getDescription()).isEqualTo("description");
        articles.save(Article.builder().title("post").content("body").author("admin")
                .menu(menu).visibility("PUBLIC").build());
        em.flush();
        JsonNode updated = response(put("/api/admin/menu/update").content("""
                {"id":%d,"categoryId":%d,"emoji":"new","name":"renamed","description":"changed"}
                """.formatted(id, category.getId())));
        // 게시글이 있어도 명령 응답의 0은 집계값이 아니라 생성자 기본값이다.
        ObjectNode expectedUpdated = menuJson(id, category.getId(), "renamed", 0);
        expectedUpdated.put("emoji", "new");
        assertThat(updated).isEqualTo(mapper.readTree(expectedUpdated.toString()));
        em.flush();
        em.clear();
        Menu persisted = menus.findById(id).orElseThrow();
        assertThat(persisted.getEmoji()).isEqualTo("new");
        assertThat(persisted.getDescription()).isEqualTo("changed");

        JsonNode deletedEmoji = response(put("/api/admin/menu/update").content("""
                {"id":%d,"categoryId":%d,"emoji":null,"name":"renamed","description":"changed"}
                """.formatted(id, category.getId())));
        assertThat(deletedEmoji)
                .isEqualTo(mapper.readTree(menuJson(id, category.getId(), "renamed", 0).toString()));
        em.flush();
        em.clear();
        assertThat(menus.findById(id).orElseThrow().getEmoji()).isNull();
    }

    @Test
    void menuDetailAndListsHaveDifferentFieldSetsAndCounts() throws Exception {
        Category category = category("language");
        Menu menu = menu(category, "java");
        Menu empty = menu(category, "empty");
        for (String visibility : new String[]{"PUBLIC", "PRIVATE"}) {
            articles.save(Article.builder().title(visibility).content("body").author("admin")
                    .menu(menu).visibility(visibility).build());
        }
        em.flush();
        em.clear();
        ObjectNode detail = menuJson(menu.getId(), category.getId(), "java", null);
        detail.put("emoji", "☕")
                .put("description", "description")
                .put("articleCount", 2);
        dates(detail);
        assertThat(normalized(response(get("/api/admin/menu/{id}", menu.getId())))).isEqualTo(mapper.readTree(detail.toString()));

        for (String url : new String[]{"/api/menus", "/api/admin/menus"}) {
            JsonNode list = response(get(url));
            assertThat(list.size()).isEqualTo(1);
            assertThat(list.path("menus").size()).isEqualTo(2);
            for (Menu item : new Menu[]{menu, empty}) {
                ObjectNode expected = detail.deepCopy();
                expected.put("id", item.getId()).put("name", item.getName());
                expected.put("articleCount", item == menu ? 2 : 0);
                assertThat(normalized(byId(list.path("menus"), item.getId()))).isEqualTo(mapper.readTree(expected.toString()));
            }
        }
        JsonNode list = response(get("/api/admin/menus/{id}", category.getId()));
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.path("menus").size()).isEqualTo(2);
        ObjectNode expected = detail.deepCopy();
        expected.put("articleCount", 2);
        assertThat(normalized(byId(list.path("menus"), menu.getId()))).isEqualTo(mapper.readTree(expected.toString()));
        expected.put("id", empty.getId()).put("name", empty.getName()).put("articleCount", 0);
        assertThat(normalized(byId(list.path("menus"), empty.getId()))).isEqualTo(mapper.readTree(expected.toString()));
    }

    @Test
    void categoryCreateUpdateDetailAndListDistinguishNullAndEmptyMenus() throws Exception {
        JsonNode created = response(post("/api/category").content("{\"name\":\"language\"}"));
        long id = created.path("id").asLong();
        ObjectNode expected = categoryJson(id, "language", null);
        expected.set("menus", mapper.createArrayNode());
        assertThat(created).isEqualTo(mapper.readTree(expected.toString()));
        menu(categories.findById(id).orElseThrow(), "java");
        em.flush();
        em.clear();
        expected.putNull("menus");
        dates(expected);
        assertThat(normalized(response(get("/api/category").param("categoryId", Long.toString(id)))))
                .isEqualTo(mapper.readTree(expected.toString()));
        JsonNode list = response(get("/api/categories"));
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.path("categories").size()).isEqualTo(1);
        assertThat(normalized(list.path("categories").get(0))).isEqualTo(mapper.readTree(expected.toString()));
        expected.put("name", "renamed");
        assertThat(normalized(response(put("/api/category/update")
                .content("{\"id\":%d,\"name\":\"renamed\"}".formatted(id))))).isEqualTo(mapper.readTree(expected.toString()));
    }

    @Test
    void navigationIncludesNestedMenusCountsAndEmptyCategory() throws Exception {
        Category category = category("language");
        Category emptyCategory = category("empty-category");
        Menu counted = menu(category, "java");
        Menu emptyMenu = menu(category, "empty");
        articles.save(Article.builder().title("post").content("body").author("admin")
                .menu(counted).visibility("PUBLIC").build());
        em.flush();
        em.clear();
        JsonNode navigation = response(get("/api/navigate"));
        assertThat(navigation.size()).isEqualTo(2);
        ObjectNode expected = categoryJson(category.getId(), "language", "📚");
        JsonNode actualCategory = byId(navigation, category.getId());
        assertThat(actualCategory.path("menus").size()).isEqualTo(2);
        assertThat(byId(actualCategory.path("menus"), counted.getId()))
                .isEqualTo(mapper.readTree(menuJson(counted.getId(), category.getId(), "java", 1).toString()));
        assertThat(byId(actualCategory.path("menus"), emptyMenu.getId()))
                .isEqualTo(mapper.readTree(menuJson(emptyMenu.getId(), category.getId(), "empty", 0).toString()));
        expected.set("menus", actualCategory.path("menus"));
        assertThat(actualCategory).isEqualTo(mapper.readTree(expected.toString()));
        ObjectNode empty = categoryJson(emptyCategory.getId(), "empty-category", "📚");
        empty.set("menus", mapper.createArrayNode());
        assertThat(byId(navigation, emptyCategory.getId())).isEqualTo(mapper.readTree(empty.toString()));
    }

    @Test
    void emptyListsAreArraysInsteadOfNull() throws Exception {
        assertThat(response(get("/api/menus"))).isEqualTo(mapper.readTree("{\"menus\":[]}"));
        assertThat(response(get("/api/admin/menus"))).isEqualTo(mapper.readTree("{\"menus\":[]}"));
        assertThat(response(get("/api/admin/menus/{id}", 999999L)))
                .isEqualTo(mapper.readTree("{\"menus\":[]}"));
        assertThat(response(get("/api/categories"))).isEqualTo(mapper.readTree("{\"categories\":[]}"));
        assertThat(response(get("/api/navigate"))).isEqualTo(mapper.createArrayNode());
    }

    private JsonNode response(MockHttpServletRequestBuilder request) throws Exception {
        String body = mvc.perform(request.with(user(principal)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = mapper.readTree(body);
        ObjectNode envelope = mapper.createObjectNode().put("success", true)
                .put("code", "S200").put("message", "SUCCESS");
        envelope.putNull("errors");
        envelope.set("data", root.get("data"));
        assertThat(root).isEqualTo(envelope);
        return root.get("data");
    }

    private Category category(String name) {
        Category category = Category.builder().name(name).build();
        ReflectionTestUtils.setField(category, "emoji", "📚");
        return categories.saveAndFlush(category);
    }

    private Menu menu(Category category, String name) {
        return menus.saveAndFlush(Menu.builder().category(category).name(name)
                .emoji("☕").description("description").build());
    }

    private ObjectNode menuJson(long id, long categoryId, String name, Integer count) {
        ObjectNode node = mapper.createObjectNode().put("id", id).put("name", name)
                .put("categoryId", categoryId).put("categoryEmoji", "📚").put("categoryName", "language");
        for (String field : new String[]{"emoji", "description", "createdAt", "updatedAt"}) node.put(field, "");
        node.put("articleCount", count);
        return node;
    }

    private ObjectNode categoryJson(long id, String name, String emoji) {
        ObjectNode node = mapper.createObjectNode().put("id", id).put("name", name).put("emoji", emoji == null ? "" : emoji);
        node.putNull("menus");
        node.put("createdAt", "").put("updatedAt", "");
        return node;
    }

    private JsonNode byId(JsonNode array, long id) {
        return StreamSupport.stream(array.spliterator(), false)
                .filter(item -> item.path("id").asLong() == id).findFirst().orElseThrow();
    }

    private void dates(ObjectNode node) {
        node.put("createdAt", "<relative-time>").put("updatedAt", "<relative-time>");
    }

    private JsonNode normalized(JsonNode original) {
        ObjectNode copy = original.deepCopy();
        for (String field : new String[]{"createdAt", "updatedAt"}) {
            assertThat(copy.has(field)).isTrue();
            assertThat(copy.get(field).isTextual()).isTrue();
            assertThat(copy.get(field).asText()).isNotBlank();
            copy.put(field, "<relative-time>");
        }
        return copy;
    }
}
