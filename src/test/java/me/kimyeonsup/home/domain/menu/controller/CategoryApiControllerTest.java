package me.kimyeonsup.home.domain.menu.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.article.adaptor.out.persistence.ArticleQueryJpaRepository;
import me.kimyeonsup.home.domain.menu.domain.dto.AddCategoryRequest;
import me.kimyeonsup.home.domain.menu.domain.dto.DeleteCategoryRequest;
import me.kimyeonsup.home.domain.menu.domain.dto.UpdateCategoryRequest;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.menu.repository.CategoryRepository;
import me.kimyeonsup.home.domain.menu.repository.MenuRepository;
import me.kimyeonsup.home.domain.draft.adaptor.out.persistence.DraftJpaRepository;
import me.kimyeonsup.home.domain.login.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryApiControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    MenuRepository menuRepository;

    @Autowired
    ArticleQueryJpaRepository articleRepository;

    @Autowired
    DraftJpaRepository draftRepository;

    private final PrincipalDetail principalDetail = new PrincipalDetail(User.builder()
            .email("user@example.com")
            .nickname("user")
            .password("test")
            .role("USER")
            .build());

    @BeforeEach
    void setUp() {
        articleRepository.deleteAll();
        draftRepository.deleteAll();
        menuRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("AddCategory : 카테고리 추가를 성공한다.")
    void addCategoryTest() throws Exception {
        // SEC-CATEGORY-002
        final String url = "/api/category/create";
        final String name = "언어";
        final AddCategoryRequest userRequest = new AddCategoryRequest(name);

        final String requestBody = objectMapper.writeValueAsString(userRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .with(user(principalDetail))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isOk());

        List<Category> categories = categoryRepository.findAll();

        assertThat(categories.size()).isEqualTo(1);
        assertThat(categories.get(0).getName()).isEqualTo(name);
    }

    @DisplayName("deleteCategory : 카테고리를 삭제한다.")
    @Test
    void deleteCategory() throws Exception {
        final String url = "/api/category/delete";
        Category savedCategory = savedCategory("부트캠프");

        DeleteCategoryRequest request = new DeleteCategoryRequest(savedCategory.getId());

        //when
        mockMvc.perform(delete(url)
                        .with(user(principalDetail))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        //then
        List<Category> categories = categoryRepository.findAll();

        assertThat(categories).isEmpty();
    }

    @DisplayName("카테고리 이름을 수정한다.")
    @Test
    void updateCategory() throws Exception {
        final String url = "/api/category/update";
        Category savedCategory = savedCategory("언어");

        final String newName = "인프라";

        UpdateCategoryRequest request = new UpdateCategoryRequest(savedCategory.getId(), newName);

        //when
        ResultActions result = mockMvc.perform(put(url, savedCategory.getId())
                .with(user(principalDetail))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isOk());

        Category category = categoryRepository.findById(savedCategory.getId()).get();

        assertThat(category.getName()).isEqualTo(newName);
    }

    @Test
    void authenticatedRequestWithoutCsrfCannotCreateCategory() throws Exception {
        // SEC-CATEGORY-001
        AddCategoryRequest request = new AddCategoryRequest("언어");

        assertThat(categoryRepository.count()).isZero();

        mockMvc.perform(post("/api/category")
                        .with(user(principalDetail))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        assertThat(categoryRepository.count()).isZero();
    }

    private Category savedCategory(String name) {
        return categoryRepository.save(Category.builder()
                .name(name)
                .menus(new ArrayList<>())
                .build());
    }

}
