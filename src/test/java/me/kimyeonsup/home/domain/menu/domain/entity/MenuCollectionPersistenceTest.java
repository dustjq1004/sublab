package me.kimyeonsup.home.domain.menu.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class MenuCollectionPersistenceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void persistsAndReloadsCategoryWithEmptyMenus() {
        Category category = entityManager.persist(Category.builder().name("empty category").build());
        Long categoryId = category.getId();
        entityManager.flush();
        entityManager.clear();

        Category reloaded = entityManager.find(Category.class, categoryId);

        assertThat(reloaded.getMenus()).isNotNull().isEmpty();
        assertThat(reloaded.hasMenus()).isFalse();
    }

    @Test
    void persistsAndReloadsMenuWithEmptyArticlesAndCategory() {
        Category category = entityManager.persist(Category.builder().name("language").build());
        Menu menu = entityManager.persist(Menu.builder().name("java").category(category).build());
        Long categoryId = category.getId();
        Long menuId = menu.getId();
        entityManager.flush();
        entityManager.clear();

        Menu reloaded = entityManager.find(Menu.class, menuId);

        assertThat(reloaded.getArticles()).isNotNull().isEmpty();
        assertThat(reloaded.hasArticles()).isFalse();
        assertThat(reloaded.getCategory().getId()).isEqualTo(categoryId);
    }
}
