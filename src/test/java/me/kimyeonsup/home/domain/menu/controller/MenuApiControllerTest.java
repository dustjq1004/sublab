package me.kimyeonsup.home.domain.menu.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.article.adaptor.out.persistence.ArticleQueryJpaRepository;
import me.kimyeonsup.home.domain.menu.domain.dto.AddMenuRequest;
import me.kimyeonsup.home.domain.menu.domain.dto.DeleteMenuRequest;
import me.kimyeonsup.home.domain.menu.domain.dto.UpdateMenuRequest;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
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
class MenuApiControllerTest {

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

    Category category;

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
        category = categoryRepository.save(Category.builder()
                .name("언어")
                .menus(new ArrayList<>())
                .build());
    }

    @Test
    @DisplayName("AddMenu : 메뉴를 추가를 성공한다.")
    void addMenuTest() throws Exception {
        final String url = "/api/admin/menu";
        final String name = "자바";
        final AddMenuRequest request = new AddMenuRequest(category.getId(), "", name, "");

        final String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .with(user(principalDetail))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isOk());

        List<Menu> menus = menuRepository.findAll();

        assertThat(menus.size()).isEqualTo(1);
        assertThat(menus.get(0).getName()).isEqualTo(name);
    }

    @DisplayName("deleteMenu : 메뉴를 삭제한다.")
    @Test
    void deleteMenu() throws Exception {
        final String url = "/api/admin/menu/delete";
        Menu savedMenu = savedMenu(category);

        DeleteMenuRequest menuRequest = new DeleteMenuRequest(savedMenu.getId());

        //when
        mockMvc.perform(delete(url)
                        .with(user(principalDetail))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(menuRequest)))
                .andExpect(status().isOk());

        //then
        List<Menu> menus = menuRepository.findAll();

        assertThat(menus).isEmpty();
    }

    @DisplayName("메뉴 이름을 수정한다.")
    @Test
    void updateMenu() throws Exception {
        final String url = "/api/admin/menu/update";
        Menu savedMenu = savedMenu(category);

        final String newName = "파이썬";

        UpdateMenuRequest request = new UpdateMenuRequest(savedMenu.getId(), category.getId(), "", newName, "");

        //when
        ResultActions result = mockMvc.perform(put(url)
                .with(user(principalDetail))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isOk());

        Menu menu = menuRepository.findById(savedMenu.getId()).get();

        assertThat(menu.getName()).isEqualTo(newName);
    }

    @Test
    void anonymousCannotReadProtectedMenus() throws Exception {
        // SEC-MENU-001
        mockMvc.perform(get("/api/admin/menus"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedUserCanReadProtectedMenus() throws Exception {
        // SEC-MENU-002
        mockMvc.perform(get("/api/admin/menus")
                        .with(user(principalDetail)))
                .andExpect(status().isOk());
    }

    private Menu savedMenu(Category category) {
        return menuRepository.save(Menu.builder()
                .name("자바")
                .category(category)
                .build());
    }
}
