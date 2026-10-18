package me.kimyeonsup.home.domain.article.adaptor.in.web.admin.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArticlePagingRequest {

    private Long categoryId;
    private Long menuId;
    private String menuName;
    private String title;
    private String visibility;
    private String searchKeyword;

    @Min(value = 1)
    private int pageNumber;

    @Min(value = 10)
    private int pageSize;

    private String sortBy;
    private String direction;

    public String getSearchKeyword() {
        return searchKeyword != null ? searchKeyword : "";
    }
}
