package me.kimyeonsup.home.domain.blog.admin.article.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ArticleBatchDeleteRequest {

    @Size(min = 1)
    private Long[] articleIds;

}
