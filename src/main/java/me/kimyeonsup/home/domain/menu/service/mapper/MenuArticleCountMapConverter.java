package me.kimyeonsup.home.domain.menu.service.mapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import me.kimyeonsup.home.domain.article.application.port.out.MenuArticleCount;
import org.springframework.stereotype.Component;

@Component
public class MenuArticleCountMapConverter {

    public Map<Long, Integer> toArticleCountByMenuId(List<MenuArticleCount> articleCounts) {
        return articleCounts.stream()
                .collect(Collectors.toMap(
                        menuArticleCount -> {
                            long menuId = menuArticleCount.menuId();
                            return menuId;
                        },
                        menuArticleCount -> {
                            long articleCount = menuArticleCount.articleCount();
                            return Math.toIntExact(articleCount);
                        }));
    }
}
