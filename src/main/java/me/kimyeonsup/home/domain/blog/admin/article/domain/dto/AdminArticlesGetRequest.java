package me.kimyeonsup.home.domain.blog.admin.article.domain.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminArticlesGetRequest {

    private Long categoryId;
    private Long menuId;
    private String title;
    private String visibility;

    @Min(value = 1)
    private int pageNumber;

    @Min(value = 10)
    private int pageSize;

    private String sortBy;
    private String direction;
}
