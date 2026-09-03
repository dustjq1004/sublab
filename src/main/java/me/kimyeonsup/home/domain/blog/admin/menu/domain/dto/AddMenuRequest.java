package me.kimyeonsup.home.domain.blog.admin.menu.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Menu;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AddMenuRequest {

    @NotNull
    private Long categoryId;

    private String emoji;

    @NotBlank
    private String name;

    private String description;

    public Menu toEntity(Category category) {
        return Menu.builder()
                .emoji(emoji)
                .name(name)
                .description(description)
                .category(category)
                .build();
    }
}
