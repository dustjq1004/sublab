package me.kimyeonsup.home.domain.article.adaptor.in.web.blog.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.kimyeonsup.home.domain.article.domain.model.Article;
import me.kimyeonsup.home.domain.article.domain.model.Thumbnail;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AddArticleRequest {

    @NotBlank
    @Size(max = 100)
    private String title;

    @Size(max = 100)
    private String subTitle;
    private String content;

    @NotNull
    private Long menuId;

    private Long draftId;

    private String thumbnailUrl;

    private String thumbnailLogicalFileName;

    @NotBlank
    private String visibility;

    public Article toEntity(String author) {
        Thumbnail thumbnail;
        if (thumbnailUrl != null && !thumbnailUrl.trim().isEmpty()) {
            thumbnail = new Thumbnail(thumbnailUrl, thumbnailLogicalFileName);
        } else {
            thumbnail = Thumbnail.of("", "");
        }

        return Article.builder()
                .title(title)
                .content(content)
                .subTitle(subTitle)
                .menu(Menu.builder().id(menuId).build())
                .thumbnailUrl(thumbnail)
                .author(author)
                .visibility(visibility)
                .build();
    }
}
