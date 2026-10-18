package me.kimyeonsup.home.domain.menu.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.domain.article.application.port.out.ArticleQueryRepository;
import me.kimyeonsup.home.domain.article.application.port.out.MenuArticleCount;
import me.kimyeonsup.home.domain.menu.domain.dto.CategoriesResponse;
import me.kimyeonsup.home.domain.menu.domain.dto.CategoryResponse;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.menu.repository.CategoryRepository;
import me.kimyeonsup.home.domain.menu.service.mapper.CategoryMapper;
import me.kimyeonsup.home.domain.menu.service.mapper.MenuArticleCountMapConverter;
import me.kimyeonsup.home.global.exception.business.ResourceNotFoundException;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import me.kimyeonsup.home.global.exception.message.constants.CategoryExceptionMessage;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryQueryService {

    private final CategoryRepository categoryRepository;
    private final ArticleQueryRepository articleQueryRepository;
    private final MessageSourceResolver messageSourceResolver;
    private final CategoryMapper mapper;
    private final MenuArticleCountMapConverter articleCountConverter;

    public CategoryResponse findById(long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        CategoryExceptionMessage.CATEGORY_NOT_FOUND, id
                )));
        return mapper.toDetailResponse(category);
    }

    public CategoriesResponse findAll() {
        List<CategoryResponse> categories = categoryRepository.findAll().stream()
                .map(mapper::toListItemResponse)
                .toList();
        return new CategoriesResponse(categories);
    }

    public List<Category> findAllWithMenus() {
        return categoryRepository.findAllWithMenus();
    }

    @Cacheable("categories")
    public List<CategoryResponse> findNavigationCategories() {
        List<MenuArticleCount> articleCounts = articleQueryRepository.countByMenuId();
        Map<Long, Integer> articleCountMap = articleCountConverter.toArticleCountByMenuId(articleCounts);
        return findAllWithMenus().stream()
                .map(category -> mapper.toNavigationResponse(category, articleCountMap))
                .toList();
    }
}
