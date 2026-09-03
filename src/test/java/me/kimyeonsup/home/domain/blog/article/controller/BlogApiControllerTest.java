package me.kimyeonsup.home.domain.blog.article.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.blog.admin.menu.repository.CategoryRepository;
import me.kimyeonsup.home.domain.blog.admin.menu.repository.MenuRepository;
import me.kimyeonsup.home.domain.blog.article.domain.dto.AddArticleRequest;
import me.kimyeonsup.home.domain.blog.article.domain.dto.ArticlePrevNextDto;
import me.kimyeonsup.home.domain.blog.article.domain.dto.UpdateArticleRequest;
import me.kimyeonsup.home.domain.blog.article.domain.entity.Article;
import me.kimyeonsup.home.domain.blog.article.domain.vo.Visibility;
import me.kimyeonsup.home.domain.blog.article.repository.ArticleRepository;
import me.kimyeonsup.home.domain.blog.draft.domain.dto.AddDraftArticle;
import me.kimyeonsup.home.domain.blog.draft.domain.dto.UpdateDraftArticle;
import me.kimyeonsup.home.domain.blog.draft.domain.entity.DraftArticle;
import me.kimyeonsup.home.domain.blog.draft.repository.DraftRepository;
import me.kimyeonsup.home.domain.blog.draft.service.DraftService;
import me.kimyeonsup.home.login.domain.entity.User;
import me.kimyeonsup.home.login.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
class BlogApiControllerTest {

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

    @Autowired
    DraftRepository draftRepository;

    @Autowired
    DraftService draftService;

    PrincipalDetail principalDetail;
    Menu menu;
    Category category;

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
        draftRepository.deleteAll();
    }

    @DisplayName("addArticle: 블로그 글 추가에 성공한다.")
    @Test
    public void addArticle() throws Exception {
        // given
        final String url = "/api/articles";
        final String title = "title";
        final String content = "content";
        final AddArticleRequest userRequest = new AddArticleRequest(title, "", content, menu.getId(), null, null, null, "PUBLIC");
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
        assertThat(articles.get(0).getTitle()).isEqualTo(title);
        assertThat(articles.get(0).getContent()).isEqualTo(content);
    }

    @DisplayName("addArticle: 썸네일 logical file name을 함께 저장한다.")
    @Test
    void addArticleWithThumbnailLogicalFileName() throws Exception {
        final String url = "/api/articles";
        final String thumbnailUrl = "https://example.com/thumb/test-thumb.png";
        final String thumbnailLogicalFileName = "test-thumb.png";
        final AddArticleRequest userRequest = new AddArticleRequest(
                "title",
                "",
                "content",
                menu.getId(),
                null,
                thumbnailUrl,
                thumbnailLogicalFileName,
                "PUBLIC"
        );

        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn("username");

        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .principal(principal)
                .content(objectMapper.writeValueAsString(userRequest)));

        result.andExpect(status().isOk());

        List<Article> articles = articleRepository.findAll();
        assertThat(articles).hasSize(1);
        assertThat(articles.get(0).getThumbnailUrl()).isEqualTo(thumbnailUrl);
        assertThat(articles.get(0).getThumbnailLogicalFileName()).isEqualTo(thumbnailLogicalFileName);
    }

    @DisplayName("addArticle: draftId가 존재하면 게시글 생성 후 draft를 정리한다.")
    @Test
    void addArticleDeletesDraftAfterPublish() throws Exception {
        DraftArticle draftArticle = createDraftArticle();
        AddArticleRequest userRequest = new AddArticleRequest(
                "published title",
                "",
                "published content",
                menu.getId(),
                draftArticle.getId(),
                null,
                null,
                "PUBLIC"
        );

        ResultActions result = mockMvc.perform(post("/api/articles")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(userRequest)));

        result.andExpect(status().isOk());

        List<Article> articles = articleRepository.findAll();
        assertThat(articles).hasSize(1);
        assertThat(draftRepository.findByAuthorAndDeleteYn(principalDetail.getName(), false)).isEmpty();
        assertThat(draftRepository.findById(draftArticle.getId()))
                .get()
                .extracting(DraftArticle::getDeleteYn)
                .isEqualTo(true);
    }

    @DisplayName("addArticle: draft가 이미 삭제된 상태여도 게시글 생성 API는 성공한다.")
    @Test
    void addArticleSucceedsWhenDraftAlreadyDeleted() throws Exception {
        AddArticleRequest userRequest = new AddArticleRequest(
                "published title",
                "",
                "published content",
                menu.getId(),
                999_999L,
                null,
                null,
                "PUBLIC"
        );

        ResultActions result = mockMvc.perform(post("/api/articles")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(userRequest)));

        result.andExpect(status().isOk());

        List<Article> articles = articleRepository.findAll();
        assertThat(articles).hasSize(1);
        assertThat(articles.get(0).getTitle()).isEqualTo("published title");
    }

    @DisplayName("addArticle: 같은 draftId로 draft 수정과 게시글 생성이 겹쳐도 게시글 생성은 성공한다.")
    @Test
    void addArticleSucceedsWhenDraftUpdateAndPublishOverlap() throws Exception {
        DraftArticle draftArticle = createDraftArticle();
        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<?> updateFuture = executorService.submit(() -> {
            try {
                startLatch.await(3, TimeUnit.SECONDS);
                draftService.update(new UpdateDraftArticle(
                        draftArticle.getId(),
                        draftArticle.getVersion(),
                        "updated draft title",
                        "updated draft subtitle",
                        "updated draft content",
                        menu.getId(),
                        null,
                        null
                ));
            } catch (Exception ignored) {
                // 동시 요청에서 update는 충돌/정리 상태에 따라 실패할 수 있다. 이 테스트의 보장 대상은 발행 성공이다.
            }
        });

        AddArticleRequest userRequest = new AddArticleRequest(
                "published title",
                "",
                "published content",
                menu.getId(),
                draftArticle.getId(),
                null,
                null,
                "PUBLIC"
        );

        startLatch.countDown();
        ResultActions result = mockMvc.perform(post("/api/articles")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(userRequest)));

        result.andExpect(status().isOk());
        updateFuture.get(3, TimeUnit.SECONDS);
        executorService.shutdownNow();

        assertThat(articleRepository.findAll())
                .extracting(Article::getTitle)
                .contains("published title");
    }

    @DisplayName("findAllArticles : 블로그 글 목록 조회에 성공한다.")
    @Test
    public void findAllArticle() throws Exception {
        // given
        final String url = "/blog";
        Article savedArticle = createDefaultArticle();

        // when
        final ResultActions resultActions = mockMvc.perform(get(url)
                .accept(MediaType.TEXT_HTML));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(savedArticle.getContent())))
                .andExpect(content().string(containsString(savedArticle.getTitle())));

    }

    @DisplayName("findAllArticles : 블로그 페이지네이션 조회에 성공한다.")
    @Test
    public void findAllArticlePagination() throws Exception {
        // given
        final String url = "/api/articles?pageNumber=0";
        List<Article> articles = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            articles.add(createDefaultArticle());
        }

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON_VALUE));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpectAll(jsonPath("$.data.totalCount").value(articles.size()));

    }

    @DisplayName("findArticle : 블로그 글 조회에 성공한다.")
    @Test
    public void findArticle() throws Exception {
        // given
        final String url = "/api/articles/{id}";
        Article savedArticle = createDefaultArticle();

        // when
        final ResultActions resultActions = mockMvc.perform(get(url, savedArticle.getId()));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("<p>" + savedArticle.getContent() + "</p>"))
                .andExpect(jsonPath("$.data.title").value(savedArticle.getTitle()));
    }

    @DisplayName("findArticleView: 비로그인 사용자는 상세 페이지 상태 뱃지를 볼 수 없다.")
    @Test
    void findArticleViewAnonymousDoesNotShowStatusBadge() throws Exception {
        Article article = createArticle("public detail content", Visibility.PUBLIC, principalDetail.getName());
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/blog/{menuName}/{id}", menu.getName(), article.getId())
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("public detail content")))
                .andExpect(content().string(not(containsString("article-status-badge"))));
    }

    @DisplayName("findArticleView: 로그인 사용자는 상세 페이지에서 공개 상태 뱃지를 볼 수 없다.")
    @Test
    void findArticleViewAuthenticatedDoesNotShowPublicStatusBadge() throws Exception {
        Article article = createArticle("authenticated detail content", Visibility.PUBLIC, principalDetail.getName());

        mockMvc.perform(get("/blog/{menuName}/{id}", menu.getName(), article.getId())
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("authenticated detail content")))
                .andExpect(content().string(not(containsString("article-status-badge-public"))))
                .andExpect(content().string(not(containsString(">공개</span>"))));
    }

    @DisplayName("findArticleView: 권한 없는 사용자는 비공개 글 본문 대신 안내 UI를 본다.")
    @Test
    void findArticleViewUnauthorizedUserSeesRestrictedState() throws Exception {
        Article article = createArticle("private detail content", Visibility.PRIVATE, principalDetail.getName());
        PrincipalDetail otherUser = new PrincipalDetail(User.builder()
                .email("other-user@gmail.com")
                .password("test")
                .role("관리자")
                .build());
        setAuthentication(otherUser);

        mockMvc.perform(get("/blog/{menuName}/{id}", menu.getName(), article.getId())
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("private detail content"))))
                .andExpect(content().string(containsString("비공개 글입니다")))
                .andExpect(content().string(containsString("권한이 있는 사용자만 볼 수 있습니다")));
    }

    @DisplayName("deleteArticle: 블로그 글 삭제에 성공한다.")
    @Test
    public void deleteArticle() throws Exception {
        // given
        final String url = "/api/articles/{id}";
        Article savedArticle = createDefaultArticle();

        // when
        mockMvc.perform(delete(url, savedArticle.getId()))
                .andExpect(status().isOk());

        // then
        List<Article> articles = articleRepository.findAll();

        assertThat(articles).isEmpty();
    }

    @DisplayName("updateArticle: 블로그 글 수정에 성공한다.")
    @Test
    public void updateArticle() throws Exception {
        // given
        final String url = "/api/articles/{id}";
        Article savedArticle = createDefaultArticle();

        final String newTitle = "new title";
        final String newSubTitle = "sub Title";
        final String newContent = "new content";

        UpdateArticleRequest request = new UpdateArticleRequest(newTitle, newSubTitle, newContent, menu.getId(), null, null, "PUBLIC");

        // when
        ResultActions result = mockMvc.perform(put(url, savedArticle.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk());

        Article article = articleRepository.findById(savedArticle.getId()).get();

        assertThat(article.getTitle()).isEqualTo(newTitle);
        assertThat(article.getContent()).isEqualTo(newContent);
    }

    @DisplayName("이전글 다음글을 조회한다.")
    @Test
    void getPrevNextArticle() {

        String menuName = "자바";
        createDefaultArticle();
        createDefaultArticle();
        createDefaultArticle();

        List<Article> all = articleRepository.findAll();
        Long id = all.get(1).getId();

        ArticlePrevNextDto prevNextArticle = articleRepository.findPrevNextArticle(id, menuName);

        assertThat(all.size()).isEqualTo(3);
        assertThat(prevNextArticle.getId()).isEqualTo(id);
        assertThat(prevNextArticle.getNextId()).isEqualTo(id + 1);
        assertThat(prevNextArticle.getPrevTitle()).isEqualTo("title");
        assertThat(prevNextArticle.getPrevId()).isEqualTo(id - 1);
        assertThat(prevNextArticle.getNextTitle()).isEqualTo("title");
    }

    @Disabled
    @DisplayName("블로그 글 추가시 썸네일 이미지도 함께 DB에 저장한다.")
    @ParameterizedTest
    @CsvSource({"![](https://urlimage.com/images/image.png), https://urlimage.com/images/image.png",
            "![image](https://urlimage.com/images/image.png), https://urlimage.com/images/image.png"})
    void saveThumbnailImageUrl(String content, String imageUrl) throws Exception {
        // given
        final String url = "/api/articles";
        final String title = "title";
        final AddArticleRequest userRequest = new AddArticleRequest(title, "", content, menu.getId(), null, null, null, "PUBLIC");
        final String requestBody = objectMapper.writeValueAsString(userRequest);

        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn("username");

        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .principal(principal)
                .content(requestBody));

        // then
        result.andExpect(status().isCreated());

        List<Article> articles = articleRepository.findAll();

        assertThat(articles.size()).isEqualTo(1);
        assertThat(articles.get(0).getThumbnailUrl()).isNotBlank();
        assertThat(articles.get(0).getThumbnailUrl()).isEqualTo(imageUrl);
    }

    private Article createDefaultArticle() {
        return createArticle("content", Visibility.PRIVATE, principalDetail.getName());
    }

    private Article createArticle(String content, Visibility visibility, String author) {
        return articleRepository.save(Article.builder()
                .title("title")
                .author(author)
                .content(content)
                .menu(menu)
                .visibility(visibility.name())
                .build());
    }

    private DraftArticle createDraftArticle() {
        return draftRepository.save(new AddDraftArticle(
                null,
                "draft title",
                "draft subtitle",
                "draft content",
                menu.getId(),
                null,
                null
        ).toEntity(principalDetail.getName()));
    }

    private void setAuthentication(PrincipalDetail userDetail) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add((GrantedAuthority) () -> userDetail.getAuthorities().iterator().next().getAuthority());

        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userDetail, authorities, "GM"));
    }
}
