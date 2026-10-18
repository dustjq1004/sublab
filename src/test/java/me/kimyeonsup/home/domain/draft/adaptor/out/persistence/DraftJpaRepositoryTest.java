package me.kimyeonsup.home.domain.draft.adaptor.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import me.kimyeonsup.home.domain.draft.domain.model.DraftArticle;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class DraftJpaRepositoryTest {

    @Autowired DraftJpaRepository draftRepository;
    @Autowired TestEntityManager entityManager;

    @Test
    void existsByMenuIdReturnsTrueForActiveDraft() {
        // DRAFT-QUERY-001
        Menu menu = persistMenu("active-menu");
        persistDraft(menu, false);

        entityManager.flush();
        entityManager.clear();

        assertThat(draftRepository.existsByMenuId(menu.getId())).isTrue();
    }

    @Test
    void existsByMenuIdReturnsTrueForLogicallyDeletedDraft() {
        // DRAFT-QUERY-002
        Menu menu = persistMenu("deleted-menu");
        persistDraft(menu, true);

        entityManager.flush();
        entityManager.clear();

        assertThat(draftRepository.existsByMenuId(menu.getId())).isTrue();
    }

    @Test
    void existsByMenuIdReturnsFalseWithoutDraftForThatMenu() {
        // DRAFT-QUERY-002
        Menu referencedMenu = persistMenu("referenced-menu");
        Menu otherMenu = persistMenu("other-menu");
        persistDraft(referencedMenu, false);

        entityManager.flush();
        entityManager.clear();

        assertThat(draftRepository.existsByMenuId(otherMenu.getId())).isFalse();
    }

    private Menu persistMenu(String name) {
        Category category = entityManager.persist(Category.builder().name(name + "-category").build());
        return entityManager.persist(Menu.builder()
                .name(name)
                .description("description")
                .emoji("M")
                .category(category)
                .build());
    }

    private DraftArticle persistDraft(Menu menu, boolean deleted) {
        DraftArticle draft = DraftArticle.builder()
                .title("draft")
                .content("body")
                .author("admin")
                .menu(menu)
                .build();
        if (deleted) {
            draft.delete();
        }
        return entityManager.persist(draft);
    }
}
