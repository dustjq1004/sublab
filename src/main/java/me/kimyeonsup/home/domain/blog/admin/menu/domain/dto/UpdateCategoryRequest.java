package me.kimyeonsup.home.domain.blog.admin.menu.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdateCategoryRequest {
    private long id;
    private String name;
}
