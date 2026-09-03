package me.kimyeonsup.home.domain.blog.admin.menu.service;

import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.dto.AddMenuRequest;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.dto.MenuResponse;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.dto.UpdateMenuRequest;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.blog.admin.menu.repository.CategoryRepository;
import me.kimyeonsup.home.domain.blog.admin.menu.repository.MenuRepository;
import me.kimyeonsup.home.global.exception.business.ResourceNotFoundException;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import me.kimyeonsup.home.global.exception.message.constants.CategoryExceptionMessage;
import me.kimyeonsup.home.global.exception.message.constants.MenuExceptionMessage;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final MessageSourceResolver messageSourceResolver;

    @CacheEvict(value = "categories", allEntries = true)
    public MenuResponse save(AddMenuRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        CategoryExceptionMessage.CATEGORY_NOT_FOUND, request.getCategoryId()
                )));
        return new MenuResponse(menuRepository.save(request.toEntity(category)));
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public MenuResponse delete(long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        MenuExceptionMessage.MENU_NOT_FOUND, id
                )));

        if (menu.hasArticles()) {
            throw new ResourceNotFoundException(messageSourceResolver.resolve(
                    MenuExceptionMessage.MENU_DELETE_HAS_ARTICLE
            ));
        }

        menuRepository.delete(menu);
        return new MenuResponse(menu);
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public MenuResponse update(UpdateMenuRequest request) {
        long id = request.getId();
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        MenuExceptionMessage.MENU_NOT_FOUND, id
                )));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        CategoryExceptionMessage.CATEGORY_NOT_FOUND, request.getCategoryId()
                )));

        menu.update(request, category);
        return new MenuResponse(menu);
    }

}
