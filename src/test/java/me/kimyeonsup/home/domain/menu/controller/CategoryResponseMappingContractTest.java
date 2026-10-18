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
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.article.adaptor.out.persistence.ArticleQueryJpaRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CategoryResponseMappingContractTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired EntityManager em;
    @Autowired CategoryRepository categories;
    @Autowired MenuRepository menus;
    @Autowired ArticleQueryJpaRepository articles;
    @Autowired DraftJpaRepository drafts;
    @Autowired CacheManager caches;

    private final PrincipalDetail principal = new PrincipalDetail(User.builder()
            .email("admin@example.com").nickname("admin").password("test").role("ADMIN").build());

    @BeforeEach
    void cleanData() {
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
    void createDetailAndUpdateHaveDistinctDateFieldsAndNullMenus() throws Exception {
        JsonNode created = response(post("/api/category").content("{\"name\":\"language\"}"));
        long id = created.path("id").asLong();
        assertThat(id).isPositive();
        assertCategory(created, id, "language", "", mapper.createArrayNode(), "", "");

        menus.saveAndFlush(Menu.builder().category(categories.findById(id).orElseThrow())
                .name("java").build());
        em.flush();
        em.clear();
        assertCategory(response(get("/api/category").param("categoryId", Long.toString(id))),
                id, "language", "", mapper.nullNode(), "지금", "지금");

        assertCategory(response(put("/api/category/update")
                .content("{\"id\":%d,\"name\":\"renamed\"}".formatted(id))),
                id, "renamed", "", mapper.nullNode(), "지금", null);
    }

    @Test
    void listHasBothDatesAndOmitsMenusEvenWhenMenuExists() throws Exception {
        Category category = categories.saveAndFlush(Category.builder().name("language").build());
        menus.saveAndFlush(Menu.builder().category(category).name("java").build());
        em.flush();
        em.clear();
        JsonNode data = response(get("/api/categories"));
        assertThat(data.size()).isEqualTo(1);
        assertThat(data.path("categories").size()).isEqualTo(1);
        assertCategory(data.path("categories").get(0), category.getId(), "language", "",
                mapper.nullNode(), null, null);
    }

    @Test
    void navigationIncludesExistingMenusAndUsesAnEmptyArrayWhenNoneExist() throws Exception {
        Category withMenu = categories.saveAndFlush(Category.builder().name("language").build());
        Category empty = categories.saveAndFlush(Category.builder().name("empty").build());
        menus.saveAndFlush(Menu.builder().category(withMenu).name("java").build());
        em.flush();
        em.clear();
        JsonNode data = response(get("/api/navigate"));
        assertThat(data.size()).isEqualTo(2);
        JsonNode withMenuResponse = findById(data, withMenu.getId());
        assertThat(withMenuResponse.path("menus")).hasSize(1);
        assertThat(withMenuResponse.path("menus").get(0).path("articleCount").asInt()).isZero();
        assertCategory(withMenuResponse, withMenu.getId(), "language", "",
                withMenuResponse.path("menus"), "", "");
        assertCategory(findById(data, empty.getId()), empty.getId(), "empty", "",
                mapper.createArrayNode(), "", "");
    }

    @Test
    void emptyListAndNavigationAreArrays() throws Exception {
        assertThat(response(get("/api/categories")))
                .isEqualTo(mapper.readTree("{\"categories\":[]}"));
        assertThat(response(get("/api/navigate"))).isEqualTo(mapper.createArrayNode());
    }

    private void assertCategory(JsonNode node, long id, String name, String emoji, JsonNode menus,
                                String createdDate, String updatedDate) throws Exception {
        ObjectNode expected = mapper.createObjectNode().put("id", id).put("name", name).put("emoji", emoji);
        expected.set("menus", menus);
        expected.put("createdAt", expectedDate(node, "createdAt", createdDate));
        expected.put("updatedAt", expectedDate(node, "updatedAt", updatedDate));
        assertThat(node).isEqualTo(mapper.readTree(expected.toString()));
    }

    private String expectedDate(JsonNode node, String field, String expected) {
        assertThat(node.has(field)).isTrue();
        assertThat(node.get(field).isTextual()).isTrue();
        if (expected != null) return expected;
        assertThat(node.get(field).asText()).isNotBlank();
        return node.get(field).asText();
    }

    private JsonNode findById(JsonNode array, long id) {
        for (JsonNode item : array) if (item.path("id").asLong() == id) return item;
        throw new AssertionError("Missing category id " + id);
    }

    private JsonNode response(MockHttpServletRequestBuilder request) throws Exception {
        String body = mvc.perform(request.with(user(principal)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = mapper.readTree(body);
        assertThat(root.size()).isEqualTo(5);
        assertThat(root.path("success").asBoolean()).isTrue();
        assertThat(root.path("code").asText()).isEqualTo("S200");
        assertThat(root.path("message").asText()).isEqualTo("SUCCESS");
        assertThat(root.get("errors").isNull()).isTrue();
        return root.get("data");
    }
}
