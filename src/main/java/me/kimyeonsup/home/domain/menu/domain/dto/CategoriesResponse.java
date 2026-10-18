package me.kimyeonsup.home.domain.menu.domain.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class CategoriesResponse {

    private List<CategoryResponse> categories;

    public CategoriesResponse(List<CategoryResponse> categories) {
        this.categories = categories;
    }
}
