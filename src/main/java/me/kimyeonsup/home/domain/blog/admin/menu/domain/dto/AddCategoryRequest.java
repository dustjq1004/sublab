package me.kimyeonsup.home.domain.blog.admin.menu.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Category;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AddCategoryRequest {

    @NotBlank
    private String name;

    public Category toEntity() {
        return Category.builder()
                .name(name)
                .build();
    }
}
