package me.kimyeonsup.home.domain.draft.adaptor.in.web;


import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.article.domain.model.Thumbnail;
import me.kimyeonsup.home.domain.draft.adaptor.in.web.request.CreateDraftRequest;
import me.kimyeonsup.home.domain.draft.adaptor.in.web.request.UpdateDraftRequest;
import me.kimyeonsup.home.domain.draft.domain.model.DraftArticle;
import me.kimyeonsup.home.domain.draft.adaptor.out.persistence.DraftJpaRepository;
import me.kimyeonsup.home.domain.login.domain.entity.User;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.menu.repository.CategoryRepository;
import me.kimyeonsup.home.domain.menu.repository.MenuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
class DraftApiControllerTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    DraftJpaRepository draftRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    MenuRepository menuRepository;

    PrincipalDetail principalDetail;
    Long menuId;
    Long updateMenuId;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .alwaysDo(print())
                .build();
        draftRepository.deleteAllInBatch();

        Category category = categoryRepository.save(Category.builder()
                .name("draft-test-category")
                .build());
        menuId = menuRepository.save(Menu.builder()
                .name("draft-test-menu")
                .category(category)
                .build()).getId();
        updateMenuId = menuRepository.save(Menu.builder()
                .name("draft-test-update-menu")
                .category(category)
                .build()).getId();
    }

    @BeforeEach
    void setSecurityContext() {
        User user = User.builder()
                .email("admin@example.com")
                .password("test")
                .role("관리자")
                .build();

        principalDetail = new PrincipalDetail(user);
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add((GrantedAuthority) user::getRole);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(principalDetail,
                authorities, "GM"));
    }

    @Test
    @DisplayName("임시 저장한 게시글을 조회한다.")
    void getDraftArticle() throws Exception {
        // given
        final String title = "title";
        final String subTitle = "subTitle";
        final String content = "content";
        final String author = "admin";

        List<DraftArticle> draftArticles = saveDraftArticles(title, subTitle, content, author);

        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn("username");

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/drafts")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .principal(principal));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(String.valueOf(draftArticles.size()))))
                .andExpect(content().string(containsString(draftArticles.get(draftArticles.size() - 1).getTitle())));

        log.info("mvcResult {}", resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    @DisplayName("게시글을 임시로 저장한다.")
    void saveDraftArticle() throws Exception {
        // given
        String title = "title";
        String subTitle = "subTitle";
        String content = "content";

        CreateDraftRequest addDraftArticle = new CreateDraftRequest(null, title, subTitle, content, menuId, null, null);

        String requestBody = objectMapper.writeValueAsString(addDraftArticle);

        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn("username");
        // when
        ResultActions resultActions = mockMvc.perform(post("/api/draft/create")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .principal(principal)
                .content(requestBody));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(title)))
                .andExpect(content().string(containsString("admin")));
    }

    @ParameterizedTest
    @DisplayName("임시로 저장된 게시글을 수정한다.")
    @CsvSource({"1,수정1", "2,수정2", "3,수정3", "4,수정4"})
    void updateDraftArticle(Long id, String updateContent) throws Exception {
        // given
        final String title = "title";
        final String subTitle = "subTitle";
        final String content = "content";
        final String author = "admin";

        saveDraftArticles(title, subTitle, content, author);

        DraftArticle draftArticle = draftRepository.findAll().get(id.intValue() - 1);
        Long version = draftArticle.getVersion();
        UpdateDraftRequest updatedArticle = new UpdateDraftRequest(version, title, subTitle,
                updateContent, updateMenuId, null, null);
        String requestBody = objectMapper.writeValueAsString(updatedArticle);

        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn("username");

        // when
        ResultActions resultActions = mockMvc.perform(put("/api/draft/update/{id}", draftArticle.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .principal(principal)
                .content(requestBody));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(updateContent)))
                .andExpect(jsonPath("$.data.version").value(version + 1))
                .andExpect(jsonPath("$.data.menuId").value(updateMenuId));
    }

    @Test
    @DisplayName("오래된 version으로 임시 저장글을 수정하면 충돌 응답을 반환한다.")
    void updateDraftArticleWithStaleVersion() throws Exception {
        // given
        final String title = "title";
        final String subTitle = "subTitle";
        final String content = "content";
        final String author = "admin";

        saveDraftArticles(title, subTitle, content, author);

        DraftArticle draftArticle = draftRepository.findAll().get(0);
        UpdateDraftRequest updatedArticle = new UpdateDraftRequest(-1L, title, subTitle,
                "stale content", menuId, null, null);
        String requestBody = objectMapper.writeValueAsString(updatedArticle);

        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn("username");

        // when
        ResultActions resultActions = mockMvc.perform(put("/api/draft/update/{id}", draftArticle.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .principal(principal)
                .content(requestBody));

        // then
        resultActions
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("E409"));
    }

    @ParameterizedTest
    @DisplayName("임시로 저장된 게시글 {id}을 삭제한다.")
    @CsvSource({"1", "2", "3", "4", "5"})
    void deleteTest(Long id) throws Exception {
        // given
        final String title = "title";
        final String subTitle = "subTitle";
        final String content = "content";
        final String author = "admin";

        saveDraftArticles(title, subTitle, content, author);

        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn("username");

        Long draftId = draftRepository.findAll().get(id.intValue() - 1).getId();

        // when
        ResultActions resultActions = mockMvc.perform(delete("/api/draft/delete/{id}", draftId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .principal(principal));

        // then
        resultActions
                .andExpect(status().isOk());

        assertThat(draftRepository.findById(draftId))
                .get()
                .extracting(DraftArticle::getDeleteYn)
                .isEqualTo(true);
        assertThat(draftRepository.findByAuthorAndDeleteYn(author, false))
                .extracting(DraftArticle::getId)
                .doesNotContain(draftId);
    }

    @Test
    @DisplayName("존재하지 않는 임시 저장글을 직접 삭제하면 실패 응답을 유지한다.")
    void deleteNotFoundDraftReturnsFailure() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn("username");

        ResultActions resultActions = mockMvc.perform(delete("/api/draft/delete/{id}", 999_999L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .principal(principal));

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    private List<DraftArticle> saveDraftArticles(String title, String subTitle, String content, String author) {
        List<DraftArticle> draftArticles = new ArrayList<>();
        final int length = 5;
        for (int i = 1; i <= length; i++) {
            DraftArticle draftArticle = draftRepository.save(DraftArticle.builder()
                    .title(title + i)
                    .subTitle(subTitle + i)
                    .content(content + i)
                    .menu(Menu.builder().id(menuId).build())
                    .author(author)
                    .thumbnailUrl(Thumbnail.empty())
                    .build());
            draftArticles.add(draftArticle);
        }
        return draftArticles;
    }
}
