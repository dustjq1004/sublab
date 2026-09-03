package me.kimyeonsup.home.domain.blog.article.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.blog.admin.menu.repository.CategoryRepository;
import me.kimyeonsup.home.domain.blog.admin.menu.repository.MenuRepository;
import me.kimyeonsup.home.domain.blog.article.domain.dto.AddArticleRequest;
import me.kimyeonsup.home.domain.blog.article.domain.entity.Article;
import me.kimyeonsup.home.domain.blog.article.repository.ArticleRepository;
import me.kimyeonsup.home.login.domain.entity.User;
import me.kimyeonsup.home.login.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class AddArticleFailTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    MenuRepository menuRepository;

    PrincipalDetail principalDetail;
    Category category;
    Menu menu;

    private static final String url = "/api/articles";

    @BeforeEach
    void setSecurityContext() {
        userRepository.deleteAll();
        User user = userRepository.save(User.builder()
                .email("admin@example.com")
                .password("test")
                .role("관리자")
                .build());
        category = categoryRepository.save(Category.builder()
                .name("언어")
                .build());
        menu = menuRepository.save(Menu.builder()
                .name("자바")
                .category(category)
                .build());

        principalDetail = new PrincipalDetail(user);
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add((GrantedAuthority) () -> user.getRole());
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(principalDetail,
                authorities, "GM"));
    }

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
        articleRepository.deleteAll();
    }

    @DisplayName("validateTitle: 타이틀 유효성 검증을한다.")
    @ParameterizedTest
    @CsvSource(
            {
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa, size must be between 0 and 100",
                    ",must not be blank"
            }
    )

    public void validateTitle(String title, String errorMessage) throws Exception {
        // given

        final String requestBody = getRequestBodyByTitle(title);
        log.info("requestBody : ${}", requestBody);
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn("username");

        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .principal(principal)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
        Assertions.assertThat(result.andReturn().getResolvedException().getMessage())
                .contains(errorMessage);
    }

    @DisplayName("블로그 글 추가시 썸네일 이미지 검증 테스트.")
    @ParameterizedTest
    @CsvSource({"[image](https://urlimage.com/images/image.png)",
            "[image](https://urlimage.com/images/image.png)",
            "[](https://urlimage.com/images/image.png)"})
    void saveThumbnailImageUrl(String content) throws Exception {
        // given
        final String url = "/api/articles";
        final String title = "title";
        final AddArticleRequest userRequest = new AddArticleRequest(title, "", content, menu.getId(), null, null, null,
                "PUBLIC");
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

        List<Article> articles = articleRepository.findAll();

        assertThat(articles.size()).isEqualTo(1);
        assertThat(articles.get(0).getThumbnailUrl().contains("noimage")).isTrue();
    }

    private String getRequestBodyByTitle(String title) throws JsonProcessingException {
        final String content = "";
        final String subTitle = "";

        final AddArticleRequest userRequest = new AddArticleRequest(title, subTitle, content, menu.getId(), null, null, null, "PUBLIC");
        final String requestBody = objectMapper.writeValueAsString(userRequest);
        return requestBody;
    }
}
