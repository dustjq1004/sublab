package me.kimyeonsup.home.domain.menu.service.mapper;

import java.util.List;
import java.util.Map;
import me.kimyeonsup.home.domain.menu.domain.dto.AddMenuRequest;
import me.kimyeonsup.home.domain.menu.domain.dto.MenuResponse;
import me.kimyeonsup.home.domain.menu.domain.dto.MenusResponse;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.util.DateTimeFormat;
import org.springframework.stereotype.Component;

@Component
public class MenuMapper {

    public Menu toMenu(AddMenuRequest request, Category category) {
        return Menu.builder()
                .emoji(request.emoji())
                .name(request.name())
                .description(request.description())
                .category(category)
                .build();
    }

    public MenuResponse toCreatedResponse(Menu menu) {
        return commandResponseBuilder(menu).build();
    }

    public MenuResponse toDetailResponse(Menu menu, int articleCount) {
        return MenuResponse.builder()
                .id(menu.getId())
                .emoji(menu.getEmoji())
                .name(menu.getName())
                .description(menu.getDescription())
                .categoryId(menu.getCategory().getId())
                .categoryEmoji(menu.getCategory().getEmoji())
                .categoryName(menu.getCategory().getName())
                .createdAt(DateTimeFormat.diffDateFromNow(menu.getCreatedAt()))
                .updatedAt(DateTimeFormat.diffDateFromNow(menu.getUpdatedAt()))
                .articleCount(articleCount)
                .build();
    }

    public MenuResponse toUpdatedResponse(Menu menu) {
        return commandResponseBuilder(menu)
                .emoji(menu.getEmoji())
                .build();
    }

    public MenuResponse toDeletedResponse(Menu menu) {
        return commandResponseBuilder(menu).build();
    }

    public MenusResponse toListResponse(List<Menu> menuList, Map<Long, Integer> articleCountByMenuId) {
        List<MenuResponse> menus = menuList.stream()
                .map(menu -> toDetailResponse(menu, articleCountByMenuId.getOrDefault(menu.getId(), 0)))
                .toList();

        return MenusResponse.builder()
                .menus(menus)
                .build();
    }

    private MenuResponse.MenuResponseBuilder commandResponseBuilder(Menu menu) {
        return MenuResponse.builder()
                .id(menu.getId())
                .name(menu.getName())
                .categoryId(menu.getCategory().getId())
                .categoryEmoji(menu.getCategory().getEmoji())
                .categoryName(menu.getCategory().getName())
                .articleCount(0);
    }
}
