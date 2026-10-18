package me.kimyeonsup.home.domain.menu.service;

import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.domain.menu.domain.dto.AddCategoryRequest;
import me.kimyeonsup.home.domain.menu.domain.dto.CategoryResponse;
import me.kimyeonsup.home.domain.menu.domain.dto.UpdateCategoryRequest;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.menu.repository.CategoryRepository;
import me.kimyeonsup.home.domain.menu.service.mapper.CategoryMapper;
import me.kimyeonsup.home.global.exception.business.ResourceConflictException;
import me.kimyeonsup.home.global.exception.business.ResourceNotFoundException;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import me.kimyeonsup.home.global.exception.message.constants.CategoryExceptionMessage;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final MessageSourceResolver messageSourceResolver;
    private final CategoryMapper mapper;

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse save(AddCategoryRequest request) {
        return mapper.toCreatedResponse(categoryRepository.save(mapper.toCategory(request)));
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void delete(long id) {
        Category category = getCategoryOrThrow(id);

        if (category.hasMenus()) {
            throw new ResourceConflictException(messageSourceResolver.resolve(
                    CategoryExceptionMessage.CATEGORY_HAS_MENUS
            ));
        }

        categoryRepository.delete(category);
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse update(UpdateCategoryRequest request) {
        long id = request.getId();
        Category category = getCategoryOrThrow(id);
        category.update(request.getName());
        return mapper.toUpdatedResponse(category);
    }

    private Category getCategoryOrThrow(long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        CategoryExceptionMessage.CATEGORY_NOT_FOUND, id
                )));
    }
}
