package me.kimyeonsup.home.domain.blog.admin.menu.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.dto.MenuResponse;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.dto.MenusResponse;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.blog.admin.menu.repository.MenuRepository;
import me.kimyeonsup.home.domain.blog.article.repository.ArticleRepository;
import me.kimyeonsup.home.global.exception.business.ResourceNotFoundException;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import me.kimyeonsup.home.global.exception.message.constants.MenuExceptionMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuQueryService {

    private final MenuRepository menuRepository;
    private final ArticleRepository articleRepository;
    private final MessageSourceResolver messageSourceResolver;

    public MenusResponse findAll() {
        Map<Long, Long> articleCountMap = articleRepository.countByMenuId().stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));
        List<MenuResponse> menus = menuRepository.findAllWithCategory().stream().map(menu ->
                MenuResponse.builder()
                        .id(menu.getId())
                        .emoji(menu.getEmoji())
                        .name(menu.getName())
                        .description(menu.getDescription())
                        .updatedAt(menu.getUpdatedAt())
                        .articleCount(articleCountMap.getOrDefault(menu.getId(), 0L).intValue())
                        .build()
        ).toList();
        return new MenusResponse(menus);
    }

    public MenusResponse findByCategory(Long categoryId) {
        Map<Long, Long> articleCountMap = articleRepository.countByMenuId().stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));
        List<Menu> menus = menuRepository.findByCategoryId(categoryId);
        return MenusResponse.builder()
                .menus(menus.stream().map(menu -> MenuResponse.builder()
                                .id(menu.getId())
                                .emoji(menu.getEmoji())
                                .name(menu.getName())
                                .description(menu.getDescription())
                                .categoryId(menu.getCategory().getId())
                                .createdAt(menu.getCreatedAt())
                                .updatedAt(menu.getUpdatedAt())
                                .articleCount(articleCountMap.getOrDefault(menu.getId(), 0L).intValue())
                                .build())
                        .toList())
                .build();
    }

    public MenuResponse findById(long menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        MenuExceptionMessage.MENU_NOT_FOUND, menuId
                )));
        return MenuResponse.builder()
                .id(menu.getId())
                .emoji(menu.getEmoji())
                .name(menu.getName())
                .description(menu.getDescription())
                .categoryId(menu.getCategory().getId())
                .categoryEmoji(menu.getCategory().getEmoji())
                .categoryName(menu.getCategory().getName())
                .createdAt(menu.getCreatedAt())
                .updatedAt(menu.getUpdatedAt())
                .build();
    }

    public MenuResponse findByName(String menuName) {
        Menu menu = menuRepository.findByName(menuName)
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        MenuExceptionMessage.MENU_NOT_FOUND, new Object[]{menuName}
                )));
        return MenuResponse.builder()
                .id(menu.getId())
                .emoji(menu.getEmoji())
                .name(menu.getName())
                .description(menu.getDescription())
                .createdAt(menu.getCreatedAt())
                .updatedAt(menu.getUpdatedAt())
                .build();
    }
}
