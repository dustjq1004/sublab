package me.kimyeonsup.home.domain.menu.service;

import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.domain.draft.application.port.in.DraftQueryUseCase;
import me.kimyeonsup.home.domain.menu.domain.dto.AddMenuRequest;
import me.kimyeonsup.home.domain.menu.domain.dto.MenuResponse;
import me.kimyeonsup.home.domain.menu.domain.dto.UpdateMenuRequest;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.menu.repository.CategoryRepository;
import me.kimyeonsup.home.domain.menu.repository.MenuRepository;
import me.kimyeonsup.home.domain.menu.service.mapper.MenuMapper;
import me.kimyeonsup.home.global.exception.business.ResourceConflictException;
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
    private final DraftQueryUseCase draftQueryUseCase;
    private final MessageSourceResolver messageSourceResolver;
    private final MenuMapper mapper;

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public MenuResponse save(AddMenuRequest request) {
        Category category = getCategoryOrThrow(request.categoryId());
        return mapper.toCreatedResponse(menuRepository.save(mapper.toMenu(request, category)));
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public MenuResponse delete(long id) {
        Menu menu = getMenuOrThrow(id);

        if (menu.hasArticles()) {
            throw new ResourceConflictException(messageSourceResolver.resolve(
                    MenuExceptionMessage.MENU_DELETE_HAS_ARTICLE
            ));
        }

        if (draftQueryUseCase.existsByMenuId(id)) {
            throw new ResourceConflictException(messageSourceResolver.resolve(
                    MenuExceptionMessage.MENU_DELETE_HAS_DRAFT
            ));
        }

        menuRepository.delete(menu);
        return mapper.toDeletedResponse(menu);
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public MenuResponse update(UpdateMenuRequest request) {
        long id = request.getId();
        Menu menu = getMenuOrThrow(id);
        Category category = getCategoryOrThrow(request.getCategoryId());

        menu.update(request.getName(), request.getDescription(), request.getEmoji(), category);
        return mapper.toUpdatedResponse(menu);
    }

    private Menu getMenuOrThrow(long id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        MenuExceptionMessage.MENU_NOT_FOUND, id
                )));
    }

    private Category getCategoryOrThrow(long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        CategoryExceptionMessage.CATEGORY_NOT_FOUND, id
                )));
    }

}
