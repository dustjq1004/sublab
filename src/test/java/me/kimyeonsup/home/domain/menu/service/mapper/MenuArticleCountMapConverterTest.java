package me.kimyeonsup.home.domain.menu.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import me.kimyeonsup.home.domain.article.application.port.out.MenuArticleCount;
import org.junit.jupiter.api.Test;

class MenuArticleCountMapConverterTest {

    private final MenuArticleCountMapConverter converter = new MenuArticleCountMapConverter();

    @Test
    void convertsNamedMenuIdsAndArticleCountsToMapEntries() {
        List<MenuArticleCount> articleCounts = List.of(
                new MenuArticleCount(11L, 3L),
                new MenuArticleCount(21L, 5L));

        Map<Long, Integer> articleCountByMenuId = converter.toArticleCountByMenuId(articleCounts);

        assertThat(articleCountByMenuId).containsExactlyInAnyOrderEntriesOf(Map.of(
                11L, 3,
                21L, 5));
    }

    @Test
    void convertsIntegerMaxValueWithoutLosingTheArticleCount() {
        List<MenuArticleCount> articleCounts = List.of(
                new MenuArticleCount(11L, Integer.MAX_VALUE));

        Map<Long, Integer> articleCountByMenuId = converter.toArticleCountByMenuId(articleCounts);

        assertThat(articleCountByMenuId).containsEntry(11L, Integer.MAX_VALUE);
    }

    @Test
    void rejectsArticleCountAboveIntegerMaxValueInsteadOfWrapping() {
        List<MenuArticleCount> articleCounts = List.of(
                new MenuArticleCount(11L, (long) Integer.MAX_VALUE + 1));

        assertThatThrownBy(() -> converter.toArticleCountByMenuId(articleCounts))
                .isInstanceOf(ArithmeticException.class);
    }
}
