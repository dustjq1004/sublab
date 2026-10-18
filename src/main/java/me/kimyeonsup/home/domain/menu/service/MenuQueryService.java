package me.kimyeonsup.home.domain.menu.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.domain.article.application.port.out.ArticleQueryRepository;
import me.kimyeonsup.home.domain.article.application.port.out.MenuArticleCount;
import me.kimyeonsup.home.domain.menu.domain.dto.MenuResponse;
import me.kimyeonsup.home.domain.menu.domain.dto.MenusResponse;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.menu.repository.MenuRepository;
import me.kimyeonsup.home.domain.menu.service.mapper.MenuArticleCountMapConverter;
import me.kimyeonsup.home.domain.menu.service.mapper.MenuMapper;
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
    private final ArticleQueryRepository articleQueryRepository;
    private final MessageSourceResolver messageSourceResolver;
    private final MenuMapper mapper;
    private final MenuArticleCountMapConverter articleCountConverter;

    public MenusResponse findAll() {
        return mapper.toListResponse(menuRepository.findAllWithCategory(), articleCountByMenuId());
    }

    public MenusResponse findByCategory(Long categoryId) {
        List<Menu> menus = menuRepository.findByCategoryIdWithCategory(categoryId);
        return mapper.toListResponse(menus, articleCountByMenuId());
    }

    public MenuResponse findById(long menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        MenuExceptionMessage.MENU_NOT_FOUND, menuId
                )));
        return mapper.toDetailResponse(menu, menu.getArticles().size());
    }

    private Map<Long, Integer> articleCountByMenuId() {
        List<MenuArticleCount> articleCounts = articleQueryRepository.countByMenuId();
        return articleCountConverter.toArticleCountByMenuId(articleCounts);
    }
}
