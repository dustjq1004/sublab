package me.kimyeonsup.home.domain.blog.admin.menu.service;

import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.dto.AddCategoryRequest;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.dto.CategoryResponse;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.dto.UpdateCategoryRequest;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.blog.admin.menu.repository.CategoryRepository;
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

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse save(AddCategoryRequest request) {
        return new CategoryResponse(categoryRepository.save(request.toEntity()));
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void delete(long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        CategoryExceptionMessage.CATEGORY_NOT_FOUND, id
                )));

        if (category.hasMenus()) {
            throw new ResourceNotFoundException(messageSourceResolver.resolve(
                    CategoryExceptionMessage.CATEGORY_HAS_MENUS
            ));
        }

        categoryRepository.delete(category);
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse update(UpdateCategoryRequest request) {
        long id = request.getId();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        CategoryExceptionMessage.CATEGORY_NOT_FOUND, id
                )));
        category.update(request.getName());
        return CategoryResponse.builder()
                .id(id)
                .emoji(category.getEmoji())
                .name(category.getName())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
