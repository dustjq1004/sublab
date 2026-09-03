package me.kimyeonsup.home.domain.blog.admin.menu.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.dto.AddCategoryRequest;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.dto.DeleteCategoryRequest;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.dto.UpdateCategoryRequest;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.blog.admin.menu.repository.CategoryRepository;
import me.kimyeonsup.home.domain.blog.admin.menu.repository.MenuRepository;
import me.kimyeonsup.home.domain.blog.admin.menu.service.CategoryService;
import me.kimyeonsup.home.domain.blog.article.repository.ArticleRepository;
import me.kimyeonsup.home.domain.blog.draft.repository.DraftRepository;
import me.kimyeonsup.home.login.domain.entity.User;
import me.kimyeonsup.home.login.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryApiControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    CategoryService categoryService;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    MenuRepository menuRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    DraftRepository draftRepository;

    User user;

    @BeforeEach
    void setSecurityContext() {
        userRepository.deleteAll();
        user = userRepository.save(User.builder()
                .email("admin@example.com")
                .password("test")
                .role("관리자")
                .build());

        Collection<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add((GrantedAuthority) () -> user.getRole());
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user,
                user.getPassword(), authorities));
    }

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
        articleRepository.deleteAll();
        draftRepository.deleteAll();
        menuRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("AddCategory : 카테고리 추가를 성공한다.")
    void addCategoryTest() throws Exception {
        final String url = "/api/category";
        final String name = "언어";
        final AddCategoryRequest userRequest = new AddCategoryRequest(name);

        final String requestBody = objectMapper.writeValueAsString(userRequest);

        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn("username");

        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .principal(principal)
                .content(requestBody));

        // then
        result.andExpect(status().isOk());

        List<Category> categories = categoryRepository.findAll();

        assertThat(categories.size()).isEqualTo(1);
        assertThat(categories.get(0).getName()).isEqualTo(name);
    }

    @DisplayName("deleteArticle : 카테고리를 삭제한다.")
    @Test
    void deleteCategory() throws Exception {
        final String url = "/api/category/delete";
        Category savedCategory = savedCategory("부트캠프");

        DeleteCategoryRequest request = new DeleteCategoryRequest(savedCategory.getId());

        //when
        mockMvc.perform(delete(url)
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
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isOk());

        Category category = categoryRepository.findById(savedCategory.getId()).get();

        assertThat(category.getName()).isEqualTo(newName);
    }

    private Category savedCategory(String name) {
        return categoryRepository.save(Category.builder()
                .name(name)
                .menus(new ArrayList<>())
                .build());
    }

}
