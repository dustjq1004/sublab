package me.kimyeonsup.home.domain.menu.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.stream.StreamSupport;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.article.adaptor.out.persistence.ArticleQueryJpaRepository;
import me.kimyeonsup.home.domain.draft.adaptor.out.persistence.DraftJpaRepository;
import me.kimyeonsup.home.domain.login.domain.entity.User;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.menu.repository.CategoryRepository;
import me.kimyeonsup.home.domain.menu.repository.MenuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
class MenuRequestValidationIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
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

    private Category category;
    private Menu menu;

    @BeforeEach
    void setUp() {
        draftRepository.deleteAll();
        articleRepository.deleteAll();
        menuRepository.deleteAll();
        categoryRepository.deleteAll();
        category = categoryRepository.save(Category.builder().name("language").build());
        menu = menuRepository.save(Menu.builder()
                .name("java")
                .emoji("☕")
                .description("description")
                .category(category)
                .build());
    }

    @Test
    void rejectsBlankCategoryUpdateName() throws Exception {
        ObjectNode body = objectMapper.createObjectNode()
                .put("id", category.getId())
                .put("name", "   ");

        assertValidation(put("/api/category/update"), body, "name", this::assertOriginalCategory);
    }

    @Test
    void rejectsBlankMenuUpdateName() throws Exception {
        ObjectNode body = validMenuUpdate().put("name", "   ");

        assertValidation(put("/api/admin/menu/update"), body, "name", this::assertOriginalMenu);
    }

    @ParameterizedTest
    @EnumSource(InvalidUpdateId.class)
    void rejectsNonPositiveUpdateTargetId(InvalidUpdateId invalid) throws Exception {
        MockHttpServletRequestBuilder request;
        ObjectNode body;
        if (invalid == InvalidUpdateId.CATEGORY_ZERO) {
            request = put("/api/category/update");
            body = objectMapper.createObjectNode().put("id", 0).put("name", "renamed");
        } else {
            request = put("/api/admin/menu/update");
            body = validMenuUpdate().put("id", -1);
        }

        assertValidation(request, body, "id", this::assertAllOriginalRows);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0, -1})
    void rejectsMissingOrNonPositiveMenuCategoryId(Long invalidCategoryId) throws Exception {
        ObjectNode body = validMenuUpdate();
        if (invalidCategoryId == null) {
            body.putNull("categoryId");
        } else {
            body.put("categoryId", invalidCategoryId);
        }

        assertValidation(put("/api/admin/menu/update"), body, "categoryId", this::assertOriginalMenu);
    }

    @ParameterizedTest
    @EnumSource(InvalidDeleteId.class)
    void rejectsMissingOrNegativeDeleteId(InvalidDeleteId invalid) throws Exception {
        MockHttpServletRequestBuilder request;
        ObjectNode body = objectMapper.createObjectNode();
        String field;
        if (invalid == InvalidDeleteId.CATEGORY_MISSING) {
            request = delete("/api/category/delete");
            field = "categoryId";
        } else {
            request = delete("/api/admin/menu/delete");
            body.put("menuId", -1);
            field = "menuId";
        }

        assertValidation(request, body, field, this::assertAllOriginalRows);
    }

    private ObjectNode validMenuUpdate() {
        return objectMapper.createObjectNode()
                .put("id", menu.getId())
                .put("categoryId", category.getId())
                .put("emoji", "☕")
                .put("name", "java")
                .put("description", "description");
    }

    private void assertValidation(MockHttpServletRequestBuilder request, JsonNode body, String field,
                                  Executable databaseInvariant) throws Exception {
        MvcResult result = mockMvc.perform(request
                        .with(user(principalDetail))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body)))
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode fieldError = findFieldError(response.path("errors"), field);

        assertAll(
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(400),
                () -> assertThat(response.path("success").asBoolean()).isFalse(),
                () -> assertThat(response.path("code").asText()).isEqualTo("E001"),
                () -> assertThat(response.path("message").asText()).isEqualTo("Validation failed"),
                () -> assertThat(response.path("data").isNull()).isTrue(),
                () -> assertThat(fieldError).as("field error for %s", field).isNotNull(),
                () -> assertThat(fieldError.path("message").asText()).isNotBlank(),
                databaseInvariant
        );
    }

    private JsonNode findFieldError(JsonNode errors, String field) {
        return StreamSupport.stream(errors.spliterator(), false)
                .filter(error -> error.path("field").asText().equals(field))
                .findFirst()
                .orElse(null);
    }

    private void assertOriginalCategory() {
        Category persisted = categoryRepository.findById(category.getId()).orElseThrow();
        assertThat(persisted.getName()).isEqualTo("language");
    }

    private void assertOriginalMenu() {
        Menu persisted = menuRepository.findById(menu.getId()).orElseThrow();
        assertThat(persisted.getName()).isEqualTo("java");
        assertThat(persisted.getEmoji()).isEqualTo("☕");
        assertThat(persisted.getDescription()).isEqualTo("description");
        assertThat(persisted.getCategory().getId()).isEqualTo(category.getId());
    }

    private void assertAllOriginalRows() {
        assertOriginalCategory();
        assertOriginalMenu();
    }

    private enum InvalidUpdateId {
        CATEGORY_ZERO,
        MENU_NEGATIVE
    }

    private enum InvalidDeleteId {
        CATEGORY_MISSING,
        MENU_NEGATIVE
    }
}
