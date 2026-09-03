package me.kimyeonsup.home.domain.blog.admin.menu.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.dto.CategoryResponse;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.blog.admin.menu.repository.CategoryRepository;
import me.kimyeonsup.home.domain.blog.article.repository.ArticleRepository;
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
    private final ArticleRepository articleRepository;
    private final MessageSourceResolver messageSourceResolver;

    public CategoryResponse findById(long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        CategoryExceptionMessage.CATEGORY_NOT_FOUND, id
                )));
        return CategoryResponse.builder()
                .id(category.getId())
                .emoji(category.getEmoji())
                .name(category.getName())
                .build();
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public List<Category> findAllWithMenus() {
        return categoryRepository.findAllWithMenus();
    }

    @Cacheable("categories")
    public List<CategoryResponse> findAllWithMenusAsResponse() {
        Map<Long, Integer> articleCountMap = articleRepository.countByMenuId().stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> ((Long) r[1]).intValue()));
        return findAllWithMenus().stream()
                .map(category -> new CategoryResponse(category, articleCountMap))
                .toList();
    }
}
