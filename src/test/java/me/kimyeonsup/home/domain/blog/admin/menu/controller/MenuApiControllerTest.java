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
import me.kimyeonsup.home.domain.blog.admin.menu.domain.dto.AddMenuRequest;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.dto.DeleteMenuRequest;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.dto.UpdateMenuRequest;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.blog.admin.menu.repository.CategoryRepository;
import me.kimyeonsup.home.domain.blog.admin.menu.repository.MenuRepository;
import me.kimyeonsup.home.domain.blog.admin.menu.service.MenuService;
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
class MenuApiControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    MenuService menuService;

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

    Category category;

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

        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn("username");

        // when
        ResultActions result = mockMvc.perform(post(url)
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
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isOk());

        Menu category = menuRepository.findById(savedMenu.getId()).get();

        assertThat(category.getName()).isEqualTo(newName);
    }

    private Menu savedMenu(Category category) {
        return menuRepository.save(Menu.builder()
                .name("자바")
                .category(category)
                .build());
    }
}
