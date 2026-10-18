package me.kimyeonsup.home.domain.article.adaptor.in.web.blog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.article.adaptor.in.web.blog.request.*;
import me.kimyeonsup.home.domain.article.adaptor.out.persistence.ArticleQueryJpaRepository;
import me.kimyeonsup.home.domain.article.domain.model.*;
import me.kimyeonsup.home.domain.draft.adaptor.out.persistence.DraftJpaRepository;
import me.kimyeonsup.home.domain.draft.domain.model.DraftArticle;
import me.kimyeonsup.home.domain.login.domain.entity.User;
import me.kimyeonsup.home.domain.menu.domain.entity.*;
import me.kimyeonsup.home.domain.menu.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BlogApiControllerTest {
    @Autowired jakarta.persistence.EntityManager entityManager;
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ArticleQueryJpaRepository articleRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired MenuRepository menuRepository;
    @Autowired DraftJpaRepository draftRepository;
    Menu menu;
    PrincipalDetail author = new PrincipalDetail(User.builder().email("author@example.com")
            .nickname("author").role("USER").password("pwd").build());

    @BeforeEach
    void setUp() {
        articleRepository.deleteAll();
        menu = menuRepository.save(Menu.builder().name("java")
                .category(categoryRepository.save(Category.builder().name("language").build())).build());
    }

    @Test
    @DisplayName("인식하지 못하는 공개 범위로 생성하면 null로 변환되고 DB 기본값이 적용된다")
    void unknownVisibilityUsesDatabaseDefault() throws Exception {
        var request = new AddArticleRequest("title", "", "body", menu.getId(), null, "", "", "SECRET");
        var response = mockMvc.perform(post("/api/articles/create").with(user(author)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.visibility").value(org.hamcrest.Matchers.nullValue()))
                .andReturn();
        long id = objectMapper.readTree(response.getResponse().getContentAsString()).path("data").path("id").asLong();

        entityManager.flush();
        entityManager.clear();
        assertThat(articleRepository.findById(id).orElseThrow().getVisibility()).isEqualTo(Visibility.PRIVATE);
    }

    @Test
    @DisplayName("생성 API는 인증된 작성자와 썸네일을 저장한다")
    void createArticle() throws Exception {
        var request = new AddArticleRequest("title", "subtitle", "body", menu.getId(), null,
                "https://example.com/thumb.png", "thumb.png", "PUBLIC");
        mockMvc.perform(post("/api/articles/create").with(user(author)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("title"))
                .andExpect(jsonPath("$.data.author").value("author"));
        assertThat(articleRepository.findAll()).singleElement().satisfies(article -> {
            assertThat(article.getAuthor()).isEqualTo("author");
            assertThat(article.getThumbnailLogicalFileName()).isEqualTo("thumb.png");
        });
    }

    @Test
    @DisplayName("발행 성공 시 연결된 임시저장을 삭제한다")
    void publishDeletesDraft() throws Exception {
        var draft = draftRepository.save(DraftArticle.builder()
                .title("draft").subTitle("").content("body").menu(menu).author(author.getName())
                .thumbnailUrl(Thumbnail.empty()).build());
        var request = new AddArticleRequest("published", "", "body", menu.getId(), draft.getId(), "", "", "PUBLIC");
        mockMvc.perform(post("/api/articles/create").with(user(author)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        assertThat(articleRepository.findAll()).hasSize(1);
        assertThat(draftRepository.findById(draft.getId())).isEmpty();
    }

    @Test
    void publishWithAlreadyDeletedDraft() throws Exception {
        var request = new AddArticleRequest("published", "", "body", menu.getId(), Long.MAX_VALUE, "", "", "PUBLIC");
        mockMvc.perform(post("/api/articles/create").with(user(author)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        assertThat(articleRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("상세 및 마크다운 조회는 저장된 원문을 반환한다")
    void findArticleAndMarkdown() throws Exception {
        Article saved = save("PUBLIC");
        for (String path : new String[]{"/api/articles/{id}", "/api/articles/{id}/markdown"}) {
            mockMvc.perform(get(path, saved.getId()).with(user(author)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.data.content").value("# body"))
                    .andExpect(jsonPath("$.data.menuName").value("java"));
        }
    }

    @Test
    void privateArticleRequiresAuthor() throws Exception {
        Article saved = save("PRIVATE");
        mockMvc.perform(get("/api/articles/{id}", saved.getId())).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/articles/{id}", saved.getId()).with(user(author)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.visibility").value("PRIVATE"));
    }

    @Test
    void findAllPagination() throws Exception {
        save("PUBLIC");
        mockMvc.perform(get("/api/articles").param("menuName", "java").param("searchKeyword", "title"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.pageNumber").value(0))
                .andExpect(jsonPath("$.data.articles[0].title").value("title"));
    }

    @Test
    @DisplayName("공개 목록에는 비공개 게시글을 포함하지 않는다")
    void publicArticleListExcludesPrivateArticles() throws Exception {
        save("PUBLIC");
        save("PRIVATE");

        mockMvc.perform(get("/api/articles").param("menuName", "java").param("searchKeyword", "title"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.articles.length()").value(1))
                .andExpect(jsonPath("$.data.articles[0].visibility").value("PUBLIC"));
    }

    @Test
    void updateArticle() throws Exception {
        Article saved = save("PUBLIC");
        var request = new UpdateArticleRequest(saved.getId(), "changed", "sub", "changed body", menu.getId(), "", "", "PRIVATE");
        mockMvc.perform(put("/api/articles/update").with(user(author)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.visibility").value("PRIVATE"));
        Article updated = articleRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("changed");
        assertThat(updated.getContent()).isEqualTo("changed body");
    }

    @Test
    void deleteArticle() throws Exception {
        Article saved = save("PUBLIC");
        mockMvc.perform(delete("/api/articles/delete").param("id", saved.getId().toString())
                        .with(user(author)).with(csrf())).andExpect(status().isOk());
        assertThat(articleRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void otherAuthorCannotUpdateOrDelete() throws Exception {
        Article saved = save("PUBLIC");
        PrincipalDetail other = new PrincipalDetail(User.builder().email("other@example.com").nickname("other").role("USER").build());
        var request = new UpdateArticleRequest(saved.getId(), "changed", "", "body", menu.getId(), "", "", "PUBLIC");
        mockMvc.perform(put("/api/articles/update").with(user(other)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/articles/delete").param("id", saved.getId().toString())
                        .with(user(other)).with(csrf())).andExpect(status().isNotFound());
        assertThat(articleRepository.findById(saved.getId())).isPresent();
        assertThat(saved.getTitle()).isEqualTo("title");
    }

    @Test
    void previousAndNextArticle() {
        Article first = save("PUBLIC");
        Article middle = save("PUBLIC");
        Article last = save("PUBLIC");
        var result = articleRepository.findPrevNextArticle(middle.getId(), "java");
        assertThat(result.getPrevId()).isEqualTo(first.getId());
        assertThat(result.getNextId()).isEqualTo(last.getId());
    }

    private Article save(String visibility) {
        return articleRepository.save(Article.builder().title("title").content("# body")
                .author(author.getName()).menu(menu).visibility(visibility).build());
    }
}
