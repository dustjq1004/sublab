package me.kimyeonsup.home.domain.menu.controller;

import static me.kimyeonsup.home.global.exception.message.constants.MenuExceptionMessage.MENU_DELETE_HAS_DRAFT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.article.adaptor.out.persistence.ArticleQueryJpaRepository;
import me.kimyeonsup.home.domain.draft.adaptor.out.persistence.DraftJpaRepository;
import me.kimyeonsup.home.domain.draft.domain.model.DraftArticle;
import me.kimyeonsup.home.domain.login.domain.entity.User;
import me.kimyeonsup.home.domain.menu.domain.dto.DeleteMenuRequest;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.menu.repository.CategoryRepository;
import me.kimyeonsup.home.domain.menu.repository.MenuRepository;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MenuDeleteDraftPolicyIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CategoryRepository categoryRepository;
    @Autowired MenuRepository menuRepository;
    @Autowired ArticleQueryJpaRepository articleRepository;
    @Autowired DraftJpaRepository draftRepository;
    @Autowired MessageSourceResolver messageSourceResolver;

    private final PrincipalDetail principalDetail = new PrincipalDetail(User.builder()
            .email("admin@example.com")
            .nickname("admin")
            .password("test")
            .role("ADMIN")
            .build());

    @BeforeEach
    void setUp() {
        draftRepository.deleteAll();
        articleRepository.deleteAll();
        menuRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void deletingMenuWithActiveDraftReturnsConflictAndPreservesRelationship() throws Exception {
        // DRAFT-HTTP-001
        assertDraftConflict(false);
    }

    @Test
    void deletingMenuWithLogicallyDeletedDraftReturnsConflictAndPreservesRelationship() throws Exception {
        // DRAFT-HTTP-002
        assertDraftConflict(true);
    }

    private void assertDraftConflict(boolean deleted) throws Exception {
        Category category = categoryRepository.save(Category.builder().name("language").build());
        Menu menu = menuRepository.save(Menu.builder()
                .name("java")
                .description("description")
                .emoji("M")
                .category(category)
                .build());
        DraftArticle draft = DraftArticle.builder()
                .title("draft")
                .content("body")
                .author("admin")
                .menu(menu)
                .build();
        if (deleted) {
            draft.delete();
        }
        draft = draftRepository.saveAndFlush(draft);
        Long draftId = draft.getId();

        mockMvc.perform(delete("/api/admin/menu/delete")
                        .with(user(principalDetail))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new DeleteMenuRequest(menu.getId()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("E409"))
                .andExpect(jsonPath("$.message").value(
                        "해당 메뉴에 임시저장글이 존재하는 경우 메뉴를 삭제할 수 없습니다."))
                .andExpect(jsonPath("$.message").value(messageSourceResolver.resolve(MENU_DELETE_HAS_DRAFT)))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.errors").value(nullValue()));

        assertThat(menuRepository.existsById(menu.getId())).isTrue();
        DraftArticle persistedDraft = draftRepository.findById(draftId).orElseThrow();
        assertThat(persistedDraft.getMenu().getId()).isEqualTo(menu.getId());
        assertThat(persistedDraft.getDeleteYn()).isEqualTo(deleted);
    }
}
