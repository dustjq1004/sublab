package me.kimyeonsup.home.domain.article.adaptor.in.web.blog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.article.adaptor.in.web.blog.request.AddArticleRequest;
import me.kimyeonsup.home.domain.article.adaptor.out.persistence.ArticleQueryJpaRepository;
import me.kimyeonsup.home.domain.login.domain.entity.User;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.menu.repository.CategoryRepository;
import me.kimyeonsup.home.domain.menu.repository.MenuRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AddArticleFailTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ArticleQueryJpaRepository articleRepository;
    @Autowired MenuRepository menuRepository;
    @Autowired CategoryRepository categoryRepository;
    PrincipalDetail author = new PrincipalDetail(User.builder().email("author@example.com")
            .nickname("author").role("USER").build());

    @ParameterizedTest
    @ValueSource(strings = {"blankTitle", "longTitle", "longSubtitle", "missingMenu", "blankVisibility"})
    void invalidRequestDoesNotSave(String kind) throws Exception {
        long before = articleRepository.count();
        var request = new AddArticleRequest(kind.equals("blankTitle") ? " " : kind.equals("longTitle") ? "a".repeat(101) : "title",
                kind.equals("longSubtitle") ? "a".repeat(101) : "", "body", kind.equals("missingMenu") ? null : 1L,
                null, "", "", kind.equals("blankVisibility") ? " " : "PUBLIC");
        mockMvc.perform(post("/api/articles/create").with(user(author)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false));
        assertThat(articleRepository.count()).isEqualTo(before);
    }

    @Test
    void omittedThumbnailUsesDefaultImage() throws Exception {
        Menu menu = menuRepository.save(Menu.builder().name("thumbnail-test").category(categoryRepository.save(Category.builder().name("thumbnail-category").build())).build());
        var request = new AddArticleRequest("title", "", "body", menu.getId(), null, null, null, "PUBLIC");
        mockMvc.perform(post("/api/articles/create").with(user(author)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.thumbnailUrl").value(org.hamcrest.Matchers.startsWith("/img/noimage")));
    }
}
