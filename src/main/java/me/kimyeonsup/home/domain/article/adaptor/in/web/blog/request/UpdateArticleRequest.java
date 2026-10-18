package me.kimyeonsup.home.domain.article.adaptor.in.web.blog.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdateArticleRequest {

    @NotNull
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String title;

    @Size(max = 100)
    private String subTitle;
    private String content;

    @NotNull
    private Long menuId;

    private String thumbnailUrl;
    private String thumbnailLogicalFileName;

    @NotBlank
    private String visibility;
}
