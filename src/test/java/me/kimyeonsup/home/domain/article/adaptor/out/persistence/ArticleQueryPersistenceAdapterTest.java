package me.kimyeonsup.home.domain.article.adaptor.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import me.kimyeonsup.home.domain.article.application.port.in.command.ArticleSearchQuery;
import me.kimyeonsup.home.domain.article.domain.model.Article;
import me.kimyeonsup.home.domain.article.domain.model.Visibility;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

@DataJpaTest
@Import(ArticleQueryPersistenceAdapter.class)
class ArticleQueryPersistenceAdapterTest {

    @Autowired
    private ArticleQueryPersistenceAdapter adapter;

    @Autowired
    private TestEntityManager entityManager;

    private Long categoryId;
    private Long menuId;

    @BeforeEach
    void setUp() {
        Category category = entityManager.persist(Category.builder().name("language").build());
        Category otherCategory = entityManager.persist(Category.builder().name("database").build());
        Menu menu = entityManager.persist(Menu.builder().name("java").category(category).build());
        Menu siblingMenu = entityManager.persist(Menu.builder().name("kotlin").category(category).build());
        Menu otherMenu = entityManager.persist(Menu.builder().name("mysql").category(otherCategory).build());
        categoryId = category.getId();
        menuId = menu.getId();

        // 저장 순서와 제목 순서를 다르게 두어 정렬 적용 여부도 확인한다.
        save("spring gamma", menu, Visibility.PUBLIC);
        save("spring beta", menu, Visibility.PRIVATE);
        save("spring alpha", menu, Visibility.PUBLIC);
        save("spring delta", siblingMenu, Visibility.PUBLIC);
        save("spring epsilon", siblingMenu, Visibility.PRIVATE);
        save("spring zeta", otherMenu, Visibility.PUBLIC);
        save("spring eta", otherMenu, Visibility.PRIVATE);
        save("unrelated title", menu, Visibility.PUBLIC);
        entityManager.flush();
        entityManager.clear();
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Filter.class)
    @DisplayName("제목 검색과 선택한 필터를 모두 만족하는 게시글만 반환한다")
    void filtersArticles(Filter filter) {
        Page<Article> result = find(filter, query(filter, "spring", 0, 20, Sort.Direction.ASC));

        assertThat(result).as("미구현 메서드도 구현 후에는 Page를 반환해야 한다").isNotNull();
        assertThat(result.getContent()).extracting(Article::getTitle)
                .containsExactlyElementsOf(filter.expectedTitles);
        assertThat(result.getTotalElements()).isEqualTo(filter.expectedTitles.size());
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Filter.class)
    @DisplayName("검색 조건을 유지하면서 페이지 번호·크기·내림차순 정렬을 적용한다")
    void appliesPaginationAndSorting(Filter filter) {
        Page<Article> result = find(filter, query(filter, "spring", 1, 1, Sort.Direction.DESC));

        assertThat(result).isNotNull();
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(filter.expectedTitles.size());
        assertThat(result.getTotalPages()).isEqualTo(filter.expectedTitles.size());
        assertThat(result.getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "title"));
        assertThat(result.getContent()).extracting(Article::getTitle)
                .containsExactly(filter.expectedTitles.get(filter.expectedTitles.size() - 2));
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Filter.class)
    @DisplayName("검색 결과가 없으면 null 대신 페이지 정보를 가진 빈 Page를 반환한다")
    void returnsEmptyPage(Filter filter) {
        Page<Article> result = find(filter, query(filter, "missing", 0, 10, Sort.Direction.ASC));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(10);
    }

    private ArticleSearchQuery query(Filter filter, String keyword, int pageNumber, int pageSize,
                                     Sort.Direction direction) {
        return ArticleSearchQuery.builder()
                .menuId(filter == Filter.MENU || filter == Filter.MENU_VISIBILITY ? menuId : null)
                .categoryId(filter == Filter.CATEGORY || filter == Filter.CATEGORY_VISIBILITY ? categoryId : null)
                .visibility(filter.visibility)
                .searchKeyword(keyword)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .sortedBy("title")
                .direction(direction)
                .build();
    }

    private Page<Article> find(Filter filter, ArticleSearchQuery query) {
        return switch (filter) {
            case MENU -> adapter.findByMenu_IdAndTitleContaining(query);
            case MENU_VISIBILITY -> adapter.findByMenu_IdAndTitleContainingAndVisibility(query);
            case CATEGORY -> adapter.findByMenu_CategoryIdAndTitleContaining(query);
            case CATEGORY_VISIBILITY -> adapter.findByMenu_CategoryIdAndVisibilityAndTitleContaining(query);
            case VISIBILITY -> adapter.findByTitleContainsAndVisibility(query);
        };
    }

    private void save(String title, Menu menu, Visibility visibility) {
        entityManager.persist(Article.builder().title(title).content("body").author("author")
                .menu(menu).visibility(visibility.name()).build());
    }

    private enum Filter {
        MENU(null, List.of("spring alpha", "spring beta", "spring gamma")),
        MENU_VISIBILITY("PUBLIC", List.of("spring alpha", "spring gamma")),
        CATEGORY(null, List.of("spring alpha", "spring beta", "spring delta", "spring epsilon", "spring gamma")),
        CATEGORY_VISIBILITY("PRIVATE", List.of("spring beta", "spring epsilon")),
        VISIBILITY("PUBLIC", List.of("spring alpha", "spring delta", "spring gamma", "spring zeta"));

        private final String visibility;
        private final List<String> expectedTitles;

        Filter(String visibility, List<String> expectedTitles) {
            this.visibility = visibility;
            this.expectedTitles = expectedTitles;
        }
    }
}
