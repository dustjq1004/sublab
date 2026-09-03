package me.kimyeonsup.home.domain.blog.admin.menu.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdateMenuRequest {
    private long id;
    private Long categoryId;
    private String emoji;
    private String name;
    private String description;
}
